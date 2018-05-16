package im.conversations.compliance.xmpp.tests;

import im.conversations.compliance.annotations.ComplianceTest;
import rocks.xmpp.core.session.XmppClient;

import java.util.Arrays;
import java.util.List;

@ComplianceTest(
        short_name = "xep0191",
        full_name = "XEP-0191: Blocking Command",
        url = "https://xmpp.org/extensions/xep-0191.html",
        description = "Provides an easy to implement method to block communications with selected users"
)
public class Blocking extends AbstractDiscoTest {


    public Blocking(XmppClient client) {
        super(client);
    }

    @Override
    List<String> getNamespaces() {
        return Arrays.asList("urn:xmpp:blocking");
    }

    @Override
    boolean checkOnServer() {
        return true;
    }
}
