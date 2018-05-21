package im.conversations.compliance.persistence;

import im.conversations.compliance.annotations.ComplianceTest;
import im.conversations.compliance.pojo.Configuration;
import im.conversations.compliance.pojo.Credential;
import im.conversations.compliance.sql2o.ComplianceTestConverter;
import im.conversations.compliance.sql2o.JidConverter;
import org.sql2o.Connection;
import org.sql2o.Sql2o;
import org.sql2o.converters.Converter;
import org.sql2o.quirks.NoQuirks;
import org.sql2o.quirks.Quirks;
import rocks.xmpp.addr.Jid;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class ServerStore {
    public static final ServerStore INSTANCE = new ServerStore();
    private final Sql2o database;
    private List<Credential> credentials;

    private ServerStore() {
        final String dbFilename = Configuration.getInstance().getStoragePath() + getClass().getSimpleName().toLowerCase(Locale.US) + ".db";
        this.database = new Sql2o("jdbc:sqlite:" + dbFilename, null, null, getQuirks());
        synchronized (this.database) {
            try (Connection con = this.database.open()) {
                con.createQuery("create table if not exists credentials(domain text,jid text,password text)").executeUpdate();
                con.createQuery("create table if not exists domains(domain text,first_added text,listed integer)").executeUpdate();
                con.createQuery("create index if not exists domain_index on credentials(domain)").executeUpdate();
            }
        }
    }


    public boolean addCredential(Credential credential) {
        synchronized (this.database) {
            try (Connection con = this.database.open()) {
                String query = "insert into credentials(domain,jid,password) values(:domain,:jid,:password)";
                con.createQuery(query).bind(credential).executeUpdate();
                fetchCredentials();
            } catch (Exception ex) {
                ex.printStackTrace();
                return false;
            }
        }
        return true;
    }

    public List<Credential> getCredentials() {
        if (credentials == null) {
            fetchCredentials();
        }
        return Collections.unmodifiableList(credentials);
    }

    public boolean removeCredential(Credential oldCredential) {
        synchronized (this.database) {
            try (Connection con = this.database.open()) {
                String query = "delete from credentials where jid=:jid and password=:password";
                con.createQuery(query).bind(oldCredential).executeUpdate();
                fetchCredentials();
            } catch (Exception ex) {
                ex.printStackTrace();
                return false;
            }
            return true;
        }
    }

    private Quirks getQuirks() {
        HashMap<Class, Converter> converters = new HashMap<>();
        final JidConverter jidConverter = new JidConverter();
        final ComplianceTestConverter complianceTestConverter = new ComplianceTestConverter();
        converters.put(Jid.class, jidConverter);
        converters.put(ComplianceTest.class, complianceTestConverter);
        return new NoQuirks(converters);
    }

    private void fetchCredentials() {
        synchronized (this.database) {
            try (Connection con = this.database.open()) {
                String query = "select domain,jid,password from credentials";
                this.credentials = con.createQuery(query).executeAndFetch(Credential.class);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

}
