package im.conversations.compliance.xmpp.tests;

import im.conversations.compliance.annotations.ComplianceTest;
import rocks.xmpp.core.session.XmppClient;

import java.util.Collections;
import java.util.List;

@ComplianceTest(
        short_name = "xep0411",
        full_name = "XEP-0411: Bookmarks Conversion",
        url = "https://xmpp.org/extensions/xep-0411.html",
        description = "Converts between private-xml and PEP based bookmarks"
)
public class BookmarkConversion extends AbstractDiscoTest {

    public BookmarkConversion(XmppClient client) {
        super(client);
    }

    @Override
    List<String> getNamespaces() {
        return Collections.singletonList("urn:xmpp:bookmarks-conversion:0");
    }

    @Override
    boolean checkOnServer() {
        return false;
    }
}
