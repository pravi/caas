package im.conversations.compliance.xrd;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@XmlRootElement(name = "XRD")
public class ExtensibleResourceDescriptor {

    static final String NAMESPACE = "http://docs.oasis-open.org/ns/xri/xrd-1.0";

    @XmlElement(name = "Link")
    private List<Link> links = null;

    public List<Link> getLinks(String... rel) {
        List<String> list = Arrays.asList(rel);
        return getLinks(list);
    }

    public List<Link> getLinks(List<String> rels) {
        return getLinks().stream().filter(l -> rels.stream().anyMatch(r -> r.equals(l.getRel()))).collect(Collectors.toList());
    }

    private List<Link> getLinks() {
        return links == null ? Collections.emptyList() : links;
    }
}
