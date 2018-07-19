package im.conversations.compliance;

import im.conversations.compliance.persistence.InternalDBOperations;
import im.conversations.compliance.pojo.Result;
import im.conversations.compliance.pojo.Server;
import im.conversations.compliance.xmpp.utils.TestUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.sql2o.Connection;
import org.sql2o.Sql2o;

import java.time.Instant;
import java.util.Arrays;

public class LastRunTest {
    private static final String JDBC_URL = "jdbc:sqlite::memory:";
    private Sql2o database;
    private Connection connection;

    @Before()
    public void init() {
        database = new Sql2o(JDBC_URL, null, null);
        connection = database.open();
        InternalDBOperations.init(connection);
    }

    @Test(expected = NullPointerException.class)
    public void whenNoneExists() {
        Instant dummy = InternalDBOperations.getLastRunFor(connection, "dummy");
        Assert.assertNull(dummy);
    }

    @Test
    public void onAddingCurrentResults() {
        Server server1 = new Server("test.com", false);
        Server server2 = new Server("test2.com", false);
        InternalDBOperations.addServer(connection, server1);
        InternalDBOperations.addServer(connection, server2);
        Instant timestamp1 = Instant.MIN;
        Instant timestamp2 = Instant.now();
        InternalDBOperations.addCurrentResults(
                connection,
                server1.getDomain(),
                Arrays.asList(new Result(TestUtils.getComplianceTests().get(0), true)),
                timestamp1
        );
        InternalDBOperations.addCurrentResults(
                connection,
                server2.getDomain(),
                Arrays.asList(new Result(TestUtils.getComplianceTests().get(0), false)),
                timestamp2
        );
        Instant readTimestamp1 = InternalDBOperations.getLastRunFor(connection, server1.getDomain());
        Instant readTimestamp2 = InternalDBOperations.getLastRunFor(connection, server2.getDomain());
        Assert.assertEquals(timestamp1, readTimestamp1);
        Assert.assertEquals(timestamp2, readTimestamp2);
    }

}
