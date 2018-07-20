package im.conversations.compliance.email;

import im.conversations.compliance.pojo.Configuration;
import org.simplejavamail.email.Email;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.UUID;

public class MailVerification {
    private static HashMap<String, VerificationRequest> verificationRequests = new HashMap<>();

    public static boolean addEmailToList(String address, String domain) {
        String code = UUID.randomUUID().toString();
        Instant expirationTime = Instant.now().plus(1, ChronoUnit.DAYS);
        verificationRequests.put(code, new VerificationRequest(address, domain, expirationTime));
        String from = Configuration.getInstance().getMailConfig().getFrom();
        Email email = MailBuilder.getInstance().buildVerificationEmail(address, code);
        MailSender.sendMail(email);
        return true;
    }

    public static String verifyEmail(String code) {
        VerificationRequest request = verificationRequests.remove(code);
        if (request != null) {
            Instant timestamp = Instant.now();
            if (timestamp.isBefore(request.verificationTimeout)) {
                //TODO: Add to database
                return "Subscribed " + request.getEmail() + " to compliance reports for " + request.getDomain();
            }
            return "Verification request timed out";
        }
        return "Invalid confirmation code";
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
