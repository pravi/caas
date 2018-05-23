package im.conversations.compliance.sql2o;

import org.sql2o.converters.Converter;
import org.sql2o.converters.ConverterException;

import java.time.Instant;

public class InstantConverter implements Converter<Instant> {
    @Override
    public Instant convert(Object val) throws ConverterException {
        if (val instanceof String) {
            return Instant.parse((String) val);
        }
        return null;
    }

    @Override
    public Object toDatabaseParam(Instant val) {
        return val.toString();
    }
}
