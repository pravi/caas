package im.conversations.compliance.pojo;

public class Module {
    private String name;
    private String type;
    private String link;

    public Module(String name, String type, String link) {
        this.name = name;
        this.type = type;
        this.link = link;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

}
