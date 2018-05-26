package im.conversations.compliance.xmpp;


import im.conversations.compliance.annotations.ComplianceTest;
import im.conversations.compliance.pojo.Credential;
import im.conversations.compliance.xmpp.extensions.csi.ClientStateIndication;
import im.conversations.compliance.xmpp.tests.AbstractTest;
import rocks.xmpp.core.XmppException;
import rocks.xmpp.core.session.Extension;
import rocks.xmpp.core.session.XmppClient;
import rocks.xmpp.core.session.XmppSessionConfiguration;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;


public class TestExecutor {
    private static final XmppSessionConfiguration configuration;

    static {
        configuration = XmppSessionConfiguration.builder()
                .extensions(Extension.of(ClientStateIndication.class))
                .defaultResponseTimeout(Duration.ofSeconds(10))
                .initialPresence(null)
                .build();
    }

    public static List<Result> executeTestsFor(Credential credential) throws XmppException, TestFactory.TestCreationException {

        ArrayList<Result> results = new ArrayList<>();
        XmppClient client = XmppClient.create(credential.getDomain(), configuration);
        client.connect(credential.getJid());
        client.login(credential.getJid().getLocal(), credential.getPassword(),"caas");

        //Update server metadata
        ServerMetadataChecker.updateServerMetadataFor(client,credential);

        //Run tests
        List<Class<? extends AbstractTest>> testClasses = Tests.getTests();
        for (Class<? extends AbstractTest> testClass : testClasses) {
            ComplianceTest test = testClass.getAnnotation(ComplianceTest.class);
            Result result = new Result(test, TestFactory.create(testClass, client).run());
            results.add(result);
        }
        client.close();
        return results;
    }
}
