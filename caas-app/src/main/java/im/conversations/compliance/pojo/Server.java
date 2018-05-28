package im.conversations.compliance.pojo;

import rocks.xmpp.extensions.version.model.SoftwareVersion;

public class Server {
    private final String domain;
    private String softwareName;
    private String softwareVersion;
    private final boolean listed;

    public Server(String domain, String softwareName, String softwareVersion, boolean listed) {
        this.domain = domain;
        this.softwareName = softwareName;
        this.softwareVersion = softwareVersion;
        this.listed = listed;
    }

    public Server(String domain, boolean listed) {
        this.domain = domain;
        this.listed = listed;
    }

    public Server(Server server, SoftwareVersion softwareVersion) {
        this(server.domain, softwareVersion.getName(), softwareVersion.getVersion(), server.listed);
    }

    public String getDomain() {
        return domain;
    }

    public String getSoftwareName() {
        return softwareName;
    }

    public String getSoftwareVersion() {
        return softwareVersion;
    }

    public boolean isListed() {
        return listed;
    }
}
