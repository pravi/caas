package im.conversations.compliance.xmpp.tests;

import im.conversations.compliance.annotations.ComplianceTest;
import rocks.xmpp.core.session.XmppClient;

import java.util.Arrays;
import java.util.List;

@ComplianceTest(
        short_name = "xep0045",
        full_name = "XEP-0045: Multi-User Chat",
        url = "https://xmpp.org/extensions/xep-0045.html",
        description = "Provides a way for multiple users to create a room and chat with each other"
)
public class MAM extends AbstractDiscoTest {

    public static final List<String> NAMESPACES = Arrays.asList("urn:xmpp:mam:0", "urn:xmpp:mam:1", "urn:xmpp:mam:2");

    public MAM(XmppClient client) {
        super(client);
    }

    @Override
    List<String> getNamespaces() {
        return NAMESPACES;
    }

    @Override
    boolean checkOnServer() {
        return false;
    }

}
