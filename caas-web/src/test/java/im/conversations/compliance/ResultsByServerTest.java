package im.conversations.compliance;

import im.conversations.compliance.annotations.ComplianceTest;
import im.conversations.compliance.persistence.InternalDBOperations;
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
import java.util.*;
import java.util.stream.Collectors;

public class ResultsByServerTest {
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
    public void checkCompliantServersApiTest() {
        String failingComplianceTestDomain = "domain0.tld";
        String failingInformationalTestDomain = "domain1.tld"; // Fails all informational tests
        String failingRegistrationTestDomain = "domain2.tld";
        String passingAllNecessaryTestsDomain = "domain3.tld"; // Fails all informational tests other than registration test
        String passingAllTestsDomain = "domain4.tld";
        String passingAllTestsUnlistedDomain = "domain5.tld"; // Server is unlisted
        InternalDBOperations.addServer(connection, new Server(failingComplianceTestDomain, true));
        InternalDBOperations.addServer(connection, new Server(failingInformationalTestDomain, true));
        InternalDBOperations.addServer(connection, new Server(failingRegistrationTestDomain, true));
        InternalDBOperations.addServer(connection, new Server(passingAllNecessaryTestsDomain, true));
        InternalDBOperations.addServer(connection, new Server(passingAllTestsDomain, true));
        InternalDBOperations.addServer(connection, new Server(passingAllTestsUnlistedDomain, false));
        List<Result> failingComplianceResults = new ArrayList<>();
        List<Result> failingInfoResults = new ArrayList<>();
        List<Result> failingRegResults = new ArrayList<>();
        List<Result> passingAllNecessaryResults = new ArrayList<>();
        List<Result> passingAllResults = new ArrayList<>();
        List<Result> passingAllUnlistedResults = new ArrayList<>();
        for (ComplianceTest complianceTest : TestUtils.getAllComplianceTests()) {
            Result pass = new Result(complianceTest, true);
            Result fail = new Result(complianceTest, false);
            passingAllResults.add(pass);
            passingAllUnlistedResults.add(pass);
            if (!complianceTest.informational()) {
                failingComplianceResults.add(fail);
                failingInfoResults.add(pass);
                failingRegResults.add(pass);
                passingAllNecessaryResults.add(pass);
            } else if (complianceTest.short_name().equals("xep0077")) {
                failingComplianceResults.add(pass);
                failingInfoResults.add(fail);
                failingRegResults.add(fail);
                passingAllNecessaryResults.add(pass);
            } else {
                failingComplianceResults.add(pass);
                failingInfoResults.add(fail);
                failingRegResults.add(pass);
                passingAllNecessaryResults.add(fail);
            }
        }
        InternalDBOperations.addCurrentResults(connection,failingComplianceTestDomain, failingComplianceResults, Instant.now());
        InternalDBOperations.addCurrentResults(connection,failingInformationalTestDomain, failingInfoResults, Instant.now());
        InternalDBOperations.addCurrentResults(connection,failingRegistrationTestDomain, failingRegResults, Instant.now());
        InternalDBOperations.addCurrentResults(connection,passingAllNecessaryTestsDomain, passingAllNecessaryResults, Instant.now());
        InternalDBOperations.addCurrentResults(connection,passingAllTestsDomain, passingAllResults, Instant.now());
        InternalDBOperations.addCurrentResults(connection,passingAllTestsUnlistedDomain, passingAllUnlistedResults, Instant.now());

        Assert.assertEquals(Arrays.asList(passingAllNecessaryTestsDomain, passingAllTestsDomain), InternalDBOperations.getCompliantServers(connection));
    }

    @Test
    public void checkHistoricalTable() {
        int i = 0;
        List<Result> results0 = new ArrayList<>();
        List<Result> results1 = new ArrayList<>();
        HashMap<String, Boolean> resultMap0 = new HashMap<>();
        HashMap<String, Boolean> resultMap1 = new HashMap<>();
        for (ComplianceTest test : TestUtils.getComplianceTests()) {
            i++;
            Result result0 = new Result(test, (i % 2 == 0));
            Result result1 = new Result(test, (i % 2 != 0));
            results0.add(result0);
            results1.add(result1);
            resultMap0.put(test.short_name(), result0.isSuccess());
            resultMap1.put(test.short_name(), result1.isSuccess());
        }
        String domain0 = "domain0.com";
        String domain1 = "domain1.com";
        String domain2 = "domain2.com";

        List<ResultDomainPair> rdpList0 = Arrays.asList(
                new ResultDomainPair(domain0, results0),
                new ResultDomainPair(domain1, results1),
                new ResultDomainPair(domain2, results0)
        );
        HashMap expectedValue0 = new HashMap() {
            {
                put(domain0, resultMap0);
                put(domain1, resultMap1);
            }
        };
        List<ResultDomainPair> rdpList1 = Arrays.asList(
                new ResultDomainPair(domain0, results1),
                new ResultDomainPair(domain1, results0),
                new ResultDomainPair(domain2, results0)
        );
        HashMap expectedValue1 = new HashMap() {
            {
                put(domain0, resultMap1);
                put(domain1, resultMap0);
            }
        };

        Instant now = Instant.now();
        InternalDBOperations.addServer(connection, new Server(domain0, true));
        InternalDBOperations.addServer(connection, new Server(domain1, true));
        InternalDBOperations.addServer(connection, new Server(domain2, false));
        InternalDBOperations.addPeriodicResults(connection, rdpList0, Instant.now(), Instant.now());
        InternalDBOperations.addPeriodicResults(connection, rdpList1, Instant.now(), Instant.now());
        Map<String, HashMap<String, Boolean>> readValue0 = InternalDBOperations.getHistoricalTableFor(
                connection, 0
        );
        Map<String, HashMap<String, Boolean>> readValue1 = InternalDBOperations.getHistoricalTableFor(
                connection, 1
        );
        Assert.assertEquals(expectedValue0, readValue0);
        Assert.assertEquals(expectedValue1, readValue1);
    }

