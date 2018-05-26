package im.conversations.compliance.pojo;

import java.util.List;

public class ServerHelp {
    private String softwareName;
    private List<TestHelp> testsHelp;

    public ServerHelp(String serverName, List<TestHelp> implementationHelp) {
        this.softwareName = serverName;
        this.testsHelp = implementationHelp;
    }

    public String getSoftwareName() {
        return softwareName;
    }

    public void setSoftwareName(String softwareName) {
        this.softwareName = softwareName;
    }

    public List<TestHelp> getTestsHelp() {
        return testsHelp;
    }

    public void setTestsHelp(List<TestHelp> testsHelp) {
        this.testsHelp = testsHelp;
    }
}
