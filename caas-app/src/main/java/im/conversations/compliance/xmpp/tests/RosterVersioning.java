package im.conversations.compliance.xmpp.tests;

import im.conversations.compliance.annotations.ComplianceTest;
import rocks.xmpp.core.session.XmppClient;
import rocks.xmpp.core.stream.model.StreamFeature;


@ComplianceTest(
        short_name = "roster_versioning",
        full_name = "Roster Versioning",
        url = "https://tools.ietf.org/html/rfc6121#section-2.6",
        description = "Provides a way to send roster changes efficiently from servers by associating a version with roster and sending only the changed parts of roster"
)
public class RosterVersioning extends AbstractStreamFeatureTest {
    public RosterVersioning(XmppClient client) {
        super(client);
    }

    @Override
    Class<? extends StreamFeature> getStreamFeature() {
        return rocks.xmpp.im.roster.versioning.model.RosterVersioning.class;
    }
}
