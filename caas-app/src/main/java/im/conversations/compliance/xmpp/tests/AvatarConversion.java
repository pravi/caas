package im.conversations.compliance.xmpp.tests;

import im.conversations.compliance.annotations.ComplianceTest;
import rocks.xmpp.core.session.XmppClient;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@ComplianceTest(
        short_name = "xep0398",
        full_name = "XEP-0398: User Avatar to vCard-Based Avatars Conversion",
        url = "https://xmpp.org/extensions/xep-0398.html",
        description = "Converts between vCard and PEP based avatars"
)
public class AvatarConversion extends AbstractDiscoTest {

    public AvatarConversion(XmppClient client) {
        super(client);
    }

    @Override
    List<String> getNamespaces() {
        return Collections.singletonList("urn:xmpp:pep-vcard-conversion:0");
    }

    @Override
    boolean checkOnServer() {
        return false;
    }
}
