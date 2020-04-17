package im.conversations.compliance.xmpp.tests;

import im.conversations.compliance.annotations.ComplianceTest;
import im.conversations.compliance.xmpp.extensions.extservices.Service;
import rocks.xmpp.core.session.XmppClient;

import java.util.Arrays;
import java.util.List;

@ComplianceTest(
        short_name = "stun",
        full_name = "XEP-0215: External Service Discovery (STUN)",
        url = "https://xmpp.org/extensions/xep-0215.html",
        description = "Checks if the server provides a STUN server",
        informational = true
)
public class StunExternalServiceTest extends AbstractExternalServiceTest {
    public StunExternalServiceTest(XmppClient client) {
        super(client);
    }

    @Override
    boolean test(List<Service> services) {
        for (final Service service : services) {
            final String host = service.getHost();
            final int port = service.getPort();

            if (host == null || port < 0 || port > 65535) {
                continue;
            }

            if (Arrays.asList("tcp", "udp").contains(service.getTransport()) && Arrays.asList("stun","stuns").contains(service.getType())) {
                return true;
            }
        }
        return false;
    }
}
