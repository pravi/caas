package im.conversations.compliance.pojo;

import com.google.gson.Gson;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class Help {
    private static final String HELP_RESOURCES_DIRECTORY = "help";
    private static final Help INSTANCE = new Help();
    private final List<ServerHelp> serverHelps;

    private Help() {
        this.serverHelps = load(getClass().getClassLoader());
    }

    private static List<ServerHelp> load(ClassLoader classLoader) {
        try {
            final URL resources = classLoader.getResource(HELP_RESOURCES_DIRECTORY);
            if (resources == null) {
                return Collections.emptyList();
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
                final List<ServerHelp> serverHelps = load(root);
                fileSystem.close();
                return serverHelps;
            } else {
                return load(Paths.get(uri));
            }

        } catch (URISyntaxException | IOException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    private static List<ServerHelp> load(Path root) throws IOException {
        final List<ServerHelp> serverHelps = new ArrayList<>();
        final Gson gson = new Gson();
        Files.walk(root, 1).filter(Files::isRegularFile).forEach(path -> {
            try {
                serverHelps.add(gson.fromJson(Files.newBufferedReader(path), ServerHelp.class));
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
        final String softwareName = software.toLowerCase().trim();
        if (serverHelps == null) {
            return Optional.empty();
        }
        return serverHelps.
                stream().
                filter(it -> it.getSoftwareName().equals(softwareName)).
                findFirst();
    }

}
