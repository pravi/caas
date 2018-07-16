package im.conversations.compliance;

import im.conversations.compliance.annotations.ComplianceTest;
import im.conversations.compliance.persistence.InternalDBOperations;
import im.conversations.compliance.pojo.Iteration;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OneOffRunnerTest {
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
        List<Result> readHistoricResults1 = InternalDBOperations.getHistoricalResultsFor(connection, domain1, 0);
        List<Result> readHistoricResults2 = InternalDBOperations.getHistoricalResultsFor(connection, domain2, 0);
        List<Result> readHistoricResults3 = InternalDBOperations.getHistoricalResultsFor(connection, domain3, 0);
        Map<String, HashMap<String, Boolean>> currentResultsByServer = InternalDBOperations.getCurrentResultsByServer(connection);
        HashMap<String, Boolean> readResultMap1 = currentResultsByServer.get(domain1);
        HashMap<String, Boolean> readResultMap2 = currentResultsByServer.get(domain2);
        HashMap<String, Boolean> readResultMap3 = currentResultsByServer.get(domain3);
        List<Result> readCurrentResults1 = InternalDBOperations.getCurrentResultsForServer(connection, domain1);
        List<Result> readCurrentResults2 = InternalDBOperations.getCurrentResultsForServer(connection, domain2);
        List<Result> readCurrentResults3 = InternalDBOperations.getCurrentResultsForServer(connection, domain3);
        Assert.assertEquals(results1, readHistoricResults1);
        Assert.assertEquals(results1, readCurrentResults1);
        Assert.assertEquals(results2, readHistoricResults2);
        Assert.assertEquals(results2, readCurrentResults2);
        Assert.assertEquals(results3, readHistoricResults3);
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

}
