package im.conversations.compliance.sql2o;

import org.sql2o.converters.Converter;
import org.sql2o.converters.ConverterException;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;

public class InstantConverter implements Converter<Instant> {
    @Override
    public Instant convert(Object o) throws ConverterException {
        if (o instanceof Long) {
            return Instant.ofEpochMilli((Long) o);
        } else if (o instanceof Timestamp) {
            return ((Timestamp) o).toInstant();
        } else if (o instanceof String) {
            try {
                return Instant.parse((String) o);
            } catch (DateTimeParseException e) {
                throw new ConverterException(e.getMessage());
            }
        }
        throw new ConverterException("can not convert object of type " + o.getClass().getName() + " to Instant");
    }

    @Override
    public Timestamp toDatabaseParam(Instant val) {
        return Timestamp.from(val);
    }
}
