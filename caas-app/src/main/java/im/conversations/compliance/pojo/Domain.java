package im.conversations.compliance.pojo;

public class Domain {
    private String domain;
    private boolean listed;

    public Domain(String domain, boolean listed) {
        this.domain = domain;
        this.listed = listed;
    }
    public String getDomain() {
        return domain;
    }
    public boolean getListed() {
        return listed;
    }
}
