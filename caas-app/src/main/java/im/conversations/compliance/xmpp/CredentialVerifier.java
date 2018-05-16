package im.conversations.compliance.xmpp;

import im.conversations.compliance.pojo.Credential;
import rocks.xmpp.addr.Jid;
import rocks.xmpp.core.XmppException;
import rocks.xmpp.core.session.XmppClient;
import rocks.xmpp.core.session.XmppSessionConfiguration;

import java.time.Duration;

public class CredentialVerifier {
    public static boolean verifyCredential(Credential credentials) {
        XmppSessionConfiguration xmppSessionConfiguration = XmppSessionConfiguration.builder()
                .defaultResponseTimeout(Duration.ofSeconds(10))
                .build();
        Jid jid;
        // Handles invalid credentials gracefully rather than throwing Internal Server error
        try {
            jid = credentials.getJid();
        } catch (Exception ex) {
            return false;
        }
        String password = credentials.getPassword();
        try (XmppClient xmppClient = XmppClient.create(jid.getDomain(), xmppSessionConfiguration)) {
            xmppClient.connect();
            xmppClient.login(jid.getLocal(), password);
        } catch (XmppException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
