package im.conversations.compliance.pojo;

import java.util.List;

public class TestHelp {
    private String name;
    private boolean possible;
    private String since;
    private String instructions;
    private List<Module> modulesRequired;

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

    public void setName(String name) {
        this.name = name;
    }

    public boolean isPossible() {
        return possible;
    }

    public void setPossible(boolean possible) {
        this.possible = possible;
    }

    public String getSince() {
        return since;
    }

    public void setSince(String since) {
        this.since = since;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    public List<Module> getModulesRequired() {
        return modulesRequired;
    }

    public void setModulesRequired(List<Module> modulesRequired) {
        this.modulesRequired = modulesRequired;
    }
}
