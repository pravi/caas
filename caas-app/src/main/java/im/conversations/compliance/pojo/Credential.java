package im.conversations.compliance.pojo;

import rocks.xmpp.addr.Jid;

public class Credential {

    private final String domain;
    private final String jid;
    private final String password;

    public Credential(String jid, String password) {
        this.jid = jid;
        this.password = password;
        this.domain = Jid.of(jid).getDomain();
    }

    public String getDomain() {
        return domain;
    }

    public Jid getJid() {
        return Jid.of(jid);
    }

    public String getPassword() {
        return password;
    }
}
