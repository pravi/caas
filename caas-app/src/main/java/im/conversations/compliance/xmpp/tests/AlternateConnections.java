package im.conversations.compliance.xmpp.tests;

import im.conversations.compliance.annotations.ComplianceTest;
import im.conversations.compliance.xrd.ExtensibleResourceDescriptor;
import im.conversations.compliance.xrd.Link;
import rocks.xmpp.core.session.XmppClient;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.io.InputStream;
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
            final JAXBContext jaxbContext = JAXBContext.newInstance(ExtensibleResourceDescriptor.class, Link.class);
            final Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            final ExtensibleResourceDescriptor xdr = (ExtensibleResourceDescriptor) unmarshaller.unmarshal(is);
            final List<Link> links = xdr.getLinks("urn:xmpp:alt-connections:xbosh", "urn:xmpp:alt-connections:websocket");
            for (Link link : links) {
                if (!checkConnection(link.getHref())) {
                    return false;
                }
            }
            return links.size() > 0;
        } catch (Throwable throwable) {
            return false;
        }
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
