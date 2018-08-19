package im.conversations.compliance.xmpp.tests;

import de.measite.minidns.hla.ResolverApi;
import de.measite.minidns.hla.ResolverResult;
import de.measite.minidns.record.SRV;
import im.conversations.compliance.annotations.ComplianceTest;
import im.conversations.compliance.xmpp.XmppDomainVerifier;
import rocks.xmpp.core.session.XmppClient;

import javax.net.ssl.SNIHostName;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.util.Collections;

@ComplianceTest(
        short_name = "xep0368",
        full_name = "XEP-0368: SRV records for XMPP over TLS",
        url = "https://xmpp.org/extensions/xep-0368.html",
        description = "Provides users a virtually zero overhead way to bypass restrictive firewalls " +
                "that only allow HTTP over port 80 and HTTPS over port 443. " +
                "For servers, it allows multiple services to run on the same port."
)
public class XmppOverTls extends AbstractTest {

    public XmppOverTls(XmppClient client) {
        super(client);
    }

    @Override
    public boolean run() {
        final String domain = client.getDomain().getDomain();
        final SSLParameters parameters = new SSLParameters();
        parameters.setServerNames(Collections.singletonList(new SNIHostName(domain)));
        parameters.setApplicationProtocols(new String[]{"xmpp-client"});
        try {
            final SSLSocketFactory sslSocketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
            ResolverResult<SRV> results = ResolverApi.INSTANCE.resolve("_xmpps-client._tcp." + domain, SRV.class);
            for (SRV record : results.getAnswers()) {
                try {
                    SSLSocket socket = (SSLSocket) sslSocketFactory.createSocket(record.name.toString(), record.port);
                    socket.setSSLParameters(parameters);
                    socket.setSoTimeout(1000);
                    socket.startHandshake();
                    final boolean result;
                    result = XmppDomainVerifier.getInstance().verify(domain, socket.getSession());
                    socket.close();
                    return result;
                } catch (IOException e) {
                    //ignored
                }
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }
}
