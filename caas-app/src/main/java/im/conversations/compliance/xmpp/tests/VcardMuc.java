package im.conversations.compliance.xmpp.tests;

import im.conversations.compliance.annotations.ComplianceTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rocks.xmpp.core.XmppException;
import rocks.xmpp.core.session.XmppClient;
import rocks.xmpp.extensions.disco.ServiceDiscoveryManager;
import rocks.xmpp.extensions.muc.ChatRoom;
import rocks.xmpp.extensions.muc.ChatService;
import rocks.xmpp.extensions.muc.MultiUserChatManager;
import rocks.xmpp.extensions.vcard.temp.model.VCard;

import java.util.List;
import java.util.Set;

import static im.conversations.compliance.utils.ConversationsUtils.generateConversationsLikePronounceableName;

@ComplianceTest(
        short_name = "vcard_muc",
        full_name = "XEP-0153: vCard-Based Avatar (MUC)",
        description = "MUC Avatars",
        url = "https://xmpp.org"

)
public class VcardMuc extends AbstractTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(VcardMuc.class);

    public VcardMuc(XmppClient client) {
        super(client);
    }

    @Override
    public boolean run() {
        final ServiceDiscoveryManager serviceDiscoveryManager = client.getManager(ServiceDiscoveryManager.class);
        final MultiUserChatManager multiUserChatManager = client.getManager(MultiUserChatManager.class);
        try {
            List<ChatService> chatServices = multiUserChatManager.discoverChatServices().getResult();
            if (chatServices.size() < 1) {
                LOGGER.debug("Unable to find a MUC service");
                return false;
            }
            final ChatService chatService = chatServices.get(0);
            final ChatRoom room = chatService.createRoom(generateConversationsLikePronounceableName());
            room.enter("test").getResult();
            final Set<String> features = serviceDiscoveryManager.discoverInformation(room.getAddress()).getResult().getFeatures();
            room.destroy().getResult();
            return features.contains(VCard.NAMESPACE);
        } catch (XmppException e) {
            LOGGER.debug(e.getMessage());
        }
        return false;
    }
}
