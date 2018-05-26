package im.conversations.compliance.xmpp;

import im.conversations.compliance.persistence.ServerStore;
import im.conversations.compliance.persistence.TestResultStore;
import im.conversations.compliance.pojo.Configuration;
import im.conversations.compliance.pojo.Credential;
import im.conversations.compliance.pojo.Iteration;
import im.conversations.compliance.pojo.Result;
import rocks.xmpp.core.XmppException;
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
    private Iteration lastIteration;

    private PeriodicTestRunner() {
        scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(1);
        lastIteration = TestResultStore.INSTANCE.getLastIteration();
        if (lastIteration == null) {
            lastIteration = new Iteration(-1, Instant.MIN, Instant.MIN);
        }
        Duration between = Duration.between(lastIteration.getBegin(), Instant.now());
        long minutes = between.toMinutes();
        long minutesLeft = Configuration.getInstance().getTestRunInterval() - minutes;
        if (minutesLeft > 0)
            System.out.println("Next test scheduled " + minutesLeft + " minutes from now");
        // Run on start
        scheduledThreadPoolExecutor.scheduleAtFixedRate(this, minutesLeft, Configuration.getInstance().getTestRunInterval(), TimeUnit.MINUTES);
    }

    public static PeriodicTestRunner getInstance() {
        return INSTANCE;
    }

    @Override
    public void run() {
        List<Credential> credentials = ServerStore.INSTANCE.getCredentials();
        if (credentials.isEmpty())
            return;
        final List<Credential> credentialsMarkedForRemoval = Collections.synchronizedList(new ArrayList());
        Instant beginTime = Instant.now();
        System.out.printf("Started running periodic tests #%d at %s%n", lastIteration.getIterationNumber() + 1, beginTime);

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
                    } catch (XmppException ex) {
                        ex.printStackTrace();
                    }
                    return rdp;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        Instant endTime = Instant.now();
        lastIteration = new Iteration(lastIteration.getIterationNumber() + 1, beginTime, endTime);

        //Add results to database
        TestResultStore.INSTANCE.putPeriodicTestResults(rdpList, lastIteration);

        //Remove invalid credential
        credentialsMarkedForRemoval.forEach(credential -> {
            ServerStore.INSTANCE.removeCredential(credential);
        });

        System.out.printf("Ended running periodic tests #%d at %s%n", lastIteration.getIterationNumber(), endTime);
    }

    public class ResultDomainPair {
        String domain;
        List<Result> results;

        public ResultDomainPair(String domain, List<Result> results) {
            this.domain = domain;
            this.results = results;
        }

        public String getDomain() {
            return domain;
        }

        public List<Result> getResults() {
            return results;
        }
    }

}
