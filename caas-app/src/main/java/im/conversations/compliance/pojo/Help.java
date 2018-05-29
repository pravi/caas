package im.conversations.compliance.pojo;

import im.conversations.compliance.utils.JsonReader;

import java.io.File;
import java.util.ArrayList;
import java.util.Optional;

public class Help {
    public static String folderName = "test_help";
    private static Help INSTANCE;
    private ArrayList<ServerHelp> serverHelps;

    private Help() {
    }

    public static Help getInstance(String helpFolder) {
        if (INSTANCE == null) {
            folderName = helpFolder;
            try {
                init();
            } catch (HelpNotFoundException e) {
                e.printStackTrace();
                INSTANCE = new Help();
            }
        } else if (!helpFolder.equals(folderName)) {
            throw new IllegalStateException("Unable to set new help folder after instance has been created");
        }
        return INSTANCE;
    }

    public static Help getInstance() {
        return getInstance(folderName);
    }

    private static void init() throws HelpNotFoundException {
        if (INSTANCE == null) {
            File folder = new File(folderName);
            INSTANCE = new Help();
            INSTANCE.serverHelps = new ArrayList<>();
            if (folder.exists() && folder.isDirectory()) {
                if (folder.listFiles() == null) {
                    throw new HelpNotFoundException("No test help files found in " + folder.getName());
                }
                for (File file : folder.listFiles()) {
                    INSTANCE.serverHelps.add(new JsonReader<>(ServerHelp.class).read(file));
                }
            } else {
                throw new HelpNotFoundException("Folder " + folder.getName() + " not found. Can't provide test help");
            }
        }
    }

    public Optional<ServerHelp> getHelpFor(String software) {
        final String softwareName = software.toLowerCase().trim();
        if (serverHelps == null) {
            return Optional.empty();
        }
        return serverHelps.
                stream().
                filter(it -> it.getSoftwareName().equals(softwareName)).
                findFirst();
    }

    private static class HelpNotFoundException extends Exception {
        public HelpNotFoundException(String s) {
            super(s);
        }
    }
}
