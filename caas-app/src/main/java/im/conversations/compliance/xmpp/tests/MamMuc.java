package im.conversations.compliance.xmpp.tests;

import im.conversations.compliance.annotations.ComplianceTest;
import im.conversations.compliance.xmpp.utils.TestUtils;
import rocks.xmpp.core.XmppException;
import rocks.xmpp.core.session.XmppClient;
import rocks.xmpp.extensions.data.model.DataForm;
import rocks.xmpp.extensions.disco.ServiceDiscoveryManager;
import rocks.xmpp.extensions.muc.ChatRoom;
import rocks.xmpp.extensions.muc.ChatService;
import rocks.xmpp.extensions.muc.MultiUserChatManager;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@ComplianceTest(
        short_name = "xep0313MUC",
        full_name = "XEP-0313: Message Archive Management for Multi-User Chat",
        url = "https://xmpp.org/extensions/xep-0313.html",
        description = "Provides a generic protocol for streaming binary data between any two entities on an XMPP network. " +
                "It establishes an out-of-band bytestream between any two XMPP users which can be direct(peer-to-peer) or mediated."
)
public class MamMuc extends AbstractTest {
    public MamMuc(XmppClient client) {
        super(client);
    }

    @Override
    public boolean run() {
        final ServiceDiscoveryManager serviceDiscoveryManager = client.getManager(ServiceDiscoveryManager.class);
        final MultiUserChatManager multiUserChatManager = client.getManager(MultiUserChatManager.class);
        try {
            List<ChatService> chatServices = multiUserChatManager.discoverChatServices().getResult();
            if (chatServices.size() < 1) {
                return false;
            }
            final ChatService chatService = chatServices.get(0);
            final ChatRoom room = chatService.createRoom(UUID.randomUUID().toString());
            room.enter("test");
            try {
                final DataForm.Field mam = room.getConfigurationForm().get().findField("mam");
                if (mam != null) {
                    return true;
                }
            } catch (ExecutionException | InterruptedException e) {
                //ignore
            }
            final Set<String> features = serviceDiscoveryManager.discoverInformation(room.getAddress()).getResult().getFeatures();
            final boolean mam = TestUtils.hasAnyone(MAM.NAMESPACES, features);
            room.destroy().getResult();
            return mam;
        } catch (XmppException e) {
            return false;
        }
    }
}
