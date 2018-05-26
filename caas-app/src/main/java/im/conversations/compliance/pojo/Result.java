package im.conversations.compliance.pojo;

import im.conversations.compliance.annotations.ComplianceTest;

public class Result {
    private final ComplianceTest test;
    private final boolean success;

    public Result(ComplianceTest test, boolean success) {
        this.test = test;
        this.success = success;
    }

    public ComplianceTest getTest() {
        return test;
    }

    public boolean isSuccess() {
        return success;
    }
}
