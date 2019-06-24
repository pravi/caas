package im.conversations.compliance.xmpp.tests;

import im.conversations.compliance.annotations.ComplianceTest;
import rocks.xmpp.core.session.XmppClient;
import rocks.xmpp.core.stream.client.StreamFeaturesManager;
import rocks.xmpp.extensions.register.model.feature.RegisterFeature;

@ComplianceTest(
        short_name = "xep0077",
        full_name = "XEP-0077: In-Band Registration",
        url = "https://xmpp.org/extensions/xep-0077.html",
        description = "Provides a protocol for registration of users directly through XMPP (in-band) or to discover a website on which users can register (out-of-band).",
        informational = true
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
            final StreamFeaturesManager streamFeaturesManager = testClient.getManager(StreamFeaturesManager.class);
            return streamFeaturesManager.getFeatures(RegisterFeature.class).size() > 0;
        } catch (Exception e) {
            return false;
        }
    }
}
