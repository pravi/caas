package im.conversations.compliance.xmpp.tests;

import im.conversations.compliance.annotations.ComplianceTest;
import im.conversations.compliance.xmpp.extensions.upload.Request;
import im.conversations.compliance.xmpp.extensions.upload.Slot;
import im.conversations.compliance.xmpp.extensions.upload.Upload;
import okhttp3.Cache;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rocks.xmpp.core.session.XmppClient;
import rocks.xmpp.core.stanza.model.IQ;
import rocks.xmpp.extensions.disco.ServiceDiscoveryManager;
import rocks.xmpp.extensions.disco.model.items.Item;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

@ComplianceTest(
        short_name = "xep0363_cors",
        full_name = "XEP-0363: HTTP File Upload (CORS Headers)",
        url = "https://xmpp.org/extensions/xep-0363.html",
        description = "Provides a protocol for transferring files between entities by uploading the file to an HTTP server.",
        informational = true
)
public class HttpUploadCors extends AbstractTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpUploadCors.class);
    private static final List<String> REQUIRED_CORS_HEADERS = Arrays.asList("Access-Control-Allow-Origin", "Access-Control-Allow-Methods", "Access-Control-Allow-Headers");
    private Request DUMMY_SLOT_REQUEST = new Request("hello.png", 1234, "image/png");

    public HttpUploadCors(XmppClient client) {
        super(client);
    }

    @Override
    public boolean run() {
        ServiceDiscoveryManager manager = client.getManager(ServiceDiscoveryManager.class);
        try {
            final List<Item> items = manager.discoverServices(Upload.NAMESPACE).getResult();
            if (items.size() == 0) {
                LOGGER.debug("No HTTP upload service found");
                return false;
            }
            IQ slotRequest = new IQ(IQ.Type.GET, DUMMY_SLOT_REQUEST);
            slotRequest.setTo(items.get(0).getJid());
            IQ response = client.query(slotRequest).getResult();
            Slot slot = response.getExtension(Slot.class);
            URL put = slot.getPut().getUrl();
            return checkCorsHeaders(put);
        } catch (Exception e) {
            LOGGER.debug(e.getMessage());
            return false;
        }
    }

    private static boolean checkCorsHeaders(URL url) throws IOException {

        LOGGER.debug("checking CORS Header on "+url.toString());

        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(url)
                .addHeader("Origin", "https://compliance.conversations.im")
                .addHeader("Access-Control-Request-Method", "PUT")
                .addHeader("Access-Control-Request-Headers", "content-type")
                .method("OPTIONS", null)
                .build();

        OkHttpClient client = new OkHttpClient();

        Response response = client.newCall(request).execute();

        final Set<String> headers = response.headers().toMultimap().keySet();

        response.close();
        for (String required : REQUIRED_CORS_HEADERS) {
            if (headers.stream().noneMatch(required::equalsIgnoreCase)) {
                LOGGER.debug("http upload missing header " + required + " headers " + headers);
                LOGGER.debug("slot url was: " + url.toString());
                shutdown(client);
                return false;
            }
        }
        shutdown(client);
        return true;
    }

    private static void shutdown(OkHttpClient client) throws IOException {
        client.dispatcher().executorService().shutdown();
        client.connectionPool().evictAll();
        final Cache cache = client.cache();
        if (cache != null) {
            cache.close();
        }
    }
}
