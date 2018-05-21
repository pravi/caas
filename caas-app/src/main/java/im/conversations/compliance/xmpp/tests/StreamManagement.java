package im.conversations.compliance.xmpp.tests;

import im.conversations.compliance.annotations.ComplianceTest;
import rocks.xmpp.core.session.XmppClient;
import rocks.xmpp.core.stream.model.StreamFeature;

@ComplianceTest(
        short_name = "xep0198",
        full_name = "XEP-0198: Stream Management",
        url = "https://xmpp.org/extensions/xep-0198.html",
        description = "Improves network reliability by adding support for resuming terminated streams " +
                "and adding the ability to know if a stream has been received by peer."
)
public class StreamManagement extends AbstractStreamFeatureTest {

    public StreamManagement(XmppClient client) {
        super(client);
    }

    @Override
    Class<? extends StreamFeature> getStreamFeature() {
        return rocks.xmpp.extensions.sm.model.StreamManagement.class;
    }
}
