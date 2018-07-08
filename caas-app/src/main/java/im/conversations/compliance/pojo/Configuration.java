package im.conversations.compliance.pojo;

import im.conversations.compliance.utils.JsonReader;

import java.io.File;
import java.util.Optional;

public class Configuration {

    private static File FILE = new File("config.json");
    private static Configuration INSTANCE;

    private String ip = "127.0.0.1";
    private int port = 4567;
    private int testRunInterval = 24 * 60;
    private int dbConnections = 1;
    private String dbUrl = "jdbc:sqlite:data.db";

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
            INSTANCE = new JsonReader<>(Configuration.class).read(FILE);
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

    public Optional<String> getDBUrl() {
        return Optional.ofNullable(dbUrl);
    }

    public int getDBConnections() {
        return dbConnections;
    }
}
