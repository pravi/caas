package im.conversations.compliance.pojo;

import im.conversations.compliance.utils.JsonReader;

import java.io.*;
import java.util.ArrayList;
import java.util.Optional;

public class Help {
    private static final String helpFilesLocationFolder = "test_help/servers.txt";
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
                InputStream helpLocationStream = classLoader.getResourceAsStream(helpFilesLocationFolder);
                BufferedReader reader = new BufferedReader(new InputStreamReader(helpLocationStream));
                String fileName;
                while((fileName = reader.readLine()) != null) {
                    classLoader.getResourceAsStream(fileName);
                    INSTANCE.serverHelps.add(new JsonReader<>(ServerHelp.class)
                            .read(classLoader.getResourceAsStream(fileName))
                    );
                    System.out.println("Added help from " + fileName);
                }
            } catch (IOException e) {
                e.printStackTrace();
                throw new HelpNotFoundException(e.getMessage());
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
