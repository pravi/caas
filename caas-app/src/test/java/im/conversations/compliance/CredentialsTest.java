package im.conversations.compliance;

import im.conversations.compliance.persistence.InternalDBOperations;
import im.conversations.compliance.pojo.Credential;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.sql2o.Connection;
import org.sql2o.Sql2o;
import rocks.xmpp.addr.Jid;

import java.util.Arrays;
import java.util.List;

public class CredentialsTest {
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
    public void whenNoCredentials() {
        boolean noCreds = InternalDBOperations.getCredentials(connection).isEmpty();
        Assert.assertTrue(noCreds);
    }

    @Test
    public void getCredentials() {
        String jid1 = "abc@jkl.m";
        String jid2 = "def@mno.p";
        Credential credential1 = new Credential(jid1, "password");
        Credential credential2 = new Credential(jid2, "password");
        List<Credential> credentialList = Arrays.asList(credential1, credential2);
        InternalDBOperations.addCredential(connection, credential1);
        InternalDBOperations.addCredential(connection, credential2);
        Assert.assertEquals(credential1, InternalDBOperations.getCredentialFor(connection, Jid.of(jid1).getDomain()));
        Assert.assertEquals(credential2, InternalDBOperations.getCredentialFor(connection, Jid.of(jid2).getDomain()));
        Assert.assertEquals(credentialList, InternalDBOperations.getCredentials(connection));
    }

    @Test
    public void removeCredentials() {
        Credential credential = new Credential("abc@d.tld", "password");
        InternalDBOperations.addCredential(connection, credential);
        //Check that credential was added
        Assert.assertEquals(InternalDBOperations.getCredentials(connection), Arrays.asList(credential));
        InternalDBOperations.removeCredential(connection, credential);
        boolean noCreds = InternalDBOperations.getCredentials(connection).isEmpty();
        //Check that credential was removed
        Assert.assertTrue(noCreds);
    }

}
