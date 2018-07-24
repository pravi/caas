package im.conversations.compliance.xmpp;

import im.conversations.compliance.persistence.DBOperations;
import im.conversations.compliance.pojo.Credential;
import im.conversations.compliance.pojo.Result;
import im.conversations.compliance.utils.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class OneOffTestRunner {
    private static final Logger LOGGER = LoggerFactory.getLogger(OneOffTestRunner.class);
    private static final ConcurrentHashMap<String, ArrayList<ResultListener>> testRunningFor;
    private static final ExecutorService executorService = Executors.newFixedThreadPool(4);

    static {
        testRunningFor = new ConcurrentHashMap<>();
    }

    public static void runOneOffTestsFor(Credential credential) {
        List<Result> results = null;
        synchronized (testRunningFor) {
            if (!testRunningFor.containsKey(credential.getDomain())) {
                testRunningFor.put(credential.getDomain(), new ArrayList<>());
                executorService.submit(() -> startTests(credential));
                LOGGER.info("Added " + credential.getDomain() + " to test running list");
            }
        }
    }

    public static boolean addResultListener(String domain, ResultListener resultListener) {
        try {
            synchronized (testRunningFor) {
                testRunningFor.get(domain).add(resultListener);
                LOGGER.info("Registered a listener for one-off tests for " + domain);
            }
        } catch (Exception ex) {
            return false;
        }
        return true;
    }

    public static boolean removeResultListener(String domain, ResultListener resultListener) {
        try {
            synchronized (testRunningFor) {
                testRunningFor.get(domain).remove(resultListener);
            }
        } catch (Exception ex) {
            return false;
        }
        return true;
    }

    private static void startTests(Credential credential) {
        List<Result> results;
        try {
            results = TestExecutor.executeTestsFor(credential);
            DBOperations.addCurrentResults(
                    credential.getDomain(),
                    results,
                    Instant.now()
            );
            synchronized (testRunningFor) {
                testRunningFor.get(credential.getDomain()).forEach(it -> it.onResult(true, "OK"));
                testRunningFor.remove(credential.getDomain());
            }
        } catch (Exception ex) {
            String msg = ExceptionUtils.getRootCause(ex).getMessage();
            synchronized (testRunningFor) {
                testRunningFor.get(credential.getDomain()).forEach(it -> it.onResult(false, msg));
                testRunningFor.remove(credential.getDomain());
            }
            ex.printStackTrace();
        }
        LOGGER.info("One-off tests completed for " + credential.getDomain());
    }

    public interface ResultListener {
        void onResult(boolean success, String msg);
    }

}
