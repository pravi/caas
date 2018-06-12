package im.conversations.compliance.persistence;

import im.conversations.compliance.pojo.Configuration;
import im.conversations.compliance.pojo.HistoricalSnapshot;
import im.conversations.compliance.pojo.Iteration;
import im.conversations.compliance.pojo.Result;
import im.conversations.compliance.xmpp.PeriodicTestRunner;
import im.conversations.compliance.xmpp.utils.TestUtils;
import org.sql2o.Connection;
import org.sql2o.Query;
import org.sql2o.Sql2o;
import org.sql2o.data.Row;
import org.sql2o.data.Table;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

public class TestResultStore {
    public static final TestResultStore INSTANCE = new TestResultStore();
    private final HashMap<String, List<Result>> resultsByServer = new HashMap<>();
    private final Sql2o database;
    private List<Iteration> iterations;
    private HashMap<String, List<HistoricalSnapshot>> historicSnapshotsByServer;
    private HashMap<String, List<HistoricalSnapshot>> historicSnapshotsByTest;
    private HashMap<String, HashMap<String, Boolean>> resultsByTests = new HashMap<>();

    private TestResultStore() {
        final String dbFilename = Configuration.getInstance().getStoragePath() + getClass().getSimpleName().toLowerCase(Locale.US) + ".db";
        this.database = new Sql2o("jdbc:sqlite:" + dbFilename, null, null);
        synchronized (this.database) {
            try (Connection con = this.database.open()) {
                // Create all the tables, if they don't exist
                con.createQuery("create table if not exists current_tests(" +
                        "domain text," +
                        "test text," +
                        "success integer," +
                        "timestamp integer," +
                        "primary key(domain,test))"
                ).executeUpdate();

                con.createQuery("create table if not exists periodic_tests(" +
                        "domain text," +
                        "test text," +
                        "success integer," +
                        "iteration_number integer," +
                        "primary key(domain,test,iteration_number))"
                ).executeUpdate();

                con.createQuery("create table if not exists periodic_test_iterations(" +
                        "iteration_number integer primary key," +
                        "begin_time integer," +
                        "end_time integer)"
                ).executeUpdate();

            }
        }
        fetchIterations();
        fetchResults();
        fetchHistoricalSnapshotsByServer();
        fetchHistoricalSnapshotsByTest();
    }

    private void fetchIterations() {
        synchronized (this.database) {
            try (Connection con = this.database.open()) {
                //Add iteration lists to an object
                iterations = con.createQuery("select iteration_number, begin_time, end_time from periodic_test_iterations")
                        .addColumnMapping("iteration_number", "iterationNumber")
                        .addColumnMapping("begin_time", "begin")
                        .addColumnMapping("end_time", "end")
                        .executeAndFetch(Iteration.class);
                iterations.sort(Comparator.comparingInt(Iteration::getIterationNumber));
                if (iterations.size() == 0) return;
                int last = iterations.size() - 1;
                if (iterations.get(last).getIterationNumber() != iterations.size() - 1) {
                    throw new IllegalStateException("Iterations size unequal in both the tables");
                }
            }
        }
    }

    public List<Iteration> getIterations() {
        return Collections.unmodifiableList(iterations);
    }

    public List<Result> getResultsFor(String domain) {
        return Collections.unmodifiableList(resultsByServer.get(domain));
    }

    public Map<String, Boolean> getServerResultHashMapFor(String test) {
        return Collections.unmodifiableMap(resultsByTests.get(test));
    }

