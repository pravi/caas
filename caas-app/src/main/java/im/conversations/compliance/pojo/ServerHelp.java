package im.conversations.compliance.pojo;

import java.util.List;

public class ServerHelp {
    private String serverName;
    private List<TestHelp> testsHelp;

    public ServerHelp(String serverName, List<TestHelp> implementationHelp) {
        this.serverName = serverName;
        this.testsHelp = implementationHelp;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public List<TestHelp> getTestsHelp() {
        return testsHelp;
    }

    public void setTestsHelp(List<TestHelp> testsHelp) {
        this.testsHelp = testsHelp;
    }
}
