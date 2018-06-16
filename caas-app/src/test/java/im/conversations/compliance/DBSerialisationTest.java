package im.conversations.compliance;

import org.junit.Assert;
import org.junit.Test;
import org.sql2o.Connection;
import org.sql2o.Sql2o;
import rocks.xmpp.addr.Jid;

import java.time.Instant;

public class DBSerialisationTest {

    private static final String JDBC_URL = "jdbc:h2:mem:";
    private static final String CREATE_JID_TABLE = "CREATE table jids (jid varchar)";
    private static final String CREATE_INSTANTS_TABLE = "CREATE table instants (instant varchar)";
    private static final Jid JID_ONE = Jid.of("test@domain.com");
    private Sql2o database = new Sql2o(JDBC_URL, null, null);
    private Instant INSTANT_ONE = Instant.MIN;

    @Test
    public void checkJIDSerialisation() {
        try (Connection con = this.database.open()) {
            con.createQuery(CREATE_JID_TABLE).executeUpdate();
            con.createQuery("insert into jids(jid) values(:jid)")
                    .addParameter("jid", JID_ONE)
                    .executeUpdate();
            Jid readBackJid = con.createQuery("select jid from jids limit 1")
                    .executeScalar(Jid.class);
            Assert.assertEquals(readBackJid, JID_ONE);
        }

    }

    @Test
    public void checkInstantSerialisation() {
        try (Connection con = this.database.open()) {
            con.createQuery(CREATE_INSTANTS_TABLE).executeUpdate();
            con.createQuery("insert into instants(instant) values(:instant)")
                    .addParameter("instant", INSTANT_ONE)
                    .executeUpdate();
            Instant readBackInstant = con.createQuery("select instant from instants limit 1")
                    .executeAndFetchFirst(Instant.class);
            Assert.assertEquals(readBackInstant, INSTANT_ONE);
        }
    }
}
