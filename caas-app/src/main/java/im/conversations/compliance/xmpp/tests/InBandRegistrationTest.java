package im.conversations.compliance.xmpp.tests;

import im.conversations.compliance.annotations.ComplianceTest;
import rocks.xmpp.core.session.XmppClient;
import rocks.xmpp.extensions.register.RegistrationManager;

@ComplianceTest(
        short_name = "xep0077",
        full_name = "XEP-0077: In-Band Registration",
        url = "https://xmpp.org/extensions/xep-0077.html",
        description = "Provides a protocol for registration of users directly through XMPP i.e. \"in-band\""
)
public class InBandRegistrationTest extends AbstractTest {

    public InBandRegistrationTest(XmppClient client) {
        super(client);
    }

    @Override
    public boolean run() {
        final String domain = client.getConnectedResource().getDomain();
        final XmppClient testClient = XmppClient.create(domain);
        try {
            testClient.connect();
            RegistrationManager registrationManager = testClient.getManager(RegistrationManager.class);
            if (registrationManager.isRegistrationSupported().getResult()) {
                registrationManager.getRegistration().get();
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }
}