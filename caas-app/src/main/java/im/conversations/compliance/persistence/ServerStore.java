package im.conversations.compliance.persistence;

import im.conversations.compliance.pojo.Configuration;
import im.conversations.compliance.pojo.Credential;
import org.sql2o.Connection;
import org.sql2o.Sql2o;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class ServerStore {
    public static final ServerStore INSTANCE = new ServerStore();
    private final Sql2o database;
    private List<Credential> credentials;

    private ServerStore() {
        final String dbFilename = Configuration.getInstance().getStoragePath() + getClass().getSimpleName().toLowerCase(Locale.US) + ".db";
        this.database = new Sql2o("jdbc:sqlite:" + dbFilename, null, null);
        synchronized (this.database) {
            try (Connection con = this.database.open()) {
                con.createQuery("create table if not exists credentials(domain text,jid text,password text)").executeUpdate();
            }
        }
    }

    public boolean addCredential(Credential credential) {
        synchronized (this.database) {
            try (Connection con = this.database.open()) {
                String query = "insert into credentials(domain,jid,password) values(:domain,:jid,:password)";
                con.createQuery(query).bind(credential).executeUpdate();
            } catch (Exception ex) {
                ex.printStackTrace();
                return false;
            }
        }
        return true;
    }

    public void fetchCredentials() {
        synchronized (this.database) {
            try (Connection con = this.database.open()) {
                String query = "select domain,jid,password from credentials";
                this.credentials = con.createQuery(query).executeAndFetch(Credential.class);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public List<Credential> getCredentials() {
        if (credentials == null) {
            fetchCredentials();
        }
        return Collections.unmodifiableList(credentials);
    }

}
