package im.conversations.compliance.pojo;

import rocks.xmpp.addr.Jid;

public class Credential {

    private final String domain;
    private final Jid jid;
    private final String password;

    public Credential(String domain, Jid jid, String password) {
        this.jid = jid;
        this.password = password;
        this.domain = domain;
    }

    public Credential(String jidString, String password) {
        this.jid = Jid.of(jidString);
        this.password = password;
        this.domain = jid.getDomain();
    }

    public String getDomain() {
        return domain;
    }

    public Jid getJid() {
        return jid;
    }

    public String getPassword() {
        return password;
    }
}
