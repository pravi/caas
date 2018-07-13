package im.conversations.compliance.persistence;

import im.conversations.compliance.annotations.ComplianceTest;
import im.conversations.compliance.pojo.*;
import im.conversations.compliance.xmpp.utils.TestUtils;
import org.sql2o.Connection;
import org.sql2o.Query;
import org.sql2o.data.Row;
import org.sql2o.data.Table;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

public class InternalDBOperations {

    public static void init(Connection connection) {
        connection.createQuery("create table if not exists credentials(" +
                "domain text," +
                "jid text primary key," +
                "password text)"
        ).executeUpdate();

        connection.createQuery("create table if not exists servers(" +
                "domain text primary key," +
                "listed integer," +
                "software_name text," +
                "software_version text)"
        ).executeUpdate();

        connection.createQuery("create table if not exists current_tests(" +
                "domain text," +
                "test text," +
                "success integer," +
                "timestamp integer," +
                "primary key(domain,test))"
        ).executeUpdate();

        connection.createQuery("create table if not exists periodic_tests(" +
                "domain text," +
                "test text," +
                "success integer," +
                "iteration_number integer," +
                "primary key(domain,test,iteration_number))"
        ).executeUpdate();

        connection.createQuery("create table if not exists periodic_test_iterations(" +
                "iteration_number integer primary key," +
                "begin_time integer," +
                "end_time integer)"
        ).executeUpdate();

        connection.createQuery("create index if not exists current_results_index on current_tests(domain)")
                .executeUpdate();

        connection.createQuery("create index if not exists periodic_results_index on periodic_tests(iteration_number,domain)")
                .executeUpdate();

        connection.createQuery("create index if not exists periodic_iterations_index" +
                " on periodic_test_iterations(iteration_number)")
                .executeUpdate();

        connection.createQuery("create index if not exists" +
                " credentials_index on credentials(domain)")
                .executeUpdate();

        connection.createQuery("create index if not exists servers_index on servers(domain)")
                .executeUpdate();

    }

    // Methods related to servers

    public static boolean addServer(Connection connection, Server server) {
        String query = "insert into servers(domain,listed,software_name,software_version) values(:domain,:listed,:softwareName,:softwareVersion)";
        connection.createQuery(query).bind(server).executeUpdate();
        return true;
    }

    public static boolean setListed(Connection connection, String domain, boolean listedServer) {
        String query = "update servers set" +
                " listed=:listed" +
                " where domain=:domain";
        connection.createQuery(query)
                .addParameter("listed", listedServer)
                .addParameter("domain", domain)
                .executeUpdate();
        return true;
    }

    public static boolean updateServer(Connection connection, Server newServer) {
        String query = "update servers set" +
                " domain=:domain," +
                " listed=:listed," +
                " software_name=:softwareName," +
                " software_version=:softwareVersion" +
                " where domain=:domain";
        connection.createQuery(query)
                .bind(newServer)
                .executeUpdate();
        return true;
    }

    public static List<Server> getPublicServers(Connection connection) {
        String query = "select domain,software_name,software_version,listed from servers where listed=1";
        return connection.createQuery(query)
                .addColumnMapping("software_name", "softwareName")
                .addColumnMapping("software_version", "softwareVersion")
                .executeAndFetch(Server.class);
    }

    public static List<Server> getAllServers(Connection connection) {
        String query = "select domain,software_name,software_version,listed from servers";
        return connection.createQuery(query)
                .addColumnMapping("software_name", "softwareName")
                .addColumnMapping("software_version", "softwareVersion")
                .executeAndFetch(Server.class);
    }

    public static Server getServer(Connection connection, String domain) {
        String query = "select domain,software_name,software_version,listed from servers where domain = :domain";
        return connection.createQuery(query)
                .addColumnMapping("software_name", "softwareName")
                .addColumnMapping("software_version", "softwareVersion")
                .addParameter("domain", domain)
                .executeAndFetchFirst(Server.class);
    }

    public static boolean removeServer(Connection connection, Server server) {
        String query = "remove from servers where domain=:domain";
        connection.createQuery(query).bind(server).executeUpdate();
        return true;
    }

    // Methods related to iterations

