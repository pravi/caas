package im.conversations.compliance;

import im.conversations.compliance.persistence.InternalDBOperations;
import im.conversations.compliance.pojo.Server;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.sql2o.Connection;
import org.sql2o.Sql2o;

import java.util.Arrays;

public class ServerTest {
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
    public void whenNoServers() {
        boolean noServers = InternalDBOperations.getAllServers(connection).isEmpty();
        boolean noPublicServers = InternalDBOperations.getPublicServers(connection).isEmpty();
        Assert.assertNull(InternalDBOperations.getServer(connection, "test.com"));
        Assert.assertTrue(noServers);
        Assert.assertTrue(noPublicServers);
    }

    @Test
    public void checkGetServers() {
        Server unlistedServer = new Server("unlisted_server.tld", false);
        Server listedServer = new Server("listed_server.tld", true);
        InternalDBOperations.addServer(connection, unlistedServer);
        InternalDBOperations.addServer(connection, listedServer);
        Assert.assertEquals(unlistedServer, InternalDBOperations.getServer(connection, unlistedServer.getDomain()));
        Assert.assertEquals(listedServer, InternalDBOperations.getServer(connection, listedServer.getDomain()));
        Assert.assertEquals(
                Arrays.asList(listedServer, unlistedServer),
                InternalDBOperations.getAllServers(connection)
        );
        Assert.assertEquals(
                Arrays.asList(listedServer),
                InternalDBOperations.getPublicServers(connection)
        );
    }

    @Test
    public void checkUpdateServer() {
        String domain = "test.tld";
        Server oldServer = new Server(domain, false);
        InternalDBOperations.addServer(connection, oldServer);
        Server newServer = new Server(domain, "xmpp-software", "v0.1", false);
        InternalDBOperations.updateServer(connection, newServer);
        Assert.assertNotEquals(oldServer, InternalDBOperations.getServer(connection, oldServer.getDomain()));
        Assert.assertEquals(newServer, InternalDBOperations.getServer(connection, oldServer.getDomain()));
    }

    @Test
    public void checkListingChange() {
        Server unlistedServer = new Server("unlisted_server.tld", false);
        Server listedServer = new Server("listed_server.tld", true);
        InternalDBOperations.addServer(connection, unlistedServer);
        InternalDBOperations.addServer(connection, listedServer);

        Assert.assertFalse(InternalDBOperations.getServer(connection, unlistedServer.getDomain()).isListed());
        InternalDBOperations.setListed(connection, unlistedServer.getDomain(), true);
        Assert.assertTrue(InternalDBOperations.getServer(connection, unlistedServer.getDomain()).isListed());
        InternalDBOperations.setListed(connection, unlistedServer.getDomain(), true);
        Assert.assertTrue(InternalDBOperations.getServer(connection, unlistedServer.getDomain()).isListed());

        Assert.assertTrue(InternalDBOperations.getServer(connection, listedServer.getDomain()).isListed());
        InternalDBOperations.setListed(connection, unlistedServer.getDomain(), false);
        Assert.assertFalse(InternalDBOperations.getServer(connection, unlistedServer.getDomain()).isListed());
    }

    @Test
    public void checkRemoveServer() {
        Server server = new Server("listed_server.tld", true);
        InternalDBOperations.addServer(connection, server);
        Assert.assertEquals(server, InternalDBOperations.getServer(connection, server.getDomain()));
        InternalDBOperations.removeServer(connection, server);
        Assert.assertNull(InternalDBOperations.getServer(connection, server.getDomain()));
    }
}
