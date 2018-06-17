package im.conversations.compliance.xmpp;

import im.conversations.compliance.persistence.ServerStore;
import im.conversations.compliance.pojo.Credential;
import im.conversations.compliance.pojo.Server;
import rocks.xmpp.core.XmppException;
import rocks.xmpp.core.session.XmppClient;
import rocks.xmpp.core.session.XmppSessionConfiguration;
import rocks.xmpp.extensions.version.SoftwareVersionManager;
import rocks.xmpp.extensions.version.model.SoftwareVersion;

import java.time.Duration;

public class ServerMetadataChecker {
    private static final XmppSessionConfiguration configuration;

    static {
        configuration = XmppSessionConfiguration.builder()
                .defaultResponseTimeout(Duration.ofSeconds(10))
                .initialPresence(null)
                .build();
    }

    public static boolean updateServerMetadataFor(XmppClient xmppClient, Credential credential) {
        return addOrUpdateServer(xmppClient, ServerStore.getInstance().getServer(credential.getDomain()), credential);
    }

    public static boolean addServerMetadataFor(XmppClient xmppClient, Credential credential, boolean listed) {
        return addOrUpdateServer(xmppClient, new Server(credential.getDomain(), listed), credential);
    }

    private static boolean addOrUpdateServer(XmppClient xmppClient, Server server, Credential credential) {
        final SoftwareVersionManager softwareVersionManager = xmppClient.getManager(SoftwareVersionManager.class);
        try {
            SoftwareVersion softwareVersion = softwareVersionManager.getSoftwareVersion(xmppClient.getDomain()).getResult();
            if (softwareVersion == null) {
                ServerStore.getInstance().addOrUpdateServer(server);
                return true;
            }
            Server newServer = new Server(
                    credential.getDomain(),
                    softwareVersion.getName(),
                    softwareVersion.getVersion(),
                    server.isListed()
            );
            ServerStore.getInstance().addOrUpdateServer(newServer);
            return true;
        } catch (XmppException e) {
            e.printStackTrace();
            //No software version found
        }
        return false;
    }
}
