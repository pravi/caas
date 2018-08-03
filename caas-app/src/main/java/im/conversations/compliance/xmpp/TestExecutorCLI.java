package im.conversations.compliance.xmpp;

import im.conversations.compliance.annotations.ComplianceTest;
import im.conversations.compliance.pojo.Credential;
import im.conversations.compliance.pojo.Result;
import im.conversations.compliance.xmpp.extensions.csi.ClientStateIndication;
import im.conversations.compliance.xmpp.extensions.omemo.Bundle;
import im.conversations.compliance.xmpp.tests.AbstractTest;
import rocks.xmpp.core.XmppException;
import rocks.xmpp.core.session.Extension;
import rocks.xmpp.core.session.XmppClient;
import rocks.xmpp.core.session.XmppSessionConfiguration;
import rocks.xmpp.extensions.version.SoftwareVersionManager;
import rocks.xmpp.extensions.version.model.SoftwareVersion;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class TestExecutorCLI {
    private static final XmppSessionConfiguration configuration;

    static {
        configuration = XmppSessionConfiguration.builder()
                .extensions(Extension.of(ClientStateIndication.class, Bundle.class))
                .defaultResponseTimeout(Duration.ofSeconds(10))
                .initialPresence(null)
                .build();
    }

    public static void executeTestsFor(Credential credential) throws XmppException, TestFactory.TestCreationException {

        XmppClient client = XmppClient.create(credential.getDomain(), configuration);
        client.connect(credential.getJid());
        client.login(credential.getJid().getLocal(), credential.getPassword(), "caas");

        //Print server metadata
        final SoftwareVersionManager softwareVersionManager = client.getManager(SoftwareVersionManager.class);
        SoftwareVersion softwareVersion = softwareVersionManager.getSoftwareVersion(client.getDomain()).getResult();

        if (softwareVersion == null) {
            System.out.println("Server is running unknown software");
        } else {
            System.out.println("Server is running " + softwareVersion.getName() + " " + softwareVersion.getVersion());
        }

        //Run tests
        List<Result> results = new ArrayList<>();
        List<Class<? extends AbstractTest>> testClasses = Tests.getTests();
        for (Class<? extends AbstractTest> testClass : testClasses) {
            ComplianceTest test = testClass.getAnnotation(ComplianceTest.class);
            Result result = new Result(test, TestFactory.create(testClass, client).run());
            results.add(result);
            String passedText = result.isSuccess() ? "Passed" : "Failed";
            System.out.println(passedText + " " + test.full_name());
        }
        results.sort(Comparator.comparing(it -> it.getTest().short_name()));
        List<Result> testResults = results.stream()
                .filter(it -> !it.getTest().informational())
                .collect(Collectors.toList());
        List<Result> informationalResults = results.stream()
                .filter(it -> it.getTest().informational())
                .collect(Collectors.toList());
        System.out.println("\n\n\nCompliance report for " + credential.getDomain());
        for (Result result : testResults) {
            String passedText = result.isSuccess() ? "PASSED" : "FAILED";
            System.out.println(result.getTest().full_name() + ": " + passedText);
        }
        System.out.println("\nInformational tests:");
        for (Result result : informationalResults) {
            String passedText = result.isSuccess() ? "PASSED" : "FAILED";
            System.out.println(result.getTest().full_name() + ": " + passedText);
        }
        client.close();
    }
}
