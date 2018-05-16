package im.conversations.compliance.xmpp.tests;

import rocks.xmpp.core.session.XmppClient;

public abstract class AbstractTest {

    protected final XmppClient client;

    public AbstractTest(XmppClient client) {
        this.client = client;
    }

    public abstract boolean run();
}
