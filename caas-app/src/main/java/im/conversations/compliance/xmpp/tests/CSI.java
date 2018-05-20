package im.conversations.compliance.xmpp.tests;

import im.conversations.compliance.annotations.ComplianceTest;
import im.conversations.compliance.xmpp.extensions.csi.ClientStateIndication;
import rocks.xmpp.core.session.XmppClient;
import rocks.xmpp.core.stream.model.StreamFeature;

@ComplianceTest(
        short_name = "xep0352",
        full_name = "XEP-0352: Client State Indication",
        url = "https://xmpp.org/extensions/xep-0352.html",
        description = "Allows a client to indicate its active/inactive state. This reduces bandwidth and resources required by optimising the traffic accordingly"
)
public class CSI extends AbstractStreamFeatureTest {

    public CSI(XmppClient client) {
        super(client);
    }

    @Override
    Class<? extends StreamFeature> getStreamFeature() {
        return ClientStateIndication.class;
    }
}
