package im.conversations.compliance.xmpp;

import im.conversations.compliance.persistence.ServerStore;
import im.conversations.compliance.persistence.TestResultStore;
import im.conversations.compliance.pojo.Configuration;
import im.conversations.compliance.pojo.Credential;
import im.conversations.compliance.pojo.Iteration;
import im.conversations.compliance.pojo.Result;
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

    private PeriodicTestRunner() {
        scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(1);
        List<Iteration> iterations = TestResultStore.INSTANCE.getIterations();
        long minutes = Configuration.getInstance().getTestRunInterval();
        if(iterations.size() > 0) {
            Iteration lastIteration= iterations.get(iterations.size() - 1);
            Duration between = Duration.between(lastIteration.getBegin(), Instant.now());
            minutes = between.toMinutes();
        }
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
        if (credentials.isEmpty()) {
            System.out.println("No credentials found. Periodic tests skipped");
            return;
        }
        credentialsMarkedForRemoval = Collections.synchronizedList(new ArrayList());
        Instant beginTime = Instant.now();
        System.out.printf("Started running periodic tests #%d at %s%n", TestResultStore.INSTANCE.getIterations().size(), beginTime);

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
        Iteration iteration = new Iteration(TestResultStore.INSTANCE.getIterations().size(), beginTime, endTime);

        //Add results to database
        TestResultStore.INSTANCE.putPeriodicTestResults(rdpList, iteration);

        System.out.printf("Ended running periodic tests #%d at %s%n", iteration.getIterationNumber(), endTime);
        postTestsRun();
    }

    private void postTestsRun() {
        //Remove invalid credential
        credentialsMarkedForRemoval.forEach(ServerStore.INSTANCE::removeCredential);
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
