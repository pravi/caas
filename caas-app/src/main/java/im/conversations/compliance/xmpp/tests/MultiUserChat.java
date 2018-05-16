package im.conversations.compliance.xmpp.tests;

import im.conversations.compliance.annotations.ComplianceTest;
import rocks.xmpp.core.session.XmppClient;
import rocks.xmpp.extensions.muc.model.Muc;


@ComplianceTest(
        short_name = "xep0045",
        full_name = "XEP-0045: Multi-User Chat",
        url = "https://xmpp.org/extensions/xep-0045.html",
        description = "Provides a way for multiple users to create a room and chat with each other"
)
public class MultiUserChat extends AbstractServiceTest {

    public MultiUserChat(XmppClient client) {
        super(client);
    }

    @Override
    public String getNamespace() {
        return Muc.NAMESPACE;
    }
}
