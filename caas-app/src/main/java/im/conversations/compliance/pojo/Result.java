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

    @Override
    public boolean equals(Object object) {
        if (object instanceof Result) {
            Result result = (Result) object;
            return result.isSuccess() == success &&
                    result.getTest().short_name().equals(test.short_name());
        }
        return false;
    }
}
