package im.conversations.compliance.xmpp.tests;

import im.conversations.compliance.annotations.ComplianceTest;
import rocks.xmpp.core.session.XmppClient;
import rocks.xmpp.extensions.disco.ServiceDiscoveryManager;
import rocks.xmpp.extensions.disco.model.info.Identity;
import rocks.xmpp.extensions.disco.model.info.InfoNode;
import rocks.xmpp.util.concurrent.AsyncResult;

import java.util.Set;
import java.util.concurrent.TimeUnit;

@ComplianceTest(
        short_name = "xep0163",
        full_name = "XEP-0163: Personal Eventing Protocol",
        url = "https://xmpp.org/extensions/xep-0163.html",
        description = "Allows users to send updates or events to all the contacts present in their roster. " +
                "It is also used for avatars and enabling OMEMO encryption"
)
public class PEP extends AbstractTest {

    public PEP(XmppClient client) {
        super(client);
    }

    @Override
    public boolean run() {
        ServiceDiscoveryManager manager = client.getManager(ServiceDiscoveryManager.class);
        AsyncResult<InfoNode> result = manager.discoverInformation(client.getConnectedResource().asBareJid());
        try {
            Set<Identity> identities = result.getResult(10, TimeUnit.SECONDS).getIdentities();
            for (Identity identity : identities) {
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
