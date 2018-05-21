package im.conversations.compliance.sql2o;


import im.conversations.compliance.annotations.ComplianceTest;
import im.conversations.compliance.xmpp.Tests;
import org.sql2o.converters.Converter;
import org.sql2o.converters.ConverterException;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ComplianceTestConverter implements Converter<ComplianceTest> {

    private static Map<String, ComplianceTest> complianceTestMap =
            Tests.getTests()
                    .stream()
                    .map(it -> it.getAnnotation(ComplianceTest.class))
                    .collect(Collectors.toMap(ComplianceTest::short_name, Function.identity()));

    @Override
    public ComplianceTest convert(Object o) throws ConverterException {
        if (o == null) {
            return null;
        } else if (o instanceof String) {
            try {
                if (complianceTestMap.containsKey(o))
                    return complianceTestMap.get(o);
                else throw new Exception();
            } catch (Exception ex) {
                throw new ConverterException("Unable to convert " + o.toString(), ex);
            }
        } else {
            throw new ConverterException("can not convert object of type " + o.getClass().getName() + " to Jid");
        }
    }

    @Override
    public Object toDatabaseParam(ComplianceTest ct) {
        return ct.short_name();
    }
}
