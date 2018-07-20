package im.conversations.compliance.email;

import im.conversations.compliance.pojo.Configuration;
import im.conversations.compliance.pojo.MailConfig;
import org.simplejavamail.email.Email;
import org.simplejavamail.mailer.Mailer;
import org.simplejavamail.mailer.MailerBuilder;
import org.simplejavamail.mailer.config.TransportStrategy;

public class MailSender {
    private static boolean init = false;
    private static MailConfig mailConfig;
    private static TransportStrategy transportStrategy;
    private static Mailer mailer;

    public static void init() {
        if (!init) {
            mailConfig = Configuration.getInstance().getMailConfig();
            transportStrategy = mailConfig.getSSL() ?
                    TransportStrategy.SMTP_TLS : TransportStrategy.SMTP;
            mailer = MailerBuilder.withSMTPServer(
                    mailConfig.getHost(),
                    mailConfig.getPort(),
                    mailConfig.getUsername(),
                    mailConfig.getPassword()
            )
                    .withTransportStrategy(transportStrategy)
                    .buildMailer();
        }
        init = true;
    }

    public static void sendMail(Email email) {
        init();
        mailer.sendMail(email);
    }

}
