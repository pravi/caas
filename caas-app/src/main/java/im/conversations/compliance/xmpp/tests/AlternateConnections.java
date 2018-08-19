package im.conversations.compliance.xmpp.tests;

import com.google.gson.*;
import im.conversations.compliance.annotations.ComplianceTest;
import im.conversations.compliance.xrd.ExtensibleResourceDescriptor;
import im.conversations.compliance.xrd.Link;
import rocks.xmpp.core.session.XmppClient;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.List;


@ComplianceTest(
        short_name = "xep0156",
        informational = true,
        full_name = "XEP-0156: Discovering Alternative XMPP Connection Methods (HTTP)",
        url = "https://xmpp.org/extensions/xep-0156.html",
        description = "Allows web clients to discover connections methods"
)
public class AlternateConnections extends AbstractTest {

    private static final GsonBuilder gsonBuilder = new GsonBuilder();
    public static final Gson gson = gsonBuilder.create();
    private static final List<String> rels = Arrays.asList("urn:xmpp:alt-connections:xbosh", "urn:xmpp:alt-connections:websocket");

    public AlternateConnections(XmppClient client) {
        super(client);
    }

    @Override
    public boolean run() {
        return discoverAndTestAltConnections(client.getDomain().toString());
    }

    private static boolean discoverAndTestAltConnections(final String domain) {
        return discoverAndTestAltConnections(domain, true) || discoverAndTestAltConnections(domain, false);
    }

    private static boolean discoverAndTestAltConnections(final String domain, final boolean https) {
        try (final InputStream is = new URL(https ? "https" : "http", domain, "/.well-known/host-meta").openStream()) {
            return testAltConnectionsFromXml(is);
        } catch (Throwable throwable) {
            try (final InputStream is = new URL(https ? "https" : "http", domain, "/.well-known/host-meta.json").openStream()) {
                return testAltConnectionsFromJson(is);
            } catch (Throwable e) {
                return false;
            }
        }
    }

    private static boolean testAltConnectionsFromXml(InputStream is) throws Throwable {
        final JAXBContext jaxbContext = JAXBContext.newInstance(ExtensibleResourceDescriptor.class, Link.class);
        final Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        final ExtensibleResourceDescriptor xdr = (ExtensibleResourceDescriptor) unmarshaller.unmarshal(is);
        final List<Link> links = xdr.getLinks(rels);
        for (Link link : links) {
            if (!checkConnection(link.getHref())) {
                return false;
            }
        }
        return links.size() > 0;
    }

    private static boolean testAltConnectionsFromJson(InputStream is) throws Throwable {
        JsonParser parser = new JsonParser();
        JsonElement jsonElement = parser.parse(new InputStreamReader(is));
        JsonArray linksArray = jsonElement.getAsJsonObject().get("links").getAsJsonArray();
        boolean alternateFound = false;
        for(JsonElement element: linksArray) {
            JsonObject object = element.getAsJsonObject();
            if(!rels.contains(object.get("rel").getAsString())) {
                continue;
            }
            URI uri = new URI(object.get("href").getAsString());
            if(!checkConnection(uri)) {
                return false;
            }
            alternateFound = true;
        }
        return alternateFound;
    }

    private static boolean checkConnection(URI uri) {
        int port = uri.getPort();
        if (port < 0) {
            if (Arrays.asList("wss", "https").contains(uri.getScheme())) {
                port = 443;
            } else if (Arrays.asList("ws", "http").contains(uri.getScheme())) {
                port = 80;
            }
        }
        return checkConnection(uri.getHost(), port);
    }

    private static boolean checkConnection(String host, int port) {
        try (final Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port));
            return socket.isConnected();
        } catch (Throwable t) {
            return false;
        }
    }
}
