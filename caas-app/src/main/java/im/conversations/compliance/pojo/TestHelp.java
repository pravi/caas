package im.conversations.compliance.pojo;

import java.util.List;

public class TestHelp {
    private final String name;
    private final boolean possible;
    private final String since;
    private final String instructions;
    private final List<Module> modulesRequired;

    public TestHelp(String name, boolean possible, String since, String instructions, List<Module> modulesRequired) {
        this.name = name;
        this.possible = possible;
        this.since = since;
        this.instructions = instructions;
        this.modulesRequired = modulesRequired;
    }

    public String getName() {
        return name;
    }

    public boolean isPossible() {
        return possible;
    }

    public String getSince() {
        return since;
    }

    public String getInstructions() {
        return instructions;
    }

    public List<Module> getModulesRequired() {
        return modulesRequired;
    }
}
