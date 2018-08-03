package im.conversations.compliance.sql2o;

import org.sql2o.converters.Converter;
import org.sql2o.converters.ConverterException;

import java.time.Instant;
import java.time.format.DateTimeParseException;

public class InstantConverter implements Converter<Instant> {
    @Override
    public Instant convert(Object val) throws ConverterException {
        try {
            if (val instanceof String) {
                return Instant.parse((String) val);
            }
        } catch (DateTimeParseException ex) {
            throw new ConverterException(ex.getMessage());
        }
        throw new ConverterException("can not convert object of type " + val.getClass().getName() + " to Instant");
    }

    @Override
    public Object toDatabaseParam(Instant val) {
        return val.toString();
    }
}
