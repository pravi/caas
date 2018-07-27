package im.conversations.compliance;

import im.conversations.compliance.pojo.Help;
import im.conversations.compliance.xmpp.utils.TestUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class HelpsTest {
    List<String> tests;

    @Before
    public void initHelp() {
        tests = TestUtils.getAllTestNames();
    }

    @Test
    public void validHelpExistsForEjabberd() {
        checkHelpFile("ejabberd");
    }

    @Test
    public void validHelpExistsForProsody() {
        checkHelpFile("prosody");
        checkHelpFile("Prosody");
    }

    @Test
    public void validHelpExistsForOpenfire() {
        checkHelpFile("openfire");
        checkHelpFile("Openfire");
    }

    void checkHelpFile(String server) {
        HashMap<String, String> help = Help.getInstance().getHelpFor(server).get();
        List<String> testsForWhichHelpAvailable = help.keySet()
                .stream()
                .sorted()
                .collect(Collectors.toList());
        Assert.assertEquals(tests, testsForWhichHelpAvailable);
    }

}
