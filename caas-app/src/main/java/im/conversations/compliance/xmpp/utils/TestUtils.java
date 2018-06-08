package im.conversations.compliance.xmpp.utils;

import im.conversations.compliance.annotations.ComplianceTest;
import im.conversations.compliance.xmpp.Tests;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class TestUtils {
    private static List<String> allComplianceTestNames =
            Tests.getTests()
                    .stream()
                    .map(it -> it.getAnnotation(ComplianceTest.class).short_name())
                    .collect(Collectors.toList());

    private static List<String> complianceTestNames =
            Tests.getTests()
                    .stream()
                    .map(it -> it.getAnnotation(ComplianceTest.class))
                    .filter(it -> !it.informational())
                    .map(ComplianceTest::short_name)
                    .collect(Collectors.toList());

    private static Map<String, ComplianceTest> complianceTestMap =
            Collections.unmodifiableMap(
                    Tests.getTests()
                            .stream()
                            .map(it -> it.getAnnotation(ComplianceTest.class))
                            .collect(Collectors.toMap(ComplianceTest::short_name, Function.identity()))
            );

    /**
     * Get short names of tests which aren't informational in nature
     * @return list of short names of tests
     */
    public static List<String> getTestNames() {
        return Collections.unmodifiableList(complianceTestNames);
    }

    /**
     * Get short names of all the tests irrespective of whether they are informational in nature
     * @return list of short names of tests
     */
    public static List<String> getAllComplianceTestNames() {
        return Collections.unmodifiableList(allComplianceTestNames);
    }

    public static ComplianceTest getTestFrom(String shortName) {
        return complianceTestMap.get(shortName);
    }

    public static boolean hasAnyone(List<String> needles, Set<String> haystack) {
        for (String needle : needles) {
            if (haystack.contains(needle)) {
                return true;
            }
        }
        return false;
    }
}
