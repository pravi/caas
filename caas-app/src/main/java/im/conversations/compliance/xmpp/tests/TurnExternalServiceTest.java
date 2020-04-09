package im.conversations.compliance.xmpp.tests;

import im.conversations.compliance.annotations.ComplianceTest;
import im.conversations.compliance.xmpp.extensions.extservices.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rocks.xmpp.core.session.XmppClient;
import rocks.xmpp.util.Strings;

import java.util.Arrays;
import java.util.List;

@ComplianceTest(
        short_name = "turn",
        full_name = "XEP-0215: External Service Discovery (TURN)",
        url = "https://xmpp.org/extensions/xep-0215.html",
        description = "Checks if the server provides a TURN server",
        informational = true
)
public class TurnExternalServiceTest extends AbstractExternalServiceTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(TurnExternalServiceTest.class);

    public TurnExternalServiceTest(XmppClient client) {
        super(client);
    }

    @Override
    boolean test(List<Service> services) {
        for (final Service service : services) {
            final String host = service.getHost();
            final int port = service.getPort();
            final String username = service.getUsername();
            final String password = service.getPassword();

            if (host == null || port < 0 || port > 65535) {
                LOGGER.debug("invalid host or port for turn server");
                continue;
            }

            if (Arrays.asList("tcp", "udp").contains(service.getTransport()) && "turn".equals(service.getType())) {
                if (Strings.isNullOrEmpty(username) || Strings.isNullOrEmpty(password)) {
                    LOGGER.debug("{}/{} server without username and password are invalid in webrtc", service.getType(), service.getTransport());
                    continue;
                }
                return true;
            }
        }
        return false;
    }

}
