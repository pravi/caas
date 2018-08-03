package im.conversations.compliance;


import im.conversations.compliance.pojo.Credential;
import im.conversations.compliance.xmpp.TestExecutorCLI;
import im.conversations.compliance.xmpp.TestFactory;
import rocks.xmpp.addr.Jid;
import rocks.xmpp.core.XmppException;

public class CommandLineLauncher {
    public static void main(String[] args) {
        String password;
        if (args.length < 1 || args.length > 2) {
            System.err.println("java -jar ComplianceTester.jar username@domain.tld [password]");
            System.exit(1);
            return;
        }
        Jid jid = Jid.of(args[0]);

        if (args.length == 2) {
            password = args[1];
            AccountStore.storePassword(jid, password);
        } else {
            String storedPassword = AccountStore.getPassword(jid);
            if (storedPassword != null) {
                password = storedPassword;
            } else {
                System.err.println("password for " + jid + " was not stored. trying to register");
                try {
                    password = RegistrationHelper.register(jid);
                    AccountStore.storePassword(jid, password);
                } catch (RegistrationHelper.RegistrationNotSupported e) {
                    System.err.println("server " + jid.getDomain() + " does not support registration");
                    System.exit(1);
                    return;
                } catch (RegistrationHelper.RegistrationFailed e) {
                    System.out.println("registration failed on server " + jid.getDomain() + " " + e.getMessage());
                    System.exit(1);
                    return;
                }

            }
        }
        Credential credential = new Credential(jid, password);
        try {
            TestExecutorCLI.executeTestsFor(credential);
        } catch (XmppException e) {
            e.printStackTrace();
        } catch (TestFactory.TestCreationException e) {
            e.printStackTrace();
        }
    }
}
