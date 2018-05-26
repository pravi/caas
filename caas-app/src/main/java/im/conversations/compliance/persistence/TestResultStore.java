package im.conversations.compliance.persistence;

import im.conversations.compliance.pojo.Configuration;
import im.conversations.compliance.pojo.Iteration;
import im.conversations.compliance.xmpp.PeriodicTestRunner;
import im.conversations.compliance.xmpp.Result;
import org.sql2o.Connection;
import org.sql2o.Query;
import org.sql2o.Sql2o;

import java.time.Instant;
import java.util.List;
import java.util.Locale;

public class TestResultStore {
    public static final TestResultStore INSTANCE = new TestResultStore();
    private final Sql2o database;

    private TestResultStore() {
        final String dbFilename = Configuration.getInstance().getStoragePath() + getClass().getSimpleName().toLowerCase(Locale.US) + ".db";
        this.database = new Sql2o("jdbc:sqlite:" + dbFilename, null, null);
        synchronized (this.database) {
            try (Connection con = this.database.open()) {

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
    }

    public Iteration getLastIteration() {
        Iteration lastIteration = null;
        synchronized (this.database) {
            try (Connection con = this.database.open()) {
                String query = "select max(iteration_number) from periodic_test_iterations";
                int iterationNumber = con.createQuery(query).executeScalar(Integer.class);
                query = "select iteration_number,begin_time,end_time from periodic_test_iterations " +
                        "where iteration_number = " + iterationNumber;
                List<Iteration> iterations = con.createQuery(query)
                        .addColumnMapping("iteration_number", "iterationNumber")
                        .addColumnMapping("begin_time", "begin")
                        .addColumnMapping("end_time", "end")
                        .executeAndFetch(Iteration.class);
                lastIteration = iterations.get(0);
            } catch (Exception ex) {
                System.out.println("No previous iterations run");
            }
        }
        return lastIteration;
    }

    public boolean putOneOffTestResults(String domain, List<Result> results) {
        return addToCurrentResults(domain, results);
    }

    public boolean putPeriodicTestResults(List<PeriodicTestRunner.ResultDomainPair> rdpList, Iteration iteration) {
        //Add to periodic results
        addToPeriodicResults(rdpList, iteration);

        //Add to current results
        rdpList.forEach(rdp -> addToCurrentResults(rdp.getDomain(), rdp.getResults()));
        return true;
    }

    private boolean addToCurrentResults(String domain, List<Result> results) {
        Instant timestamp = Instant.now();
        synchronized (this.database) {
            try (Connection con = this.database.beginTransaction(java.sql.Connection.TRANSACTION_SERIALIZABLE)) {
                Query query = con.createQuery("insert or replace into current_tests(domain,test,success,timestamp) values(:domain,:test,:success,:timestamp)");
                results.forEach(result -> {
                    query
                            .addParameter("test", result.getTest().short_name())
                            .addParameter("success", result.isSuccess())
                            .addParameter("domain", domain)
                            .addParameter("timestamp", timestamp)
                            .addToBatch();
                });
                query.executeBatch();
                con.commit();
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
                    rdp.getResults().forEach(result -> {
                        resultInsertQuery
                                .addParameter("test", result.getTest().short_name())
                                .addParameter("success", result.isSuccess())
                                .addParameter("domain", domain)
                                .addParameter("iteration", iteration.getIterationNumber())
                                .addToBatch();
                    });
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
            }
        }
        return false;
    }
}
