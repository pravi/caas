package im.conversations.compliance;

import im.conversations.compliance.annotations.ComplianceTest;
import im.conversations.compliance.xmpp.Tests;
import org.apache.commons.validator.routines.UrlValidator;
import org.junit.Assert;
import org.junit.Test;

public class TestMetadataTest {
    @Test
    public void checkValidUrlInTests() {
        UrlValidator validator = new UrlValidator();
        Tests.getTests().stream()
                .map(testClass -> testClass.getAnnotation(ComplianceTest.class))
                .forEach(
                        test -> Assert.assertTrue(validator.isValid(test.url()))
                );
    }
}
