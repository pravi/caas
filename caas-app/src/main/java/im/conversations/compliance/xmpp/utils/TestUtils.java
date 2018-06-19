package im.conversations.compliance.xmpp.utils;

import im.conversations.compliance.annotations.ComplianceTest;
import im.conversations.compliance.xmpp.Tests;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class TestUtils {
    private static List<ComplianceTest> allComplianceTests =
            Collections.unmodifiableList(
                    Tests.getTests()
                            .stream()
                            .map(it -> it.getAnnotation(ComplianceTest.class))
                            .sorted(Comparator.comparing(ComplianceTest::short_name))
                            .collect(Collectors.toList()));

    private static List<String> allComplianceTestNames =
            Collections.unmodifiableList(
                    allComplianceTests
                            .stream()
                            .map(ComplianceTest::short_name)
                            .collect(Collectors.toList()));

    private static List<String> complianceTestNames =
            Collections.unmodifiableList(
                    allComplianceTests
                            .stream()
                            .filter(it -> !it.informational())
                            .map(ComplianceTest::short_name)
                            .collect(Collectors.toList()));

    private static Map<String, ComplianceTest> complianceTestMap =
            Collections.unmodifiableMap(
                    allComplianceTests
                            .stream()
                            .collect(Collectors.toMap(ComplianceTest::short_name, Function.identity()))
            );

    /**
     * Get short names of tests which aren't informational in nature
     *
     * @return list of short names of tests
     */
    public static List<String> getTestNames() {
        return complianceTestNames;
    }

    /**
     * Get compliance test object of all tests
     *
     * @return A {@link List} of all {@link ComplianceTest}
     */
    public static List<ComplianceTest> getAllComplianceTests() {
        return allComplianceTests;
    }

    /**
     * Get short names of all the tests irrespective of whether they are informational in nature
     *
     * @return list of short names of tests
     */
    public static List<String> getAllTestNames() {
        return allComplianceTestNames;
    }

    /**
     * Get the {@link ComplianceTest} with the given short name, or null if none exists
     *
     * @param shortName
     * @return
     */
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
