package im.conversations.compliance.xrd;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.net.URI;

@XmlRootElement(name = "Link")
public class Link {

    @XmlAttribute
    private String rel;
    @XmlAttribute
    private URI href;


    private Link() {

    }

    public String getRel() {
        return rel;
    }

    public URI getHref() {
        return href;
    }
}