    @Test
    public void checkPeriodicResults() {
        int i = 0;
        List<Result> results1 = new ArrayList<>();
        List<Result> results2 = new ArrayList<>();
        List<Result> results3 = new ArrayList<>();
        HashMap<String, Boolean> resultMap1 = new HashMap<>();
        HashMap<String, Boolean> resultMap2 = new HashMap<>();
        HashMap<String, Boolean> resultMap3 = new HashMap<>();
        Instant begin = Instant.now();
        List<ResultDomainPair> resultDomainPairs = new ArrayList<>();
        for (ComplianceTest test : TestUtils.getAllComplianceTests()) {
            i++;
            Result result1 = new Result(test, (i % 2 != 0));
            Result result2 = new Result(test, (i % 2 == 0));
            Result result3 = new Result(test, (i % 3 != 0));
            if (!test.informational()) {
                resultMap1.put(test.short_name(), (i % 2 != 0));
                resultMap2.put(test.short_name(), (i % 2 == 0));
                resultMap3.put(test.short_name(), (i % 3 != 0));
            }
            results1.add(result1);
            results2.add(result2);
            results3.add(result3);
        }
        String domain1 = "periodictestdomain1.dshd";
        String domain2 = "periodictestdomain2.dshd";
        String domain3 = "periodictestdomain3.dshd";

        InternalDBOperations.addServer(connection, new Server(domain1, true));
        InternalDBOperations.addServer(connection, new Server(domain2, false));
        InternalDBOperations.addServer(connection, new Server(domain3, true));

        resultDomainPairs.add(new ResultDomainPair(domain1, results1));
        resultDomainPairs.add(new ResultDomainPair(domain2, results2));
        resultDomainPairs.add(new ResultDomainPair(domain3, results3));
        Instant end = Instant.now();
        InternalDBOperations.addPeriodicResults(connection, resultDomainPairs, begin, end);
        List<Result> readHistoricalResults1 = InternalDBOperations.getHistoricalResultsFor(connection, domain1, 0);
        List<Result> readHistoricalResults2 = InternalDBOperations.getHistoricalResultsFor(connection, domain2, 0);
        List<Result> readHistoricalResults3 = InternalDBOperations.getHistoricalResultsFor(connection, domain3, 0);
        Map<String, HashMap<String, Boolean>> currentResultsByServer = InternalDBOperations.getCurrentResultsByServer(connection);
        HashMap<String, Boolean> readResultMap1 = currentResultsByServer.get(domain1);
        HashMap<String, Boolean> readResultMap2 = currentResultsByServer.get(domain2);
        HashMap<String, Boolean> readResultMap3 = currentResultsByServer.get(domain3);
        List<Result> readCurrentResults1 = InternalDBOperations.getCurrentResultsForServer(connection, domain1);
        List<Result> readCurrentResults2 = InternalDBOperations.getCurrentResultsForServer(connection, domain2);
        List<Result> readCurrentResults3 = InternalDBOperations.getCurrentResultsForServer(connection, domain3);
        Assert.assertEquals(results1, readHistoricalResults1);
        Assert.assertEquals(results1, readCurrentResults1);
        Assert.assertEquals(results2, readHistoricalResults2);
        Assert.assertEquals(results2, readCurrentResults2);
        Assert.assertEquals(results3, readHistoricalResults3);
        Assert.assertEquals(results3, readCurrentResults3);
        Assert.assertEquals(resultMap1, readResultMap1);
        //Server wasn't public, so result should not be in the public servers' result
        Assert.assertNull(readResultMap2);
        Assert.assertEquals(resultMap3, readResultMap3);
    }

