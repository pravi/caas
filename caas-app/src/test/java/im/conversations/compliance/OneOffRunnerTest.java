package im.conversations.compliance;

import org.junit.Test;

public class OneOffRunnerTest {
    private static final String JDBC_URL = "jdbc:sqlite:";

    @Test(expected = NullPointerException.class)
    public void whenNoIterations() {
    }

}
