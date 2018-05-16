package im.conversations.compliance.xmpp.tests;

import im.conversations.compliance.annotations.ComplianceTest;
import rocks.xmpp.core.session.XmppClient;

import java.util.Arrays;
import java.util.List;

@ComplianceTest(
        short_name = "xep0357",
        full_name = "XEP-0357: Push Notifications",
        url = "https://xmpp.org/extensions/xep-0357.html",
        description = "Defines a way for an XMPP servers to deliver information for use in push notifications to mobile and other devices."
)
public class Push extends AbstractDiscoTest {

    public Push(XmppClient client) {
        super(client);
    }

    @Override
    List<String> getNamespaces() {
        return Arrays.asList("urn:xmpp:push:0");
    }

    @Override
    boolean checkOnServer() {
        return false;
    }
}