    public List<HistoricalSnapshot> getHistoricalSnapshotsForServer(String domain) {
        if (historicSnapshotsByServer.get(domain) == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(historicSnapshotsByServer.get(domain));
    }

    public List<HistoricalSnapshot> getHistoricalSnapshotsForTest(String test) {
        if (historicSnapshotsByTest.get(test) == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(historicSnapshotsByTest.get(test));
    }

    public boolean putOneOffTestResults(String domain, List<Result> results) {
        return addToCurrentResults(domain, results);
    }

    public boolean putPeriodicTestResults(List<PeriodicTestRunner.ResultDomainPair> rdpList, Iteration iteration) {
        //Check if iteration number is correct
        if (iteration.getIterationNumber() != iterations.size()) {
            System.err.println("Iterations donot match");
            return false;
        }

        //Add to periodic results
        boolean status = addToPeriodicResults(rdpList, iteration);

        //Add to current results
        rdpList.forEach(rdp -> addToCurrentResults(rdp.getDomain(), rdp.getResults()));

        return status;
    }


    public Instant getLastRunFor(String domain) {
        synchronized (this.database) {
            try (Connection con = this.database.open()) {
                Instant lastRun = con.createQuery("select timestamp from current_tests where domain=:domain limit 1;")
                        .addParameter("domain", domain)
                        .executeScalar(Instant.class);
                return lastRun;
            } catch (Exception ex) {
                ex.printStackTrace();
                return Instant.now();
            }
        }
    }

    private boolean addToCurrentResults(String domain, List<Result> results) {
        Instant timestamp = Instant.now();
        synchronized (this.database) {
            try (Connection con = this.database.beginTransaction(java.sql.Connection.TRANSACTION_SERIALIZABLE)) {
                Query query = con.createQuery("insert or replace into current_tests(domain,test,success,timestamp) values(:domain,:test,:success,:timestamp)");
                results.forEach(
                        result -> query
                                .addParameter("test", result.getTest().short_name())
                                .addParameter("success", result.isSuccess())
                                .addParameter("domain", domain)
                                .addParameter("timestamp", timestamp)
                                .addToBatch()
                );
                query.executeBatch();
                con.commit();
                fetchResults();
            } catch (Exception ex) {
                ex.printStackTrace();
                return false;
            }
            return true;
        }
    }

    private boolean addToPeriodicResults(List<PeriodicTestRunner.ResultDomainPair> rdpList, Iteration iteration) {
        synchronized (this.database) {
            try (Connection con = this.database.beginTransaction(java.sql.Connection.TRANSACTION_SERIALIZABLE)) {
                Query resultInsertQuery = con.createQuery("insert or replace into periodic_tests(domain,test,success,iteration_number) values(:domain,:test,:success,:iteration)");
                rdpList.forEach(rdp -> {
                    String domain = rdp.getDomain();
                    rdp.getResults().forEach(
                            result -> resultInsertQuery
                                    .addParameter("test", result.getTest().short_name())
                                    .addParameter("success", result.isSuccess())
                                    .addParameter("domain", domain)
                                    .addParameter("iteration", iteration.getIterationNumber())
                                    .addToBatch());
                });

                //Insert periodic results
                resultInsertQuery.executeBatch();

                //Insert periodic result iteration details
                con.createQuery("insert into periodic_test_iterations(iteration_number,begin_time,end_time) " +
                        "values(:iteration,:begin,:end)")
                        .addParameter("iteration", iteration.getIterationNumber())
                        .addParameter("begin", iteration.getBegin())
                        .addParameter("end", iteration.getEnd())
                        .executeUpdate();

                con.commit();
            } catch (Exception ex) {
                ex.printStackTrace();
                return false;
            }
        }
        //Get iterations
        fetchIterations();

        //Get historical snapshots
        fetchHistoricalSnapshotsByServer();
        fetchHistoricalSnapshotsByTest();
        return true;
    }

    private void fetchHistoricalSnapshotsByTest() {
        historicSnapshotsByTest = new HashMap<>();
        synchronized (this.database) {
            try (Connection connection = this.database.open()) {
                for (String test : TestUtils.getAllComplianceTestNames()) {
                    HashMap<Integer, HistoricalSnapshot.Change> resultChanges = new HashMap<>();
                    //TODO: Check if server's listed is set to true
                    for (String domain : ServerStore.INSTANCE.getServerNames()) {
                        Table table = connection.createQuery("select iteration_number,success from periodic_tests " +
                                "where domain=:domain and test=:test")
                                .addParameter("domain", domain)
                                .addParameter("test", test)
                                .executeAndFetchTable();
                        addChanges(resultChanges, domain, table.rows());
                    }
                    List<HistoricalSnapshot> historicalSnapshots = new ArrayList<>();
                    for (int iterationNumber : resultChanges.keySet()) {
                        List<Integer> results = connection.createQuery("select success from periodic_tests where iteration_number = :it and test=:test and domain in (:domains)")
                                .addParameter("it", iterationNumber)
                                .addParameter("test", test)
                                .addParameter("domains", ServerStore.INSTANCE.getServerNames())
                                .executeScalarList(Integer.class);
                        int total = 0;
                        int pass = 0;
                        for (int result : results) {
                            pass += result;
                            total++;
                        }
                        String timestamp = connection.createQuery("select begin_time from periodic_test_iterations where iteration_number = :it")
                                .addParameter("it", iterationNumber)
                                .executeScalar(String.class);
                        historicalSnapshots.add(new HistoricalSnapshot(iterationNumber, timestamp, pass, total, resultChanges.get(iterationNumber)));
                    }
                    historicalSnapshots.sort(Comparator.comparingInt(HistoricalSnapshot::getIteration));
                    historicSnapshotsByTest.put(test, historicalSnapshots);
                }
            }
        }
    }

    private void addChanges(HashMap<Integer, HistoricalSnapshot.Change> changes, String value, List<Row> iterationResultPairs) {
        int lastResult = -1;
        int len = iterationResultPairs.size();
        for (int i = 0; i < len; i++) {
            Row row = iterationResultPairs.get(i);
            int iterationNumber = row.getInteger("iteration_number");
            int success = row.getInteger("success");
            // Only add to change if the new result is different than the older results
            if (lastResult != success) {
                if (changes.get(iterationNumber) == null) {
                    changes.put(iterationNumber, new HistoricalSnapshot.Change());
                }
                if (success == 0) {
                    changes.get(iterationNumber).getFail().add(value);
                } else {
                    changes.get(iterationNumber).getPass().add(value);
                }
                lastResult = success;
            }
            //Add the last point on graph (if there are more than 1 points)
            else if (len > 1 && i == (len - 1)) {
                if (changes.get(iterationNumber) == null) {
                    changes.put(iterationNumber, new HistoricalSnapshot.Change());
                }
            }
        }
    }

    private void fetchHistoricalSnapshotsByServer() {
        historicSnapshotsByServer = new HashMap<>();
        synchronized (this.database) {
            try (Connection connection = this.database.open()) {
                for (String domain : ServerStore.INSTANCE.getServerNames()) {
                    HashMap<Integer, HistoricalSnapshot.Change> testResultChanges = new HashMap<>();
                    for (String test : TestUtils.getTestNames()) {
                        Table table = connection.createQuery("select iteration_number,success from periodic_tests " +
                                "where domain=:domain and test=:test")
                                .addParameter("domain", domain)
                                .addParameter("test", test)
                                .executeAndFetchTable();
                        addChanges(testResultChanges, test, table.rows());
                    }
                    List<HistoricalSnapshot> historicalSnapshots = new ArrayList<>();
                    for (int iterationNumber : testResultChanges.keySet()) {
                        List<Integer> results = connection.createQuery("select success from periodic_tests where iteration_number = :it and domain=:domain and test in (:tests)")
                                .addParameter("it", iterationNumber)
                                .addParameter("tests", TestUtils.getTestNames())
                                .addParameter("domain", domain)
                                .executeScalarList(Integer.class);
                        int total = 0;
                        int pass = 0;
                        for (int result : results) {
                            pass += result;
                            total++;
                        }
                        String timestamp = connection.createQuery("select begin_time from periodic_test_iterations where iteration_number = :it")
                                .addParameter("it", iterationNumber)
                                .executeScalar(String.class);
                        historicalSnapshots.add(new HistoricalSnapshot(iterationNumber, timestamp, pass, total, testResultChanges.get(iterationNumber)));
                    }
                    historicalSnapshots.sort(Comparator.comparingInt(HistoricalSnapshot::getIteration));
                    historicSnapshotsByServer.put(domain, historicalSnapshots);
                }
            }
        }
    }

    private void fetchResults() {
        synchronized (this.database) {
            try (Connection con = this.database.open()) {
                List<String> domains = con.createQuery("select distinct domain from current_tests").executeAndFetch(String.class);
                domains.forEach(domain -> {
                    Table table = con.createQuery("select test,success from current_tests where domain=:domain")
                            .addParameter("domain", domain)
                            .executeAndFetchTable();
                    ArrayList<Result> r = table.rows().stream()
                            .map(row -> new Result(
                                    TestUtils.getTestFrom(row.getString("test")),
                                    (row.getInteger("success") == 1)))
                            .collect(Collectors.toCollection(ArrayList::new));
                    resultsByServer.put(domain, r);
                });
                TestUtils.getAllComplianceTestNames().forEach(test -> {
                    Table table = con.createQuery("select domain,success from current_tests where test=:test")
                            .addParameter("test", test)
                            .executeAndFetchTable();
                    HashMap<String, Boolean> testResults = new HashMap<>();
                    table.rows().stream().forEach(
                            row -> {
                                testResults.put(
                                        row.getString("domain"),
                                        row.getBoolean("success")
                                );
                            }
                    );
                    resultsByTests.put(test, testResults);
                });
            }
        }
    }
}
