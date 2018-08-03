package im.conversations.compliance.pojo;

import java.time.Instant;
import java.util.UUID;

public class Subscriber {
    private String email;
    private String domain;
    private String unsubscribeCode;

    private Subscriber(String email, String domain, String unsubscribeCode) {
        this.email = email;
        this.domain = domain;
        this.unsubscribeCode = unsubscribeCode;
    }

    public static Subscriber createSubscriber(String email, String domain) {
        String code = Instant.now().toString() + UUID.randomUUID().toString();
        return new Subscriber(email, domain, code);
    }

    public String getEmail() {
        return email;
    }

    public String getDomain() {
        return domain;
    }

    public String getUnsubscribeCode() {
        return unsubscribeCode;
    }

    @Override
    public boolean equals(Object o) {
        if(o instanceof Subscriber) {
            Subscriber s = (Subscriber) o;
            return s.domain.equals(domain) && s.email.equals(email) && s.unsubscribeCode.equals(unsubscribeCode);
        }
        return false;
    }
}
