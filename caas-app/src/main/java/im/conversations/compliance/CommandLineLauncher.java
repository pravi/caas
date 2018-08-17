package im.conversations.compliance;


import im.conversations.compliance.pojo.Credential;
import im.conversations.compliance.pojo.Result;
import im.conversations.compliance.xmpp.TestExecutor;
import im.conversations.compliance.xmpp.TestFactory;
import rocks.xmpp.addr.Jid;
import rocks.xmpp.core.XmppException;
import rocks.xmpp.core.session.XmppClient;
import rocks.xmpp.extensions.version.SoftwareVersionManager;
import rocks.xmpp.extensions.version.model.SoftwareVersion;

import java.util.*;
import java.util.stream.Collectors;

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
            Map<Boolean, List<Result>> results = TestExecutor.executeTestsFor(credential, (client -> {
                final Optional<SoftwareVersion> version = getSoftwareVersion(client);
                if (version.isPresent()) {
                    System.out.println("Server is running " + version.get().getName() + " " + version.get().getVersion());
                } else {
                    System.out.println("Server is running unknown software");
                }
            })).stream().collect(Collectors.partitioningBy(r -> r.getTest().informational()));
            System.out.println("\nCompliance report for " + credential.getDomain());
            int padding = Collections.max(results.values().stream().flatMap(List::stream).collect(Collectors.toList()), Comparator.comparing(r -> r.getTest().full_name().length())).getTest().full_name().length() + 1;
            for (Result result : results.get(false)) {
                print(result, padding);
            }
            System.out.println("\nInformational tests:");
            for (Result result : results.get(true)) {
                print(result, padding);
            }
        } catch (TestFactory.TestCreationException | XmppException e) {
            e.printStackTrace();
        }
    }

    private static Optional<SoftwareVersion> getSoftwareVersion(XmppClient client) {
        try {
            return Optional.ofNullable(client.getManager(SoftwareVersionManager.class).getSoftwareVersion(client.getDomain()).getResult());
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private static void print(Result result, int padding) {
        System.out.println(String.format("%1$-" + padding + "s", result.getTest().full_name() + " ") + (result.isSuccess() ? "\u001B[32mPASSED\u001B[0m" : "\u001B[31mFAILED\u001B[0m"));
    }
}
