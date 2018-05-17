package im.conversations.compliance.pojo;

import im.conversations.compliance.utils.JsonReader;

import java.io.File;
import java.util.ArrayList;
import java.util.Optional;

public class Help {
    private static Help INSTANCE;
    private ArrayList<ServerHelp> serverHelps;

    private Help() {
    }

    public static Help getInstance() {
        if (INSTANCE == null) {
            init();
        }
        return INSTANCE;
    }

    private static void init() {
        if (INSTANCE == null) {
            INSTANCE = new JsonReader<Help>(Help.class).read(new File("servers.json"));
        }

    }

    public ArrayList<ServerHelp> getServerHelps() {
        return serverHelps;
    }

    public Optional<ServerHelp> getHelpFor(String server) {
        return serverHelps.
                stream().
                filter(it -> it.getServerName().equals(server)).
                findFirst();
    }
}
