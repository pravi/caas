package im.conversations.compliance;

import im.conversations.compliance.annotations.ComplianceTest;
import im.conversations.compliance.pojo.Help;
import im.conversations.compliance.pojo.ServerHelp;
import im.conversations.compliance.pojo.TestHelp;
import im.conversations.compliance.xmpp.Tests;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.stream.Collectors;

public class HelpExistsTest {
    List<String> tests;

    @Before
    public void initHelp() {
        tests = Tests.getTests()
                .stream()
                .map(test -> test.getAnnotation(ComplianceTest.class).short_name())
                .sorted()
                .collect(Collectors.toList());
    }

    @Test
    public void helpExistsForEjabberd() {
        checkHelpFile("ejabberd");
    }

    @Test
    public void helpExistsForProsody() {
        checkHelpFile("prosody");
    }

    void checkHelpFile(String server) {
        ServerHelp help = Help.getInstance().getHelpFor(server).get();
        List<String> testsForWhichHelpAvailable = help.getTestsHelp()
                .stream()
                .map(TestHelp::getName)
                .sorted()
                .collect(Collectors.toList());
        Assert.assertEquals(tests, testsForWhichHelpAvailable);
    }
}
