package im.conversations.compliance;

import im.conversations.compliance.annotations.ComplianceTest;
import im.conversations.compliance.persistence.InternalDBOperations;
import im.conversations.compliance.pojo.HistoricalSnapshot;
import im.conversations.compliance.pojo.Result;
import im.conversations.compliance.pojo.ResultDomainPair;
import im.conversations.compliance.pojo.Server;
import im.conversations.compliance.xmpp.utils.TestUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.sql2o.Connection;
import org.sql2o.Sql2o;

import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResultsByTestsTest {
    private static final String JDBC_URL = "jdbc:sqlite::memory:";
    private Sql2o database;
    private Connection connection;

    @Before()
    public void init() {
        database = new Sql2o(JDBC_URL, null, null);
        connection = database.open();
        InternalDBOperations.init(connection);
    }

    @Test
    public void whenNoResults() {
        Map<String, Boolean> dummy = InternalDBOperations.getCurrentResultsForTest(connection, "dummy");
        Assert.assertTrue(InternalDBOperations.getCurrentResultsByServer(connection).isEmpty());
        Assert.assertTrue(dummy.isEmpty());
    }

    @Test
    public void checkHistoricalSnapshots() {
        Server sererListed1 = new Server("serverlisted1.tld", true);
        Server sererListed2 = new Server("serverlisted2.tld", true);
        Server serverUnlisted = new Server("serverunlisted.tld", false);
        List<String> servers = Arrays.asList(
                sererListed1.getDomain(),
                sererListed2.getDomain(),
                serverUnlisted.getDomain()
        );
        InternalDBOperations.addServer(connection, sererListed1);
        InternalDBOperations.addServer(connection, sererListed2);
        InternalDBOperations.addServer(connection, serverUnlisted);
        ComplianceTest test = TestUtils.getComplianceTests().get(0);
        ComplianceTest informationalTest = TestUtils.getAllComplianceTests()
                .stream()
                .filter(ComplianceTest::informational)
                .findFirst()
                .get();
        Result passingTest = new Result(test, true);
        Result failingTest = new Result(test, false);
        Result passingInformationalTest = new Result(informationalTest, true);
        Result failingInformationalTest = new Result(informationalTest, false);
        Instant timestamp = Instant.now();
        ResultDomainPair server1AllFailing = new ResultDomainPair(sererListed1.getDomain(), Arrays.asList(failingInformationalTest, failingTest));
        ResultDomainPair server2AllFailing = new ResultDomainPair(sererListed2.getDomain(), Arrays.asList(failingInformationalTest, failingTest));

        ResultDomainPair server1AllPassing = new ResultDomainPair(sererListed1.getDomain(), Arrays.asList(passingInformationalTest, passingTest));
        ResultDomainPair server2InfoFailing = new ResultDomainPair(sererListed2.getDomain(), Arrays.asList(failingInformationalTest, passingTest));
        ResultDomainPair unlistedServerAllPassing = new ResultDomainPair(serverUnlisted.getDomain(), Arrays.asList(passingInformationalTest, passingTest));

        InternalDBOperations.addPeriodicResults(connection, Arrays.asList(server1AllFailing, server2AllFailing, unlistedServerAllPassing), timestamp, timestamp);
        InternalDBOperations.addPeriodicResults(connection, Arrays.asList(server1AllFailing, server2InfoFailing, unlistedServerAllPassing), timestamp, timestamp);
        InternalDBOperations.addPeriodicResults(connection, Arrays.asList(server1AllPassing, server2InfoFailing, unlistedServerAllPassing), timestamp, timestamp);
        InternalDBOperations.addPeriodicResults(connection, Arrays.asList(server1AllPassing, server2InfoFailing, unlistedServerAllPassing), timestamp, timestamp);
        InternalDBOperations.addPeriodicResults(connection, Arrays.asList(server1AllPassing, server2InfoFailing, unlistedServerAllPassing), timestamp, timestamp);
        HashMap<String, List<HistoricalSnapshot>> readHistoricalSnapshots =
                InternalDBOperations.getHistoricalSnapshotGroupedByTest(
                        connection,
                        Arrays.asList(informationalTest.short_name(), test.short_name()),
                        Arrays.asList(sererListed1.getDomain(), sererListed2.getDomain(), serverUnlisted.getDomain())
                );
        HistoricalSnapshot.Change iZeroChange = new HistoricalSnapshot.Change();
        HistoricalSnapshot.Change iTwoChange = new HistoricalSnapshot.Change();
        HistoricalSnapshot.Change tZeroChange = new HistoricalSnapshot.Change();
        HistoricalSnapshot.Change tOneChange = new HistoricalSnapshot.Change();
        HistoricalSnapshot.Change tTwoChange = new HistoricalSnapshot.Change();

        tZeroChange.getFail().addAll(Arrays.asList(sererListed1.getDomain(), sererListed2.getDomain()));
        tZeroChange.getPass().add(serverUnlisted.getDomain());
        tOneChange.getPass().add(sererListed2.getDomain());
        tTwoChange.getPass().add(sererListed1.getDomain());

        iZeroChange.getFail().addAll(Arrays.asList(sererListed1.getDomain(), sererListed2.getDomain()));
        iZeroChange.getPass().add(serverUnlisted.getDomain());
        iTwoChange.getPass().add(sererListed1.getDomain());

        List<HistoricalSnapshot> infoSnapshots =
                Arrays.asList(
                        new HistoricalSnapshot(
                                0,
                                timestamp.toString(),
                                1,
                                3,
                                iZeroChange
                        ),
                        new HistoricalSnapshot(
                                2,
                                timestamp.toString(),
                                2,
                                3,
                                iTwoChange
                        ),
                        new HistoricalSnapshot(
                                4,
                                timestamp.toString(),
                                2,
                                3,
                                new HistoricalSnapshot.Change()
                        )
                );

        List<HistoricalSnapshot> testSnapshots =
                Arrays.asList(
                        new HistoricalSnapshot(
                                0,
                                timestamp.toString(),
                                1,
                                3,
                                tZeroChange
                        ),
                        new HistoricalSnapshot(
                                1,
                                timestamp.toString(),
                                2,
                                3,
                                tOneChange
                        ),
                        new HistoricalSnapshot(
                                2,
                                timestamp.toString(),
                                3,
                                3,
                                tTwoChange
                        ),
                        new HistoricalSnapshot(
                                4,
                                timestamp.toString(),
                                3,
                                3,
                                new HistoricalSnapshot.Change()
                        )
                );
        HashMap<String, List<HistoricalSnapshot>> historicalSnapshots = new HashMap<>();
        historicalSnapshots.put(test.short_name(), testSnapshots);
        historicalSnapshots.put(informationalTest.short_name(), infoSnapshots);

        Assert.assertEquals(historicalSnapshots, readHistoricalSnapshots);
    }

    @Test
    public void checkCurrentResults() {
        List<String> domains = Arrays.asList(
                "domain1.tld",
                "domain2.tld",
                "domain3.tld"
        );
        ComplianceTest test = TestUtils.getComplianceTests().get(0);
        Instant timestamp = Instant.now();

        for (String domain : domains) {
            InternalDBOperations.addServer(connection, new Server(domain, true));
        }
        List<Result> pass = Arrays.asList(
                new Result(test, true)
        );
        List<Result> fail = Arrays.asList(
                new Result(test, false)
        );
        Instant now = Instant.now();
        HashMap<String, Boolean> resultTest1 = new HashMap<>();
        resultTest1.put(domains.get(0), true);
        for (int i = 0; i < 3; i++) {
            String domain = domains.get(i);
            InternalDBOperations.addCurrentResults(connection, domain, (i % 2 == 0) ? pass : fail, now);
        }
        HashMap<String, Boolean> results = new HashMap<>();
        for (int i = 0; i < 3; i++) {
            results.put(domains.get(i), (i % 2 == 0));
        }
        Map<String, Boolean> readResults = InternalDBOperations.getCurrentResultsForTest(connection, test.short_name());
        Assert.assertEquals(results, readResults);
    }

}
