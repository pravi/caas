package im.conversations.compliance.email;

import im.conversations.compliance.persistence.DBOperations;
import im.conversations.compliance.pojo.Configuration;
import im.conversations.compliance.pojo.Subscriber;
import org.simplejavamail.email.Email;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class MailVerification {
    private static final HashMap<String, VerificationRequest> verificationRequests = new HashMap<>();

    public static boolean addEmailToList(String address, String domain) {
        String code = UUID.randomUUID().toString();
        Instant expirationTime = Instant.now().plus(1, ChronoUnit.DAYS);
        VerificationRequest verificationRequest = new VerificationRequest(address, domain, expirationTime);
        synchronized (verificationRequests) {
            verificationRequests.put(code, verificationRequest);
        }
        String from = Configuration.getInstance().getMailConfig().getFrom();
        Email email = MailBuilder.getInstance().buildVerificationEmail(address, code, domain);
        MailSender.sendMail(email);
        return true;
    }

    public static String verifyEmail(String code) {
        VerificationRequest request;
        synchronized (verificationRequests) {
            request = verificationRequests.remove(code);
        }
        if (request != null) {
            Instant timestamp = Instant.now();
            if (timestamp.isBefore(request.verificationTimeout)) {
                Subscriber subscriber = Subscriber.createSubscriber(request.email, request.domain);
                DBOperations.addSubscriber(subscriber);
                return "Subscribed " + request.getEmail() + " to compliance reports for " + request.getDomain();
            }
            return "Verification request timed out";
        }
        return "Invalid confirmation code";
    }

    public static void removeExpiredRequests() {
        Instant now = Instant.now();
        synchronized (verificationRequests) {
            for (Iterator<Map.Entry<String, VerificationRequest>> it = verificationRequests.entrySet().iterator(); it.hasNext(); ) {
                Map.Entry<String, VerificationRequest> next = it.next();
                VerificationRequest request = next.getValue();
                if (now.isAfter(request.getVerificationTimeout())) {
                    it.remove();
                }
            }
        }
    }

    static class VerificationRequest {
        String email;
        String domain;
        Instant verificationTimeout;

        public VerificationRequest(String email, String server, Instant verificationTimeout) {
            this.email = email;
            this.domain = server;
            this.verificationTimeout = verificationTimeout;
        }

        String getEmail() {
            return email;
        }

        String getDomain() {
            return domain;
        }

        Instant getVerificationTimeout() {
            return verificationTimeout;
        }
    }
}
