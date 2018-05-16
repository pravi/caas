package im.conversations.compliance.xmpp.tests;

import rocks.xmpp.core.session.XmppClient;
import rocks.xmpp.core.stream.StreamFeaturesManager;
import rocks.xmpp.core.stream.model.StreamFeature;

import java.util.Map;

public abstract class AbstractStreamFeatureTest extends AbstractTest {

    public AbstractStreamFeatureTest(XmppClient client) {
        super(client);
    }

    @Override
    public boolean run() {
        Map<Class<? extends StreamFeature>, StreamFeature> features = client.getManager(StreamFeaturesManager.class).getFeatures();
        if (features.containsKey(getStreamFeature())) {
            return true;
        } else {
            return false;
        }
    }

    abstract Class<? extends StreamFeature> getStreamFeature();
}
