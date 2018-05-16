package im.conversations.compliance.xmpp.tests;

import im.conversations.compliance.xmpp.utils.TestUtils;
import rocks.xmpp.addr.Jid;
import rocks.xmpp.core.XmppException;
import rocks.xmpp.core.session.XmppClient;
import rocks.xmpp.extensions.disco.ServiceDiscoveryManager;

import java.util.List;
import java.util.Set;

public abstract class AbstractDiscoTest extends AbstractTest {

    public AbstractDiscoTest(XmppClient client) {
        super(client);
    }

    //test will succeed if any namespace matches
    abstract List<String> getNamespaces();

    abstract boolean checkOnServer();

    @Override
    public boolean run() {
        Jid target = checkOnServer() ? Jid.of(client.getConnectedResource().getDomain()) : client.getConnectedResource().asBareJid();
        final ServiceDiscoveryManager serviceDiscoveryManager = client.getManager(ServiceDiscoveryManager.class);
        try {
            Set<String> features = serviceDiscoveryManager.discoverInformation(target).getResult().getFeatures();
            return TestUtils.hasAnyone(getNamespaces(), features) ? true : false;
        } catch (XmppException e) {
            return false;
        }
    }
}
