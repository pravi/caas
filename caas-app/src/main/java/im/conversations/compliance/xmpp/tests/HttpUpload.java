package im.conversations.compliance.xmpp.tests;

import im.conversations.compliance.annotations.ComplianceTest;
import rocks.xmpp.core.session.XmppClient;

@ComplianceTest(
        short_name = "xep363",
        full_name = "XEP-0363: HTTP File Upload",
        url = "https://xmpp.org/extensions/xep-0363.html",
        description = "Provides a protocol for transfering files between entities by uploading the file to an HTTP server"
)
public class HttpUpload extends AbstractServiceTest {

    public HttpUpload(XmppClient client) {
        super(client);
    }

    @Override
    public String getNamespace() {
        return "urn:xmpp:http:upload:0";
    }
}
