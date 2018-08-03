package im.conversations.compliance.pojo;

import rocks.xmpp.addr.Jid;

public class Credential {

    private final String domain;
    private final Jid jid;
    private final String password;

    public Credential(Jid jid, String password) {
        this.jid = jid.asBareJid();
        this.domain = jid.getDomain();
        this.password = password;
    }

    public Credential(String jidString, String password) {
        this(Jid.of(jidString), password);
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

    @Override
    public boolean equals(Object o) {
        if (o instanceof Credential) {
            Credential c = (Credential) o;
            return jid.equals(c.getJid()) && password.equals(c.getPassword());
        }
        return false;
    }
}
