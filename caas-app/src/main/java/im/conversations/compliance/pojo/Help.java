package im.conversations.compliance.pojo;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

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
    private static final Parser PARSER = Parser.builder().build();
    private static final HtmlRenderer RENDERER = HtmlRenderer.builder().build();
    private static final String HELP_RESOURCES_DIRECTORY = "help";
    private static final Help INSTANCE = new Help();
    private final Map<String, HashMap<String, String>> helps;

    private Help() {
        this.helps = load(getClass().getClassLoader());
    }

    private static Map<String, HashMap<String, String>> load(ClassLoader classLoader) {
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
                final Map<String, HashMap<String, String>> serverHelps = load(root);
                fileSystem.close();
                return serverHelps;
            } else {
                return load(Paths.get(uri));
            }

        } catch (URISyntaxException | IOException e) {
            e.printStackTrace();
            return Collections.emptyMap();
        }
    }

    private static Map<String, HashMap<String, String>> load(Path root) throws IOException {
        Map<String, HashMap<String, String>> helpMap = new HashMap<>();
        Files.walk(root, 1).filter(Files::isDirectory).forEach(folderPath -> {
            String softwareName = folderPath.getFileName().toString();
            HashMap<String, String> helpForThisSoftware = new HashMap<>();
            try {
                Files.walk(folderPath, 1).filter(Files::isRegularFile).forEach(filePath -> {
                    try {
                        Node document = PARSER.parseReader(Files.newBufferedReader(filePath));
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
            } catch (IOException ex) {
            }
            helpMap.put(softwareName, helpForThisSoftware);
        });
        return helpMap;
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
