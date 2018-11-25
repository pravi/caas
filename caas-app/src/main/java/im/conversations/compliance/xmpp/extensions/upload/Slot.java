package im.conversations.compliance.xmpp.extensions.upload;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="slot")
public class Slot {

    private Put put;
    private Get get;

    public Put getPut() {
        return put;
    }

    public Get getGet() {
        return get;
    }
}
