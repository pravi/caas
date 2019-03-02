package im.conversations.compliance.xmpp.tests;

import im.conversations.compliance.annotations.ComplianceTest;
import im.conversations.compliance.xmpp.extensions.omemo.Bundle;
import rocks.xmpp.core.session.XmppClient;
import rocks.xmpp.core.stanza.model.StanzaErrorException;
import rocks.xmpp.core.stanza.model.errors.Condition;
import rocks.xmpp.extensions.disco.ServiceDiscoveryManager;
import rocks.xmpp.extensions.disco.model.info.Identity;
import rocks.xmpp.extensions.disco.model.info.InfoNode;
import rocks.xmpp.extensions.pubsub.PubSubManager;
import rocks.xmpp.extensions.pubsub.PubSubNode;
import rocks.xmpp.extensions.pubsub.PubSubService;
import rocks.xmpp.extensions.pubsub.model.AccessModel;
import rocks.xmpp.extensions.pubsub.model.NodeConfiguration;
import rocks.xmpp.extensions.pubsub.model.PublishOptions;
import rocks.xmpp.util.concurrent.AsyncResult;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static im.conversations.compliance.xmpp.extensions.omemo.OMEMO.NAMESPACE;

/**
 * This test checks for the availability of publish-options on the account’s PEP service.
 * publish-options allows a client to efficiently change the access model of the OMEMO key material
 * such that everyone can access it. Without publish-options OMEMO is only available to contacts with
 * mutual presence subscription.
 */
@ComplianceTest(
        short_name = "xep0384",
        full_name = "XEP-0384: OMEMO Encryption",
        url = "https://xmpp.org/extensions/xep-0384.html",
        description = "Provides a protocol for secure multi-client end-to-end encryption."
)
public class OMEMO extends AbstractTest {

    private static final String PUBLISH_OPTIONS = "http://jabber.org/protocol/pubsub#publish-options";
    private static final String WHITELISTED = NAMESPACE + ".whitelisted";

    public OMEMO(XmppClient client) {
        super(client);
    }

    private static boolean hasPepIdentity(InfoNode infoNode) {
        for (Identity identity : infoNode.getIdentities()) {
            if ("pep".equalsIgnoreCase(identity.getType()) && "pubsub".equalsIgnoreCase(identity.getCategory())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean run() {
        ServiceDiscoveryManager manager = client.getManager(ServiceDiscoveryManager.class);
        AsyncResult<InfoNode> result = manager.discoverInformation(client.getConnectedResource().asBareJid());
        try {
            final InfoNode infoNode = result.get(10, TimeUnit.SECONDS);
            if (!hasPepIdentity(infoNode)) {
                return false;
            }
            final Set<String> features = infoNode.getFeatures();
            if (features.contains(PUBLISH_OPTIONS)) {
                PubSubService personalEventingService = client.getManager(PubSubManager.class).createPersonalEventingService();
                final String temporaryTestNamespace = NAMESPACE + "." + UUID.randomUUID();
                PubSubNode node = personalEventingService.node(temporaryTestNamespace);
                publishWithAccessModel(node, new Bundle(), AccessModel.OPEN, true);
                NodeConfiguration configuration = node.getNodeConfiguration().get();
                AccessModel accessModel = configuration.getAccessModel();
                node.delete().get();
                return accessModel.equals(AccessModel.OPEN);
            } else {
                return features.contains(WHITELISTED);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean publishWithAccessModel(final PubSubNode node, final Object object, final AccessModel accessModel, final boolean retry) {
        try {
            node.publish(object, PublishOptions.builder().accessModel(accessModel).build()).get();
            return true;
        } catch (ExecutionException e) {
            if (e.getCause() instanceof StanzaErrorException) {
                StanzaErrorException stanzaException = (StanzaErrorException) e.getCause();
                if (stanzaException.getCondition().equals(Condition.CONFLICT) && retry) {
                    try {
                        node.configureNode(NodeConfiguration.builder().accessModel(accessModel).build()).get();
                    } catch (Exception inner) {
                        return false;
                    }
                    return publishWithAccessModel(node, object, accessModel, false);
                }
            }
            return false;
        } catch (InterruptedException e) {
            return false;
        }
    }
}
