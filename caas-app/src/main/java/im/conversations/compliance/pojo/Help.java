package im.conversations.compliance.pojo;

import com.google.gson.Gson;

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
    private static final String HELP_RESOURCES_DIRECTORY = "help";
    private static final Help INSTANCE = new Help();
    private final Map<String, ServerHelp> serverHelps;

    private Help() {
        this.serverHelps = load(getClass().getClassLoader());
    }

    private static Map<String, ServerHelp> load(ClassLoader classLoader) {
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
                final Map<String, ServerHelp> serverHelps = load(root);
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

    private static Map<String, ServerHelp> load(Path root) throws IOException {
        final Map<String, ServerHelp> serverHelps = new HashMap<>();
        final Gson gson = new Gson();
        Files.walk(root, 1).filter(Files::isRegularFile).forEach(path -> {
            try {
                final ServerHelp serverHelp = gson.fromJson(Files.newBufferedReader(path), ServerHelp.class);
                serverHelps.putIfAbsent(serverHelp.getSoftwareName(), serverHelp);
            } catch (IOException e) {
                //continue
            }
        });
        return serverHelps;
    }

    public static synchronized Help getInstance() {
        return INSTANCE;
    }

    public Optional<ServerHelp> getHelpFor(String software) {
        software = software.trim().toLowerCase();
        return Optional.ofNullable(serverHelps.get(software));
    }

}
