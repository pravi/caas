package im.conversations.compliance.xmpp.extensions.upload;


import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "request")
public class Request {

    @XmlAttribute
    private String filename;

    @XmlAttribute
    private long size;

    @XmlAttribute(name = "content-type")
    private String contentType;

    private Request() {

    }

    public Request(String filename, long size, String contentType) {
        this.filename = filename;
        this.size = size;
        this.contentType = contentType;
    }

}
