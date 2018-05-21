package im.conversations.compliance.xmpp.tests;

import im.conversations.compliance.annotations.ComplianceTest;
import rocks.xmpp.core.session.XmppClient;

import java.util.Arrays;
import java.util.List;

@ComplianceTest(
        short_name = "xep0313",
        full_name = "XEP-0313: Message Archive Management",
        url = "https://xmpp.org/extensions/xep-0313.html",
        description = "Provides a protocol to query and control an archive of messages stored on a server. " +
                "It is used to record conversations that take place on clients that do not support local history storage, " +
                "to synchronise conversation history seamlessly between multiple clients, etc."
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
