package im.conversations.compliance.sql2o;

import com.google.auto.service.AutoService;
import org.sql2o.converters.Converter;
import org.sql2o.converters.ConvertersProvider;
import rocks.xmpp.addr.Jid;

import java.time.Instant;
import java.util.Map;

@AutoService(org.sql2o.converters.ConvertersProvider.class)
public class Sql2oConverterProviders implements ConvertersProvider {
    @Override
    public void fill(Map<Class<?>, Converter<?>> mapToFill) {
        mapToFill.put(Jid.class, new JidConverter());
        mapToFill.put(Instant.class, new InstantConverter());
    }
}