    /**
     * Get the iteration which has the given iteration number
     *
     * @param connection
     * @param iterationNumber The iteration numbers for which the corresponding iteration is to be found
     * @return A list of iterations
     */
    public static Iteration getIteration(Connection connection, int iterationNumber) {
        String query = "select iteration_number, begin_time, end_time from periodic_test_iterations" +
                " where iteration_number = (:iteration) limit 1";
        return connection.createQuery(query)
                .addParameter("iteration", iterationNumber)
                .addColumnMapping("iteration_number", "iterationNumber")
                .addColumnMapping("begin_time", "begin")
                .addColumnMapping("end_time", "end")
                .executeAndFetchFirst(Iteration.class);
    }

    /**
     * Get latest iteration, null if no iteration exists
     *
     * @param connection
     * @return
     */
    public static Iteration getLatestIteration(Connection connection) {
        String query = "select iteration_number, begin_time, end_time from periodic_test_iterations" +
                " where iteration_number = (select max(iteration_number) from periodic_test_iterations)";
        return connection.createQuery(query)
                .addColumnMapping("iteration_number", "iterationNumber")
                .addColumnMapping("begin_time", "begin")
                .addColumnMapping("end_time", "end")
                .executeAndFetchFirst(Iteration.class);
    }

    private static void addIteration(Connection connection, int iterationNumber, Instant beginTime, Instant endTime) {
        String query = "insert into periodic_test_iterations(iteration_number,begin_time,end_time) " +
                "values(:number,:begin,:end)";
        //Insert periodic result iteration details
        try {
            connection.createQuery(query)
                    .addParameter("number", iterationNumber)
                    .addParameter("begin", beginTime)
                    .addParameter("end", endTime)
                    .executeUpdate();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Gets iteration number for next iteration, 0 if no iteration exists
     *
     * @param connection
     * @return
     */
    private static int getNextIterationNumber(Connection connection) {
        String query = "select max(iteration_number) from periodic_test_iterations";
        try {
            return connection.createQuery(query).executeScalar(Integer.class) + 1;
        } catch (Exception ex) {
            return 0;
        }
    }

    // Methods related to historical snapshots
    public static Map<String, List<HistoricalSnapshot>> getHistoricResultsGroupedByServer(Connection connection, List<String> tests, List<String> domains) {
        HashMap<String, List<HistoricalSnapshot>> historicalSnapshotsByServer = new HashMap<>();
        for (String domain : domains) {
            List<HistoricalSnapshot> historicalSnapshots = new ArrayList<>();
            HashMap<Integer, HistoricalSnapshot.Change> resultChanges = new HashMap<>();
            for (String test : tests) {
                Table table = connection.createQuery("select iteration_number,success from periodic_tests " +
                        "where domain=:domain and test=:test")
                        .addParameter("domain", domain)
                        .addParameter("test", test)
                        .executeAndFetchTable();
                addHistoricalPoints(resultChanges, test, table.rows());
            }

            String query = "select count(success),sum(success) from periodic_tests " +
                    "where iteration_number = :it and domain=:domain and test in (:tests)";

            for (int changeIterations : resultChanges.keySet()) {
                Row row = connection.createQuery(query)
                        .addParameter("it", changeIterations)
                        .addParameter("domain", domain)
                        .addParameter("tests", tests)
                        .executeAndFetchTable()
                        .rows()
                        .get(0);
                int total = row.getInteger(0);
                int pass = row.getInteger(1);
                String timestamp = getIteration(connection, changeIterations)
                        .getBegin()
                        .toString();
                historicalSnapshots.add(new HistoricalSnapshot(changeIterations, timestamp, pass, total, resultChanges.get(changeIterations)));
            }

            historicalSnapshots.sort(Comparator.comparingInt(HistoricalSnapshot::getIteration));
            historicalSnapshotsByServer.put(domain, historicalSnapshots);
        }
        return historicalSnapshotsByServer;
    }

    public static HashMap<String, List<HistoricalSnapshot>> getHistoricResultsGroupedByTest(Connection connection, List<String> tests, List<String> domains) {
        HashMap<String, List<HistoricalSnapshot>> historicalSnapshotsByTest = new HashMap<>();
        for (String test : tests) {
            List<HistoricalSnapshot> historicalSnapshots = new ArrayList<>();
            HashMap<Integer, HistoricalSnapshot.Change> resultChanges = new HashMap<>();

            for (String domain : domains) {
                Table table = connection.createQuery("select iteration_number,success from periodic_tests " +
                        "where domain=:domain and test=:test")
                        .addParameter("domain", domain)
                        .addParameter("test", test)
                        .executeAndFetchTable();
                addHistoricalPoints(resultChanges, domain, table.rows());
            }

            String query = "select count(success),sum(success) from periodic_tests " +
                    "where iteration_number = :it and test=:test and domain in (:domains)";

            for (int changeIterations : resultChanges.keySet()) {
                Row row = connection.createQuery(query)
                        .addParameter("it", changeIterations)
                        .addParameter("test", test)
                        .addParameter("domains", domains)
                        .executeAndFetchTable()
                        .rows()
                        .get(0);
                int total = row.getInteger(0);
                int pass = row.getInteger(1);
                String timestamp = getIteration(connection, changeIterations)
                        .getBegin()
                        .toString();
                historicalSnapshots.add(new HistoricalSnapshot(changeIterations, timestamp, pass, total, resultChanges.get(changeIterations)));
            }
            historicalSnapshots.sort(Comparator.comparingInt(HistoricalSnapshot::getIteration));
            historicalSnapshotsByTest.put(test, historicalSnapshots);
        }
        return historicalSnapshotsByTest;
    }

    /**
     * @param connection should be a transaction of type java.sql.Connection.TRANSACTION_SERIALIZABLE for sqlite database
     * @param rdpList
     * @param beginTime
     * @param endTime
     * @return
     */
    public static boolean addPeriodicResults(Connection connection, List<ResultDomainPair> rdpList, Instant beginTime, Instant endTime) {
        //Add the iteration to iterations list
        String query = "insert or replace into periodic_tests(domain,test,success,iteration_number)" +
                "values(:domain,:test,:success,:iteration)";
        int iterationNumber = getNextIterationNumber(connection);
        addIteration(connection, iterationNumber, beginTime, endTime);
        Query resultInsertQuery = connection.createQuery(query);
        rdpList.forEach(rdp -> {
            String domain = rdp.getDomain();
            rdp.getResults().forEach(
                    result -> resultInsertQuery
                            .addParameter("test", result.getTest().short_name())
                            .addParameter("success", result.isSuccess())
                            .addParameter("domain", domain)
                            .addParameter("iteration", iterationNumber)
                            .addToBatch());
        });
        resultInsertQuery.executeBatch();
        for (ResultDomainPair rdp : rdpList) {
            addCurrentResults(connection, rdp.getDomain(), rdp.getResults(), beginTime);
        }
        return true;
    }

    /**
     * Get points at which there is a change in the periodic results, along with a corresponding list of all the changes.
     * It takes all periodic results for a particular test-domain pair and adds all the points at which
     * there is a change along with the value taken as a parameter
     *
     * @param changes              The map to which all the changes will be added
     * @param value                The value which has to be stored corresponding to this change
     *                             (when grouping by test, it is server's domain name and vice versa)
     * @param iterationResultPairs A list of rows of results for a particular domain and test
     */
    private static void addHistoricalPoints(HashMap<Integer, HistoricalSnapshot.Change> changes, String value, List<Row> iterationResultPairs) {
        int lastResult = -1;
        int len = iterationResultPairs.size();
        for (int i = 0; i < len; i++) {
            Row row = iterationResultPairs.get(i);
            int iterationNumber = row.getInteger("iteration_number");
            int success = row.getInteger("success");
            // Only add to change if the new result is different than the older results
            if (lastResult != success) {
                changes.putIfAbsent(iterationNumber, new HistoricalSnapshot.Change());
                if (success == 0) {
                    changes.get(iterationNumber).getFail().add(value);
                } else {
                    changes.get(iterationNumber).getPass().add(value);
                }
                lastResult = success;
            }
            //Add the latest test result on graph (if there are more than 1 points)
            else if (len > 1 && i == (len - 1)) {
                changes.putIfAbsent(iterationNumber, new HistoricalSnapshot.Change());
            }
        }
    }

    public static boolean addCredential(Connection connection, Credential credential) {
        String query = "insert into credentials(domain,jid,password) values(:domain,:jid,:password)";
        connection.createQuery(query).bind(credential).executeUpdate();
        return true;
    }

    public static List<Credential> getCredentials(Connection connection) {
        String query = "select domain,jid,password from credentials";
        return connection.createQuery(query).executeAndFetch(Credential.class);
    }

    public static Credential getCredentialFor(Connection connection, String domain) {
        String query = "select domain,jid,password from credentials where domain=:domain";
        return connection.createQuery(query)
                .addParameter("domain", domain)
                .executeAndFetchFirst(Credential.class);
    }

    public static boolean removeCredential(Connection connection, Credential credential) {
        String query = "delete from credentials where jid=:jid and password=:password";
        connection.createQuery(query).bind(credential).executeUpdate();
        return true;
    }

    /**
     * @param connection
     * @param domain
     * @param results
     * @param timestamp
     * @return
     */
    public static boolean addCurrentResults(Connection connection, String domain, List<Result> results, Instant timestamp) {
        String queryText = "insert or replace into current_tests(domain,test,success,timestamp) " +
                "values(:domain,:test,:success,:timestamp)";
        Query query = connection.createQuery(queryText);
        results.forEach(
                result -> query
                        .addParameter("test", result.getTest().short_name())
                        .addParameter("success", result.isSuccess())
                        .addParameter("domain", domain)
                        .addParameter("timestamp", timestamp)
                        .addToBatch()
        );
        query.executeBatch();
        return true;
    }

    public static Map<String, List<Result>> getCurrentResultsByServer(Connection connection) {
        HashMap<String, List<Result>> resultsByServer = new HashMap<>();
        List<String> tests = TestUtils.getAllTestNames();
        Table table = connection.createQuery("select test,servers.domain,success from current_tests" +
                " inner join servers on servers.domain = current_tests.domain" +
                " where test in (:tests)")
                .addParameter("tests", tests)
                .executeAndFetchTable();
        table.rows().forEach(
                row -> {
                    String domain = row.getString("domain");
                    ComplianceTest test = TestUtils.getTestFrom(row.getString("test"));
                    boolean success = row.getInteger("success") == 1;
                    resultsByServer.putIfAbsent(domain, new ArrayList<>());
                    resultsByServer.get(domain).add(new Result(test, success));
                }
        );
        return resultsByServer;
    }


    public static Map<String, HashMap<String, Boolean>> getCurrentResultsHashMapByServer(Connection connection) {
        HashMap<String, HashMap<String, Boolean>> resultsByServer = new HashMap<>();
        List<String> tests = TestUtils.getTestNames();
        Table table = connection.createQuery("select test,servers.domain,success from current_tests" +
                " inner join servers on servers.domain = current_tests.domain" +
                " where test in (:tests) and listed = 1")
                .addParameter("tests", tests)
                .executeAndFetchTable();
        table.rows().forEach(
                row -> {
                    String domain = row.getString("domain");
                    String test = row.getString("test");
                    boolean success = row.getBoolean("success");
                    resultsByServer.putIfAbsent(domain, new HashMap<>());
                    resultsByServer.get(domain).put(test, success);
                }
        );
        return resultsByServer;
    }

    public static Map<String, HashMap<String, Boolean>> getCurrentResultsByTest(Connection connection) {
        HashMap<String, HashMap<String, Boolean>> resultsByTests = new HashMap<>();
        List<String> tests = TestUtils.getAllTestNames();
        Table table = connection.createQuery("select test,servers.domain,success from current_tests" +
                " inner join servers on servers.domain = current_tests.domain" +
                " where test in (:tests) and listed=1 order by test")
                .addParameter("tests", tests)
                .executeAndFetchTable();
        table.rows().forEach(
                row -> {
                    String domain = row.getString("domain");
                    String test = row.getString("test");
                    boolean success = row.getBoolean("success");
                    resultsByTests.putIfAbsent(test, new HashMap<>());
                    resultsByTests.get(test).put(domain, success);
                }
        );
        return resultsByTests;
    }

    public static Instant getLastRunFor(Connection connection, String domain) {
        Instant lastRun = connection.createQuery("select max(timestamp) from current_tests where domain=:domain")
                .addParameter("domain", domain)
                .executeScalar(Instant.class);
        return lastRun;
    }

    public static List<Result> getHistoricalResultsFor(Connection connection, String domain, int iteration) {
        Table table = connection.createQuery("select test,success from periodic_tests " +
                "where domain=:domain and iteration_number = :iteration")
                .addParameter("domain", domain)
                .addParameter("iteration", iteration)
                .executeAndFetchTable();
        ArrayList<Result> r = table.rows().stream()
                .map(row -> new Result(
                        TestUtils.getTestFrom(row.getString("test")),
                        (row.getInteger("success") == 1)))
                .collect(Collectors.toCollection(ArrayList::new));
        return r;
    }

}
