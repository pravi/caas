package im.conversations.compliance.pojo;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

public class Configuration {

    private static File FILE = new File("config.json");
    private static Configuration INSTANCE;

    private String ip = "127.0.0.1";
    private int port = 4567;
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
            INSTANCE = load();
        }
        return INSTANCE;
    }

    private static Configuration load() {
        final GsonBuilder gsonBuilder = new GsonBuilder();
        final Gson gson = gsonBuilder.create();
        try {
            System.out.println("Reading configuration from " + FILE.getAbsolutePath());
            final Configuration configuration = gson.fromJson(new FileReader(FILE), Configuration.class);
            return configuration;
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Configuration file not found");
        } catch (JsonSyntaxException e) {
            throw new RuntimeException("Invalid syntax in config file");
        }
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
