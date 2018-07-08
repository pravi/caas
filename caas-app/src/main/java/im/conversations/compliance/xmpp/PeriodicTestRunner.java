package im.conversations.compliance.xmpp;

import im.conversations.compliance.persistence.DBOperations;
import im.conversations.compliance.pojo.Configuration;
import im.conversations.compliance.pojo.Credential;
import im.conversations.compliance.pojo.Iteration;
import im.conversations.compliance.pojo.Result;
import im.conversations.compliance.web.WebUtils;
import rocks.xmpp.core.sasl.AuthenticationException;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class PeriodicTestRunner implements Runnable {
    private static final PeriodicTestRunner INSTANCE = new PeriodicTestRunner();
    private ScheduledThreadPoolExecutor scheduledThreadPoolExecutor;
    private List<Credential> credentialsMarkedForRemoval;

    private PeriodicTestRunner() {
        scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(1);
        Iteration lastIteration = DBOperations.getLatestIteration().orElse(null);
        long minutesLeft = 0;
        if (lastIteration != null) {
            Duration between = Duration.between(lastIteration.getBegin(), Instant.now());
            minutesLeft = Configuration.getInstance().getTestRunInterval() - between.toMinutes();
        }
        if (minutesLeft > 0) {
            System.out.println("Next test scheduled " + minutesLeft + " minutes from now");
        }
        // Run on start
        scheduledThreadPoolExecutor.scheduleAtFixedRate(this, minutesLeft, Configuration.getInstance().getTestRunInterval(), TimeUnit.MINUTES);
    }

    public static PeriodicTestRunner getInstance() {
        return INSTANCE;
    }


    @Override
    public void run() {
        if(!WebUtils.isConnected()) {
            System.out.println("Internet connection not available. Retrying in 5 minutes");
            scheduledThreadPoolExecutor.schedule(this,5, TimeUnit.MINUTES);
            return;
        }
        List<Credential> credentials = DBOperations.getCredentials();
        if (credentials.isEmpty()) {
            System.out.println("No credentials found. Periodic test skipped");
            return;
        }
        credentialsMarkedForRemoval = Collections.synchronizedList(new ArrayList());
        Iteration iteration = DBOperations.getLatestIteration().orElse(null);
        int iterationNumber = -1;
        if(iteration != null) {
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

        System.out.printf("Ended running periodic tests #%d at %s%n", iterationNumber + 1, beginTime);
        postTestsRun();
    }

    private void postTestsRun() {
        //Remove invalid credential
        credentialsMarkedForRemoval.forEach(DBOperations::removeCredential);
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
