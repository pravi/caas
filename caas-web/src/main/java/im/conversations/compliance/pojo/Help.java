package im.conversations.compliance.pojo;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Help {
    private static final Logger LOGGER = LoggerFactory.getLogger(Help.class);
    private static final Parser PARSER = Parser.builder().build();
    private static final HtmlRenderer RENDERER = HtmlRenderer.builder().build();
    private static final String HELP_RESOURCES_DIRECTORY = "help";
    private static final Help INSTANCE = new Help();
    private final Map<String, HashMap<String, String>> helps;

    private Help() {
        this.helps = load(getClass().getClassLoader());
    }

    private static Map<String, HashMap<String, String>> load(ClassLoader classLoader) {
        Map<String, HashMap<String, String>> helpsMap = new HashMap<>();
        try {
            final URL resources = classLoader.getResource(HELP_RESOURCES_DIRECTORY);
            if (resources == null) {
                return Collections.emptyMap();
            }
            final URI uri = resources.toURI();
            if (uri.getScheme().equals("jar")) {
                FileSystem fileSystem;
                try {
                    fileSystem = FileSystems.getFileSystem(uri);
                } catch (FileSystemNotFoundException e) {
                    fileSystem = FileSystems.newFileSystem(uri, Collections.emptyMap());
                }
                final Path root = fileSystem.getPath(HELP_RESOURCES_DIRECTORY);
                final FileSystem finalFileSystem = fileSystem;
                Files.walk(root, 1).filter(Files::isDirectory).forEach(folderPath -> {
                    //Prevent it from trying to read the same folder
                    if(folderPath.equals(root)) {
                        return;
                    }
                    String softwareName = folderPath.getFileName().toString();
                    //Remove / at the end
                    if (softwareName.endsWith("/")) {
                        softwareName = softwareName.substring(0, softwareName.length() - 1);
                    }
                    final Path softwareFolder = finalFileSystem.getPath(HELP_RESOURCES_DIRECTORY + "/" + softwareName);
                    try {
                        helpsMap.put(softwareName, load(softwareFolder));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
                fileSystem.close();
            } else {
                Path root = Paths.get(uri);
                Files.walk(root, 1).filter(Files::isDirectory).forEach(folderPath -> {
                    //Prevent it from trying to read the same folder
                    if(folderPath.equals(root)) {
                        return;
                    }
                    String softwareName = folderPath.getFileName().toString();
                    try {
                        helpsMap.put(softwareName, load(folderPath));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            }
            return helpsMap;

        } catch (URISyntaxException | IOException e) {
            e.printStackTrace();
            return Collections.emptyMap();
        }
    }

    private static HashMap<String, String> load(Path serverFolder) throws IOException {
        HashMap<String, String> helpForThisSoftware = new HashMap<>();
        Files.walk(serverFolder, 1).filter(Files::isRegularFile).forEach(filePath -> {
            Node document;
            try {
                document = PARSER.parseReader(Files.newBufferedReader(filePath));
                String htmlText = RENDERER.render(document);
                String testFileName = filePath.getFileName().toString();
                int length = testFileName.length();
                if (!testFileName.substring(length - 3).equals(".md")) {
                    throw new IllegalStateException("All help files should have md extension");
                }
                String testName = testFileName.substring(0, (testFileName.length() - 3));
                helpForThisSoftware.put(testName, htmlText);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        LOGGER.info(
                "Read help file for " + serverFolder.getFileName().toString() +
                        " which contained help for " + helpForThisSoftware.size() + " tests"
        );
        return helpForThisSoftware;
    }

    public static synchronized Help getInstance() {
        return INSTANCE;
    }

    public Optional<HashMap<String, String>> getHelpFor(String software) {
        if(software == null) {
            return Optional.empty();
        }
        software = software.trim().toLowerCase();
        return Optional.ofNullable(helps.get(software));
    }

}
