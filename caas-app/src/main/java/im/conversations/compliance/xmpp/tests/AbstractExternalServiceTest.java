package im.conversations.compliance.xmpp.tests;

import im.conversations.compliance.xmpp.extensions.extservices.Service;
import im.conversations.compliance.xmpp.extensions.extservices.Services;
import rocks.xmpp.core.session.XmppClient;
import rocks.xmpp.core.stanza.model.IQ;

import java.util.List;

public abstract  class AbstractExternalServiceTest extends AbstractTest {
    public AbstractExternalServiceTest(XmppClient client) {
        super(client);
    }

    @Override
    public boolean run() {
        final IQ request = new IQ(IQ.Type.GET,new Services());
        request.setTo(client.getDomain());
        try {
            return test(client.query(request, Services.class).get().getServices());
        } catch (final Exception e) {
            return false;
        }
    }

    abstract boolean test(List<Service> services);
}
