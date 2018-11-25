package im.conversations.compliance.xmpp.tests;

import im.conversations.compliance.annotations.ComplianceTest;
import im.conversations.compliance.xmpp.utils.TestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rocks.xmpp.core.XmppException;
import rocks.xmpp.core.session.XmppClient;
import rocks.xmpp.extensions.data.model.DataForm;
import rocks.xmpp.extensions.disco.ServiceDiscoveryManager;
import rocks.xmpp.extensions.muc.ChatRoom;
import rocks.xmpp.extensions.muc.ChatService;
import rocks.xmpp.extensions.muc.MultiUserChatManager;

import java.security.SecureRandom;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import static im.conversations.compliance.utils.ConversationsUtils.generateConversationsLikePronounceableName;

@ComplianceTest(
        short_name = "xep0313muc",
        full_name = "XEP-0313: Message Archive Management (for Multi-User Chat)",
        url = "https://xmpp.org/extensions/xep-0313.html",
        description = "Provides a protocol to query and control an archive of messages of a multi user chat stored on a server. " +
                "It is used to synchronise conversation history seamlessly between multiple clients, " +
                "record conversations that take place on clients that do not support local history storage, etc. for a MUC"
)
public class MamMuc extends AbstractTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(MamMuc.class);

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
                LOGGER.debug("Unable to find a MUC service");
                return false;
            }
            final ChatService chatService = chatServices.get(0);
            final ChatRoom room = chatService.createRoom(generateConversationsLikePronounceableName());
            room.enter("test").getResult();
            boolean hasFormField = false;
            try {
                DataForm form = room.getConfigurationForm().get();
                final DataForm.Field mam = form.findField("mam"); //ejabberd community
                final DataForm.Field roomConfigMam = form.findField("muc#roomconfig_mam"); //ejabberd SaaS
                final DataForm.Field roomConfigEnable = form.findField("muc#roomconfig_enablearchiving");
                hasFormField = mam != null || roomConfigMam != null || roomConfigEnable != null;
            } catch (ExecutionException | InterruptedException e) {
                //ignore
            }
            final Set<String> features = serviceDiscoveryManager.discoverInformation(room.getAddress()).getResult().getFeatures();
            final boolean hasFeature = TestUtils.hasAnyone(MAM.NAMESPACES, features);
            room.destroy().getResult();
            return (hasFeature || hasFormField);
        } catch (XmppException e) {
            LOGGER.debug("Unable to create a test room ("+e.getMessage()+")");
            return false;
        }
    }
}
