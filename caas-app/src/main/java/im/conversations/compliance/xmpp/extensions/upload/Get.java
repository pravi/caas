package im.conversations.compliance.xmpp.extensions.upload;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.net.URL;

@XmlRootElement(name="get")
public class Get {

    @XmlAttribute
    private URL url;
}
