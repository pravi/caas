package im.conversations.compliance.xmpp.utils;

import okhttp3.Cache;
import okhttp3.OkHttpClient;

import java.io.IOException;

public class HttpUtils {

    public static void shutdown(OkHttpClient client) throws IOException {
        client.dispatcher().executorService().shutdown();
        client.connectionPool().evictAll();
        final Cache cache = client.cache();
        if (cache != null) {
            cache.close();
        }
    }

    public static void shutdownAndIgnoreException(OkHttpClient client) {
        try {
            shutdown(client);
        } catch (Throwable t) {
            return;
        }
    }

}
