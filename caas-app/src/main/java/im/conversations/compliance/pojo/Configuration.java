package im.conversations.compliance.pojo;

import im.conversations.compliance.utils.JsonReader;

import java.io.File;

public class Configuration {

    private static File FILE = new File("config.json");
    private static Configuration INSTANCE;

    private String ip = "127.0.0.1";
    private int port = 4567;
    private int testRunInterval = 24 * 60;
    private String storagePath = "." + File.separator;

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
            INSTANCE = new JsonReader<Configuration>(Configuration.class).read(FILE);
        }
        return INSTANCE;
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    public String getStoragePath() {
        if (storagePath.endsWith(File.separator)) {
            return storagePath;
        } else {
            return storagePath + File.separator;
        }
    }
}
