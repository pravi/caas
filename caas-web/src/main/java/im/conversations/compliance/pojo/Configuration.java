package im.conversations.compliance.pojo;

import im.conversations.compliance.utils.JsonReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Optional;

public class Configuration {

    private static final Logger LOGGER = LoggerFactory.getLogger(Configuration.class);

    private static File FILE = new File("config.json");
    private static Configuration INSTANCE;

    private String ip = "127.0.0.1";
    private int port = 4567;
    private int testRunInterval = 24 * 60;
    private int dbConnections = 1;
    //Required only if you want to send mails, and have a valid mail config
    private String rootUrl;
    private MailConfig mailConfig;
    private String dbUrl = "jdbc:sqlite:data.db";
    private String dbUsername;
    private String dbPassword;

    private Configuration() {

    }

    public synchronized static void setFilename(String filename) {
        if (INSTANCE != null) {
            throw new IllegalStateException("Unable to set filename after instance has been created");
        }
        Configuration.FILE = new File(filename);
    }

    public synchronized static Configuration getInstance() {
        if (INSTANCE == null) {
            if (FILE.exists()) {
                INSTANCE = new JsonReader<>(Configuration.class).read(FILE);
            } else {
                LOGGER.warn("Configuration file not found. Reverting to default configuration");
                INSTANCE = new Configuration();
            }
        }
        return INSTANCE;
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    public int getTestRunInterval() {
        return testRunInterval;
    }

    public String getDBUrl() {
        return dbUrl;
    }

    public Optional<DbCredentials> getDbCredentials() {
        if (dbUsername != null && dbPassword != null) {
            return Optional.of(new DbCredentials(dbUsername, dbPassword));
        } else {
            return Optional.empty();
        }
    }

    public int getDBConnections() {
        return dbConnections;
    }

    public MailConfig getMailConfig() {
        return mailConfig;
    }

    public String getRootURL() {
        return rootUrl;
    }

    public static class DbCredentials {
        public final String username;
        public final String password;

        public DbCredentials(String username, String password) {
            this.username = username;
            this.password = password;
        }
    }
}
