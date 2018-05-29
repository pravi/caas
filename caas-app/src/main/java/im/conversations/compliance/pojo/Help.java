package im.conversations.compliance.pojo;

import im.conversations.compliance.utils.JsonReader;

import java.io.File;
import java.util.ArrayList;
import java.util.Optional;

public class Help {
    private static final String folderName = "test_help";
    private static Help INSTANCE;
    private ArrayList<ServerHelp> serverHelps;

    private Help() {
    }

    public static Help getInstance() {
        if (INSTANCE == null) {
            try {
                init();
            } catch (HelpNotFoundException e) {
                e.printStackTrace();
                INSTANCE = new Help();
            }
        }
        return INSTANCE;
    }

    private static void init() throws HelpNotFoundException {
        if (INSTANCE == null) {
            INSTANCE = new Help();
            INSTANCE.serverHelps = new ArrayList<>();
            try {
                ClassLoader classLoader = INSTANCE.getClass().getClassLoader();
                File folder = new File(classLoader.getResource(folderName).getFile());
                if (folder.listFiles() == null) {
                    throw new HelpNotFoundException("No test help files found in " + folder.getName());
                }
                for (File file : folder.listFiles()) {
                    INSTANCE.serverHelps.add(new JsonReader<>(ServerHelp.class).read(file));
                }
            } catch (NullPointerException ex) {
                throw new HelpNotFoundException("Folder " + folderName + " not found. Can't provide test help");
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
