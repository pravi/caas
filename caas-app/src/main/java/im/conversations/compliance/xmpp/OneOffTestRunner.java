package im.conversations.compliance.xmpp;

import im.conversations.compliance.persistence.TestResultStore;
import im.conversations.compliance.pojo.Credential;
import im.conversations.compliance.pojo.Result;
import im.conversations.compliance.utils.ExceptionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class OneOffTestRunner {
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
                System.out.println("Added " + credential.getDomain() + " to test running list");
            }
        }
    }

    public static boolean addResultListener(String domain, ResultListener resultListener) {
        try {
            synchronized (testRunningFor) {
                testRunningFor.get(domain).add(resultListener);
                System.out.println("Registered a listener for one-off tests for " + domain);
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
            TestResultStore.INSTANCE.putOneOffTestResults(credential.getDomain(), results);
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
    }

    public interface ResultListener {
        void onResult(boolean success, String msg);
    }

}
