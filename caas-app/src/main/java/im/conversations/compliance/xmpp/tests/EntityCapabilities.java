package im.conversations.compliance.xmpp.tests;

import im.conversations.compliance.annotations.ComplianceTest;
import rocks.xmpp.core.session.XmppClient;
import rocks.xmpp.core.stream.model.StreamFeature;

/**
 * This test is looking for the c element in a servers stream features
 * see http://xmpp.org/extensions/xep-0115.html#stream
 */
@ComplianceTest(
        short_name = "xep0115",
        full_name = "XEP-0115: Entity Capabilities",
        url = "https://xmpp.org/extensions/xep-0115.html",
        description = "Provides a robust, scalable way for exchanging information about capabilities supported by an entity. " +
                "It caches the results determined with the help of XEP-0030 for improved network efficiency."
)
public class EntityCapabilities extends AbstractStreamFeatureTest {

    public EntityCapabilities(XmppClient client) {
        super(client);
    }

    @Override
    Class<? extends StreamFeature> getStreamFeature() {
        return rocks.xmpp.extensions.caps.model.EntityCapabilities.class;
    }
}
