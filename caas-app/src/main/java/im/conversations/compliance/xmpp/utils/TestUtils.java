package im.conversations.compliance.xmpp.utils;

import im.conversations.compliance.annotations.ComplianceTest;
import im.conversations.compliance.xmpp.Tests;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class TestUtils {
    private static Map<String, ComplianceTest> complianceTestMap =
            Tests.getTests()
                    .stream()
                    .map(it -> it.getAnnotation(ComplianceTest.class))
                    .collect(Collectors.toMap(ComplianceTest::short_name, Function.identity()));

    public static boolean hasAnyone(List<String> needles, Set<String> haystack) {
        for (String needle : needles) {
            if (haystack.contains(needle)) {
                return true;
            }
        }
        return false;
    }

    public static ComplianceTest getTestFrom(String shortName) {
        return complianceTestMap.get(shortName);
    }
}
