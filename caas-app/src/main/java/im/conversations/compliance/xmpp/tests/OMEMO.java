package im.conversations.compliance.xmpp.tests;

import im.conversations.compliance.annotations.ComplianceTest;
import rocks.xmpp.core.session.XmppClient;
import rocks.xmpp.extensions.disco.ServiceDiscoveryManager;
import rocks.xmpp.extensions.disco.model.info.Identity;
import rocks.xmpp.extensions.disco.model.info.InfoNode;
import rocks.xmpp.util.concurrent.AsyncResult;

import java.util.Set;
import java.util.concurrent.TimeUnit;

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
    private static final String WHITELISTED = "eu.siacs.conversations.axolotl.whitelisted";

    public OMEMO(XmppClient client) {
        super(client);
    }

    @Override
    public boolean run() {
        ServiceDiscoveryManager manager = client.getManager(ServiceDiscoveryManager.class);
        AsyncResult<InfoNode> result = manager.discoverInformation(client.getConnectedResource().asBareJid());
        try {
            final InfoNode infoNode = result.get(10, TimeUnit.SECONDS);
            final Set<String> features = infoNode.getFeatures();
            if (!features.contains(PUBLISH_OPTIONS) && !features.contains(WHITELISTED)) {
                return false;
            }
            for (Identity identity : infoNode.getIdentities()) {
                if ("pep".equalsIgnoreCase(identity.getType()) && "pubsub".equalsIgnoreCase(identity.getCategory())) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
