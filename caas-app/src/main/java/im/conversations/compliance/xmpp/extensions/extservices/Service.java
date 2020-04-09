package im.conversations.compliance.xmpp.extensions.extservices;

import javax.xml.bind.annotation.XmlAttribute;

public class Service {

    @XmlAttribute
    private String host;

    @XmlAttribute
    private int port;

    @XmlAttribute
    private String transport;

    @XmlAttribute
    private String type;

    @XmlAttribute
    private String username;

    @XmlAttribute
    private String password;

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getTransport() {
        return transport;
    }

    public String getType() {
        return type;
    }
}
