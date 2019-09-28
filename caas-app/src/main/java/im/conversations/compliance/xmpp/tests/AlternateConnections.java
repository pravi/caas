package im.conversations.compliance.xmpp.tests;

import com.google.gson.GsonBuilder;
import im.conversations.compliance.annotations.ComplianceTest;
import im.conversations.compliance.xmpp.utils.HttpUtils;
import im.conversations.compliance.xrd.ExtensibleResourceDescriptor;
import im.conversations.compliance.xrd.Link;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rocks.xmpp.core.session.XmppClient;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Map;


@ComplianceTest(
        short_name = "xep0156",
        informational = true,
        full_name = "XEP-0156: Discovering Alternative XMPP Connection Methods (HTTP)",
        url = "https://xmpp.org/extensions/xep-0156.html",
        description = "Allows web clients to discover connections methods"
)
public class AlternateConnections extends AbstractTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(AlternateConnections.class);

    private static final GsonBuilder GSON_BUILDER = new GsonBuilder();
    private static final List<String> rels = Arrays.asList("urn:xmpp:alt-connections:xbosh", "urn:xmpp:alt-connections:websocket");


    public AlternateConnections(XmppClient client) {
        super(client);
    }

    private static boolean testAltConnectionsFromXml(InputStream is) throws Throwable {
        final JAXBContext jaxbContext = JAXBContext.newInstance(ExtensibleResourceDescriptor.class, Link.class);
        final Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        final ExtensibleResourceDescriptor xdr = (ExtensibleResourceDescriptor) unmarshaller.unmarshal(is);
        final List<Link> links = xdr.getLinks(rels);
        return checkConnections(links);
    }

    private static boolean testAltConnectionsFromJson(InputStream is) {
        final ExtensibleResourceDescriptor xdr = GSON_BUILDER.create().fromJson(new InputStreamReader(is), ExtensibleResourceDescriptor.class);
        final List<Link> links = xdr.getLinks(rels);
        return checkConnections(links);
    }

    private static boolean checkConnections(List<Link> links) {
        for (Link link : links) {
            if (!checkConnection(link.getHref())) {
                return false;
            }
        }
        return links.size() > 0;
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
        return checkConnection(uri, port);
    }

    private static boolean checkConnection(URI uri, int port) {
        final String url;
        switch (uri.getScheme()) {
            case "wss":
                url = String.format("https://%s:%d/%s", uri.getHost(), port, uri.getPath());
                break;
            case "ws":
                url = String.format("http://%s:%d/%s", uri.getHost(), port, uri.getPath());
                break;
            default:
                url = String.format("%s://%s:%d/%s", uri.getScheme(), uri.getHost(), port, uri.getPath());
                break;
        }
        final OkHttpClient okHttpClient = new OkHttpClient();
        LOGGER.debug(String.format("checking CORS Headers on %s", url));
        try {
            Request request = new Request.Builder().url(url).head().build();
            Response response = okHttpClient.newCall(request).execute();
            Map<String, List<String>> headers = response.headers().toMultimap();
            return containsIgnoreCase(headers, "Access-Control-Allow-Origin", "*");
        } catch (Throwable t) {
            return false;
        } finally {
            HttpUtils.shutdownAndIgnoreException(okHttpClient);
        }
    }

    private static boolean containsIgnoreCase(Map<String, List<String>> headers, String needle, String expectedValue) {
        for (Map.Entry<String, List<String>> header : headers.entrySet()) {
            if (header.getKey().equalsIgnoreCase(needle)) {
                return header.getValue().contains(expectedValue);
            }
        }
        return false;
    }

    private boolean discoverAndTestAltConnections(final String domain) {
        return discoverAndTestAltConnections(domain, false, true)
                || discoverAndTestAltConnections(domain, true, true)
                || discoverAndTestAltConnections(domain, false, false)
                || discoverAndTestAltConnections(domain, true, false);
    }

    private boolean discoverAndTestAltConnections(final String domain, final boolean json, final boolean https) {
        OkHttpClient okHttpClient = new OkHttpClient();
        try {
            final URL url = new URL(https ? "https" : "http", domain, "/.well-known/host-meta" + (json ? ".json" : ""));
            LOGGER.debug(String.format("checking on %s ", url.toString()));
            Request request = new Request.Builder().url(url).build();
            Response response = okHttpClient.newCall(request).execute();
            Map<String, List<String>> headers = response.headers().toMultimap();
            if (!containsIgnoreCase(headers, "Access-Control-Allow-Origin", "*")) {
                response.close();
                LOGGER.debug(url + " did not set Access-Control-Allow-Origin to *");
                return false;
            }
            final ResponseBody body = response.body();
            if (body == null) {
                response.close();
                return false;
            }
            try (final InputStream is = body.byteStream()) {
                return json ? testAltConnectionsFromJson(is) : testAltConnectionsFromXml(is);
            } finally {
                response.close();
            }
        } catch (Throwable throwable) {
            LOGGER.debug("unable to discover ", throwable);
            return false;
        } finally {
            HttpUtils.shutdownAndIgnoreException(okHttpClient);
        }
    }

    @Override
    public boolean run() {
        return discoverAndTestAltConnections(client.getDomain().toString());
    }
}
