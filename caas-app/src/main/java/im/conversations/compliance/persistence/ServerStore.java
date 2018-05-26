package im.conversations.compliance.persistence;

import im.conversations.compliance.pojo.Configuration;
import im.conversations.compliance.pojo.Credential;
import im.conversations.compliance.pojo.Server;
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
                con.createQuery("create table if not exists credentials(domain text,jid text primary key,password text)").executeUpdate();
                con.createQuery("create table if not exists servers(domain text primary key,listed integer, software_name text, software_version text)").executeUpdate();
                con.createQuery("create index if not exists credentials_index on credentials(domain)").executeUpdate();
                con.createQuery("create index if not exists servers_index on credentials(domain)").executeUpdate();
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

    public boolean addOrUpdateServer(Server server) {
        synchronized (this.database) {
            try (Connection con = this.database.open()) {
                String query = "insert or replace into servers(domain,listed,software_name,software_version) values(:domain,:listed,:softwareName,:softwareVersion)";
                con.createQuery(query).bind(server).executeUpdate();
            } catch (Exception ex) {
                ex.printStackTrace();
                return false;
            }
        }
        return true;
    }

    public Server getServer(String domainName) {
        synchronized (this.database) {
            try (Connection con = this.database.open()) {
                String query = "select domain,software_name,software_version,listed from servers where domain=:domain";
                return con.createQuery(query)
                        .addColumnMapping("software_name", "softwareName")
                        .addColumnMapping("software_version", "softwareVersion")
                        .addParameter("domain", domainName)
                        .executeAndFetchFirst(Server.class);
            } catch (Exception ex) {
                ex.printStackTrace();
                return null;
            }
        }
    }

    public boolean removeDomain(Server server) {
        synchronized (this.database) {
            try (Connection con = this.database.open()) {
                String query = "delete from servers where domain=:domain";
                con.createQuery(query).bind(server).executeUpdate();
            } catch (Exception ex) {
                ex.printStackTrace();
                return false;
            }
            return true;
        }
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
