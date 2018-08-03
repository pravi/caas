package im.conversations.compliance;

import im.conversations.compliance.persistence.InternalDBOperations;
import im.conversations.compliance.pojo.Iteration;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.sql2o.Connection;
import org.sql2o.Sql2o;

import java.time.Instant;
import java.util.ArrayList;

public class IterationsTest {
    private static final String JDBC_URL = "jdbc:sqlite::memory:";
    private Sql2o database;
    private Connection connection;

    @Before()
    public void init() {
        database = new Sql2o(JDBC_URL, null, null);
        connection = database.open();
        InternalDBOperations.init(connection);
    }

    @Test()
    public void whenNoIterations() {
        Assert.assertNull(InternalDBOperations.getLatestIteration(connection));
        Assert.assertNull(InternalDBOperations.getIteration(connection, 0));
    }

    @Test()
    public void checkLatestIteration() {
        Instant begin = Instant.MIN;
        Instant end = Instant.MAX;
        Iteration iteration0 = new Iteration(0, begin, end);
        Iteration iteration1 = new Iteration(1, begin, end);
        InternalDBOperations.addPeriodicResults(connection, new ArrayList<>(), begin, end);
        InternalDBOperations.addPeriodicResults(connection, new ArrayList<>(), begin, end);
        Assert.assertNotEquals(iteration0, InternalDBOperations.getLatestIteration(connection));
        Assert.assertEquals(iteration1, InternalDBOperations.getLatestIteration(connection));
    }

    @Test()
    public void checkGetIteration() {
        Instant begin = Instant.MIN;
        Instant end = Instant.MAX;
        Iteration iteration = new Iteration(0, begin, end);
        InternalDBOperations.addPeriodicResults(connection, new ArrayList<>(), begin, end);
        Assert.assertEquals(iteration,
                InternalDBOperations.getIteration(connection, 0)
        );
        Assert.assertNull(
                InternalDBOperations.getIteration(connection, 1)
        );
    }
}
