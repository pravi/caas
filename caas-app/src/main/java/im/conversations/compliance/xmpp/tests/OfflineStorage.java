package im.conversations.compliance.xmpp.tests;

import im.conversations.compliance.annotations.ComplianceTest;
import rocks.xmpp.core.session.XmppClient;

import java.util.Arrays;
import java.util.List;

@ComplianceTest(
        short_name = "xep0160",
        full_name = "XEP-0160: Best Practices for Handling Offline Messages",
        url = "https://xmpp.org/extensions/xep-0160s.html",
        description = "Checks if the server follows the best practices for Jabber/XMPP servers " +
                "in handling messages sent to recipients who are offline."
)
public class OfflineStorage extends AbstractDiscoTest {

    public OfflineStorage(XmppClient client) {
        super(client);
    }

    @Override
    List<String> getNamespaces() {
        return Arrays.asList("msgoffline");
    }

    @Override
    boolean checkOnServer() {
        return true;
    }
}
