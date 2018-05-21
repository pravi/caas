package im.conversations.compliance.xmpp.tests;

import im.conversations.compliance.annotations.ComplianceTest;
import rocks.xmpp.core.session.XmppClient;

import java.util.Arrays;
import java.util.List;

@ComplianceTest(
        short_name = "xep0280",
        full_name = "XEP-0280: Message Carbons",
        url = "https://xmpp.org/extensions/xep-0198.html",
        description = "Provides a way for all clients of a user to be engaged in a conversation " +
                "by carbon-copying outbound messages to all resources."
)
public class MessageCarbons extends AbstractDiscoTest {

    public MessageCarbons(XmppClient client) {
        super(client);
    }

    @Override
    List<String> getNamespaces() {
        return Arrays.asList("urn:xmpp:carbons:2");
    }

    @Override
    boolean checkOnServer() {
        return true;
    }
}
