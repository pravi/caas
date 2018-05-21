package im.conversations.compliance.xmpp.tests;

import im.conversations.compliance.annotations.ComplianceTest;
import rocks.xmpp.addr.Jid;
import rocks.xmpp.core.XmppException;
import rocks.xmpp.core.session.XmppClient;
import rocks.xmpp.extensions.data.model.DataForm;
import rocks.xmpp.extensions.disco.ServiceDiscoveryManager;

import java.util.List;

@ComplianceTest(
        short_name = "xep0157",
        full_name = "XEP-0157: Contact Addresses for XMPP Services",
        url = "https://xmpp.org/extensions/xep-0163.html",
        description = "Checks if the server has a contact for reporting abuse."
)
public class AbuseContactTest extends AbstractTest {
    public AbuseContactTest(XmppClient client) {
        super(client);
    }

    public static void main(String[] args) {
        System.out.println("Hoo");
    }

    @Override
    public boolean run() {
        Jid target = Jid.of(client.getConnectedResource().getDomain());
        final ServiceDiscoveryManager serviceDiscoveryManager = client.getManager(ServiceDiscoveryManager.class);
        try {
            List<DataForm> extensions = serviceDiscoveryManager.discoverInformation(target).getResult().getExtensions();
            for (DataForm extension : extensions) {
                final DataForm.Field addr = extension.findField("abuse-addresses");
                if (addr != null && addr.getValues().size() > 0) {
                    return true;
                }
            }
            return false;
        } catch (XmppException e) {
            return false;
        }
    }
}
