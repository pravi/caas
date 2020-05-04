package im.conversations.compliance.xmpp;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import im.conversations.compliance.email.MailBuilder;
import im.conversations.compliance.email.MailSender;
import im.conversations.compliance.persistence.DBOperations;
import im.conversations.compliance.pojo.*;
import im.conversations.compliance.web.WebUtils;
import org.simplejavamail.email.Email;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rocks.xmpp.core.XmppException;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class PeriodicTestRunner implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(PeriodicTestRunner.class);
    private final static ScheduledThreadPoolExecutor SCHEDULED_THREAD_POOL_EXECUTOR = new ScheduledThreadPoolExecutor(1);
    private static final PeriodicTestRunner INSTANCE = new PeriodicTestRunner();
    private final static ExecutorService THREAD_POOL_EXECUTOR_SERVICE = Executors.newFixedThreadPool(12);
    private final Queue<Credential> credentialsMarkedForRemoval = new ArrayDeque<>();

    private PeriodicTestRunner() {
        final var interval = Configuration.getInstance().getTestRunInterval();
        final Iteration lastIteration = DBOperations.getLatestIteration().orElse(null);
        long minutesLeft = 0;
        if (lastIteration != null) {
            Duration between = Duration.between(lastIteration.getBegin(), Instant.now());
            minutesLeft = interval - between.toMinutes();
        }
        if (minutesLeft > 0) {
            LOGGER.info("Next test scheduled " + minutesLeft + " minutes from now");
        }
        // Run on start
        SCHEDULED_THREAD_POOL_EXECUTOR.scheduleAtFixedRate(this, minutesLeft, interval, TimeUnit.MINUTES);
    }

    public static PeriodicTestRunner getInstance() {
        return INSTANCE;
    }


    @Override
    public void run() {
        if (!WebUtils.isConnected()) {
            LOGGER.warn("Internet connection not available. Retrying in 5 minutes");
            SCHEDULED_THREAD_POOL_EXECUTOR.schedule(this, 5, TimeUnit.MINUTES);
            return;
        }
        List<Credential> credentials = DBOperations.getCredentials();
        if (credentials.isEmpty()) {
            LOGGER.info("No credentials found. Periodic test skipped");
            return;
        }
        Iteration iteration = DBOperations.getLatestIteration().orElse(null);
        int iterationNumber = -1;
        if (iteration != null) {
            iterationNumber = iteration.getIterationNumber();
        }
        Instant beginTime = Instant.now();
        LOGGER.info("Started running periodic tests #" + (iterationNumber + 1) + " at " + beginTime);

        List<ListenableFuture<ResultDomainPair>> futures = Lists.transform(credentials, c -> Futures.submit(() -> performTest(c), THREAD_POOL_EXECUTOR_SERVICE));
        final List<ResultDomainPair> results;
        try {
            results = Futures.allAsList(futures).get().stream().filter(Objects::nonNull).collect(Collectors.toList());
        } catch (Exception e) {
            LOGGER.error("Unable to execute tests", Throwables.getRootCause(e));
            return;
        }
        Instant endTime = Instant.now();
        //Add results to database
        DBOperations.addPeriodicResults(results, beginTime, endTime);
        LOGGER.info("Attempted {} tests. Got {} results", credentials.size(), results.size());
        LOGGER.info("Ended running periodic tests #" + (iterationNumber + 1) + " at " + endTime);
        postTestsRun();
    }

    private static ResultDomainPair performTest(Credential credential) {
        try {
            final List<Result> result = TestExecutor.executeTestsFor(
                    credential,
                    (client) -> ServerMetadataChecker.updateServerMetadataFor(client, credential)
            );
            LOGGER.info("Completed test for {}", credential.getDomain());
            DBOperations.setSuccess(credential);
            return new ResultDomainPair(credential.getDomain(), result);
        } catch (final Exception e) {
            final Class<? extends Exception> clazz = e.getClass();
            LOGGER.warn("Unable to perform test for {} - {}", credential.getDomain(), clazz.getSimpleName());
            DBOperations.setFailure(credential, clazz.getSimpleName());
            return null;
        }
    }

    private void postTestsRun() {
        //Remove invalid credential
        Credential credential;
        synchronized (this.credentialsMarkedForRemoval) {
            while ((credential = credentialsMarkedForRemoval.poll()) != null) {
                DBOperations.removeCredential(credential);
                if (Configuration.getInstance().getMailConfig() != null) {
                    List<Email> mails = MailBuilder.getInstance().buildCredentialRemovalEmails(credential);
                    if (!mails.isEmpty()) {
                        LOGGER.info(
                                "Sending email to subscribers of "
                                        + credential.getDomain()
                                        + " notifying about credential failing to authenticate"
                        );
                        MailSender.sendMails(mails);
                        LOGGER.info(
                                "Sent email to subscribers of "
                                        + credential.getDomain()
                                        + " notifying about credential failing to authenticate"
                        );
                    }
                }
            }
        }
        if (Configuration.getInstance().getMailConfig() != null) {
            sendMailsForChange();
        }
    }

    private void sendMailsForChange() {
        Iteration iteration = DBOperations.getLatestIteration().orElse(null);
        if (iteration == null) {
            return;
        }
        List<Server> servers = DBOperations.getServers(false);
        for (Server server : servers) {
            String domain = server.getDomain();
            int newIteration = iteration.getIterationNumber();
            int oldIteration = newIteration - 1;
            List<Result> newResults = DBOperations.getHistoricalResultsFor(domain, newIteration);
            List<Result> oldResults = DBOperations.getHistoricalResultsFor(domain, oldIteration);
            //If results are unavailable due to error, notify subscribers
            if (newResults.isEmpty()) {
                List<Email> mails = MailBuilder.getInstance().buildResultsNotAvailableMails(domain, iteration);
                if (!mails.isEmpty()) {
                    LOGGER.info(
                            "Sending email to subscribers of "
                                    + domain
                                    + " notifying about error while getting compliance results"
                    );
                    MailSender.sendMails(mails);
                    LOGGER.info(
                            "Sent email to subscribers of "
                                    + domain
                                    + " notifying about error while getting compliance results"
                    );
                }
            }
            HistoricalSnapshot.Change change = new HistoricalSnapshot.Change();
            for (Result result : newResults) {
                Result oldResult = oldResults.stream()
                        .filter(it -> it.getTest().short_name().equals(result.getTest().short_name()))
                        .findFirst()
                        .orElse(null);
                if (oldResult == null || oldResult.isSuccess() != result.isSuccess()) {
                    if (result.isSuccess()) {
                        change.getPass().add(result.getTest().short_name());
                    } else {
                        change.getFail().add(result.getTest().short_name());
                    }
                }
            }
            if (!change.getFail().isEmpty() || !change.getPass().isEmpty()) {
                List<Email> mails = MailBuilder.getInstance().buildChangeEmails(change, iteration, domain);
                if (!mails.isEmpty()) {
                    LOGGER.info(
                            "Sending email to subscribers of "
                                    + domain
                                    + " notifying about changes in its compliance result"
                    );
                    MailSender.sendMails(mails);
                    LOGGER.info(
                            "Sent email to subscribers of "
                                    + domain
                                    + " notifying about changes in its compliance result"
                    );
                }
            }
        }
    }
}
