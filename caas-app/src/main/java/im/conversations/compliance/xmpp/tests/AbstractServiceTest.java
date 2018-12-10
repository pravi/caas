package im.conversations.compliance.xmpp.tests;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rocks.xmpp.core.XmppException;
import rocks.xmpp.core.session.XmppClient;
import rocks.xmpp.extensions.disco.ServiceDiscoveryManager;

public abstract class AbstractServiceTest extends AbstractTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractServiceTest.class);

    public AbstractServiceTest(XmppClient client) {
        super(client);
    }

    @Override
    public boolean run() {
        ServiceDiscoveryManager manager = client.getManager(ServiceDiscoveryManager.class);
        try {
            return manager.discoverServices(getNamespace()).getResult().size() > 0;
        } catch (XmppException e) {
            LOGGER.debug(e.getMessage());
            return false;
        }
    }

    public abstract String getNamespace();
}
