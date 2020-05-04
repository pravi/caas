package im.conversations.compliance.xmpp;

import im.conversations.compliance.pojo.Credential;
import rocks.xmpp.addr.Jid;
import rocks.xmpp.core.XmppException;
import rocks.xmpp.core.session.XmppClient;
import rocks.xmpp.core.session.XmppSessionConfiguration;
import rocks.xmpp.extensions.caps.EntityCapabilitiesManager;

import java.time.Duration;

public class CredentialVerifier {
    private static final XmppSessionConfiguration xmppSessionConfiguration = XmppSessionConfiguration.builder()
            .defaultResponseTimeout(Duration.ofSeconds(10))
            .build();
    public static boolean verifyCredential(Credential credentials) {
        Jid jid;
        // Handles invalid credentials gracefully rather than throwing Internal Server error
        try {
            jid = credentials.getJid();
        } catch (Exception ex) {
            return false;
        }
        String password = credentials.getPassword();
        try (XmppClient xmppClient = XmppClient.create(jid.getDomain(), xmppSessionConfiguration)) {
            xmppClient.getManager(EntityCapabilitiesManager.class).setEnabled(false);
            xmppClient.connect();
            xmppClient.login(jid.getLocal(), password);
        } catch (XmppException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