    @Test
    public void checkCurrentResults() {
        int i = 0;
        List<Result> results = new ArrayList<>();
        for (ComplianceTest test : TestUtils.getAllComplianceTests()) {
            i++;
            Result result = new Result(test, (i % 2 == 0));
            results.add(result);
        }
        Instant now = Instant.now();
        String domain = "test";
        InternalDBOperations.addServer(connection, new Server(domain, true));
        InternalDBOperations.addCurrentResults(connection, domain, results, now);
        List<Result> readResults = InternalDBOperations.getCurrentResultsForServer(connection, domain);
        Assert.assertEquals(results, readResults);
    }

    @Test
    public void checkHistoricalSnapshots() {
        ArrayList<ResultDomainPair> rdpList;
        List<ComplianceTest> tests = TestUtils.getComplianceTests()
                .stream()
                .limit(3)
                .collect(Collectors.toList());
        List<String> testNames = tests.stream()
                .map(ComplianceTest::short_name)
                .collect(Collectors.toList());
        String domain = "historicalsnapshotservertest.tld";
        InternalDBOperations.addServer(connection, new Server(domain, true));
        ArrayList<Result> results1 = new ArrayList<>();
        ArrayList<Result> results2 = new ArrayList<>();
        ArrayList<Result> results3 = new ArrayList<>();
        ArrayList<Result> results4 = new ArrayList<>();
        ArrayList<Result> results5 = new ArrayList<>();

        for (int i = 0; i < 3; i++) {
            results1.add(new Result(tests.get(i), true));
            results2.add(new Result(tests.get(i), true));
            results3.add(new Result(tests.get(i), (i % 2 == 0)));
            results5.add(new Result(tests.get(i), (i % 2 == 0)));
        }
        Instant timestamp = Instant.now();
        HistoricalSnapshot.Change change0 = new HistoricalSnapshot.Change();
        change0.getPass().add(testNames.get(0));
        change0.getPass().add(testNames.get(1));
        change0.getPass().add(testNames.get(2));
        //Check if first point is present
        HistoricalSnapshot historicalSnapshot0 = new HistoricalSnapshot(
                0,
                timestamp.toString(),
                3,
                3,
                change0
        );
        HistoricalSnapshot.Change change1 = new HistoricalSnapshot.Change();
        change1.getFail().add(testNames.get(1));
        //Check if point for which there was a change is present
        HistoricalSnapshot historicalSnapshot1 = new HistoricalSnapshot(
                2,
                timestamp.toString(),
                2,
                3,
                change1
        );
        //Check if the latest point is present
        HistoricalSnapshot historicalSnapshot2 = new HistoricalSnapshot(
                4,
                timestamp.toString(),
                2,
                3,
                new HistoricalSnapshot.Change()
        );
        List<HistoricalSnapshot> historicalSnapshots = Arrays.asList(
                historicalSnapshot0,
                historicalSnapshot1,
                historicalSnapshot2
        );
        rdpList = new ArrayList<>();
        rdpList.add(new ResultDomainPair(domain, results1));
        InternalDBOperations.addPeriodicResults(connection, rdpList, timestamp, timestamp);
        rdpList = new ArrayList<>();
        rdpList.add(new ResultDomainPair(domain, results2));
        InternalDBOperations.addPeriodicResults(connection, rdpList, timestamp, timestamp);
        rdpList = new ArrayList<>();
        rdpList.add(new ResultDomainPair(domain, results3));
        InternalDBOperations.addPeriodicResults(connection, rdpList, timestamp, timestamp);
        rdpList = new ArrayList<>();
        rdpList.add(new ResultDomainPair(domain, results4));
        InternalDBOperations.addPeriodicResults(connection, rdpList, timestamp, timestamp);
        rdpList = new ArrayList<>();
        rdpList.add(new ResultDomainPair(domain, results5));
        InternalDBOperations.addPeriodicResults(connection, rdpList, timestamp, timestamp);
        List<HistoricalSnapshot> readBackHistoricalSnapshots =
                InternalDBOperations
                        .getHistoricalSnapshotsGroupedByServer(
                                connection,
                                testNames,
                                Collections.singletonList(domain)
                        )
                        .get(domain);
        Assert.assertEquals(historicalSnapshots, readBackHistoricalSnapshots);
    }

    @Test
    public void whenNoResults() {
        List<Result> dummy = InternalDBOperations.getCurrentResultsForServer(connection, "dummy");
        Assert.assertTrue(InternalDBOperations.getCurrentResultsByServer(connection).isEmpty());
        Assert.assertTrue(InternalDBOperations.getHistoricalTableFor(connection, 0).isEmpty());
        Assert.assertTrue(InternalDBOperations.getHistoricalResultsFor(connection, "dummy", 0).isEmpty());
        Assert.assertTrue(dummy.isEmpty());
    }
}
