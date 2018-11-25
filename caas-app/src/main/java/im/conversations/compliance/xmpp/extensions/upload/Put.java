package im.conversations.compliance.xmpp.extensions.upload;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.net.URL;

@XmlRootElement(name="put")
public class Put {

    @XmlAttribute
    private URL url;

    public URL getUrl() {
        return url;
    }
}
