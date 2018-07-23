package im.conversations.compliance.xmpp;

import im.conversations.compliance.email.MailBuilder;
import im.conversations.compliance.email.MailSender;
import im.conversations.compliance.persistence.DBOperations;
import im.conversations.compliance.pojo.*;
import im.conversations.compliance.web.WebUtils;
import org.simplejavamail.email.Email;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rocks.xmpp.core.sasl.AuthenticationException;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class PeriodicTestRunner implements Runnable {
    private static final PeriodicTestRunner INSTANCE = new PeriodicTestRunner();
    private ScheduledThreadPoolExecutor scheduledThreadPoolExecutor;
    private List<Credential> credentialsMarkedForRemoval;
    private static final Logger LOGGER = LoggerFactory.getLogger(PeriodicTestRunner.class);

    private PeriodicTestRunner() {
        scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(1);
        Iteration lastIteration = DBOperations.getLatestIteration().orElse(null);
        long minutesLeft = 0;
        if (lastIteration != null) {
            Duration between = Duration.between(lastIteration.getBegin(), Instant.now());
            minutesLeft = Configuration.getInstance().getTestRunInterval() - between.toMinutes();
        }
        if (minutesLeft > 0) {
            LOGGER.info("Next test scheduled " + minutesLeft + " minutes from now");
        }
        // Run on start
        scheduledThreadPoolExecutor.scheduleAtFixedRate(this, minutesLeft, Configuration.getInstance().getTestRunInterval(), TimeUnit.MINUTES);
    }

    public static PeriodicTestRunner getInstance() {
        return INSTANCE;
    }


    @Override
    public void run() {
        if (!WebUtils.isConnected()) {
            LOGGER.warn("Internet connection not available. Retrying in 5 minutes");
            scheduledThreadPoolExecutor.schedule(this, 5, TimeUnit.MINUTES);
            return;
        }
        List<Credential> credentials = DBOperations.getCredentials();
        if (credentials.isEmpty()) {
            LOGGER.info("No credentials found. Periodic test skipped");
            return;
        }
        credentialsMarkedForRemoval = Collections.synchronizedList(new ArrayList());
        Iteration iteration = DBOperations.getLatestIteration().orElse(null);
        int iterationNumber = -1;
        if (iteration != null) {
            iterationNumber = iteration.getIterationNumber();
        }
        Instant beginTime = Instant.now();
        System.out.printf("Started running periodic tests #%d at %s%n", iterationNumber + 1, beginTime);

        List<ResultDomainPair> rdpList = credentials.parallelStream()
                .map(credential -> {
                    ResultDomainPair rdp = null;
                    try {
                        rdp = new ResultDomainPair(credential.getDomain(), TestExecutor.executeTestsFor(credential));
                    } catch (TestFactory.TestCreationException e) {
                        e.printStackTrace();
                    } catch (AuthenticationException ex) {
                        synchronized (credentialsMarkedForRemoval) {
                            credentialsMarkedForRemoval.add(credential);
                        }
                        ex.printStackTrace();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    return rdp;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        Instant endTime = Instant.now();
        //Add results to database
        DBOperations.addPeriodicResults(rdpList, beginTime, endTime);

        System.out.printf("Ended running periodic tests #%d at %s%n", iterationNumber + 1, Instant.now());
        postTestsRun();
    }

    private void postTestsRun() {
        //Remove invalid credential
        if (Configuration.getInstance().getMailConfig() != null) {
            sendMailsForChange();
        }
        for (Credential credential : credentialsMarkedForRemoval) {
            DBOperations.removeCredential(credential);
            if (Configuration.getInstance().getMailConfig() != null) {
                List<Email> mails = MailBuilder.getInstance().buildCredentialRemovalEmails(credential);
                if (!mails.isEmpty()) {
                    LOGGER.info(
                            "Sending email to subscribers of "
                                    + credential.getDomain()
                                    + " notifying about credential failing to authenticate"
                    );
                }
                MailSender.sendMails(mails);
            }
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
                }
                MailSender.sendMails(mails);
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
                if(!mails.isEmpty()) {
                    LOGGER.info(
                            "Sending email to subscribers of "
                                    + domain
                                    + " notifying about changes in its compliance result"
                    );
                }
                MailSender.sendMails(mails);
            }
        }
    }
}
