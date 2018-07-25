package im.conversations.compliance.email;

import freemarker.template.Configuration;
import freemarker.template.TemplateException;
import im.conversations.compliance.persistence.DBOperations;
import im.conversations.compliance.pojo.Credential;
import im.conversations.compliance.pojo.HistoricalSnapshot;
import im.conversations.compliance.pojo.Iteration;
import im.conversations.compliance.pojo.Subscriber;
import im.conversations.compliance.utils.TimeUtils;
import im.conversations.compliance.xmpp.utils.TestUtils;
import org.simplejavamail.email.Email;
import org.simplejavamail.email.EmailBuilder;
import spark.template.freemarker.FreeMarkerEngine;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MailBuilder {
    private static Configuration configuration;
    private static MailBuilder INSTANCE;
    private String from, rootUrl;

    private MailBuilder(String from, String rootUrl) {
        this.from = from;
        this.rootUrl = rootUrl;
    }

    public static void init(String from, String rootUrl) {
        if (INSTANCE != null) {
            throw new IllegalStateException("MailBuilder has already been initialised");
        }
        if(rootUrl == null) {
            throw new IllegalArgumentException("rootUrl can't be null in configuration");
        }
        configuration = new Configuration(Configuration.VERSION_2_3_26);
        configuration.setClassForTemplateLoading(FreeMarkerEngine.class, "");
        INSTANCE = new MailBuilder(from, rootUrl);
    }

    public static MailBuilder getInstance() {
        return INSTANCE;
    }

    private static Email buildEmail(String from, String to, String subject, String html, String plainText) {
        Email email = EmailBuilder.startingBlank()
                .from("XMPP Compliance Tester", from)
                .to(to)
                .withSubject(subject)
                .withHTMLText(html)
                .withPlainText(plainText)
                .buildEmail();
        return email;
    }

    public Email buildVerificationEmail(String to, String code, String domain) {
        HashMap<String, Object> model = new HashMap<>();
        model.put("code", code);
        model.put("rootUrl", rootUrl);
        model.put("domain", domain);
        String html = getProcessedTemplate("emails/verification.ftl", model);
        String plainText = getProcessedTemplate("emails/verification_text.ftl", model);
        return buildEmail(from, to, "Verify your E-Mail address", html, plainText);
    }

    public List<Email> buildChangeEmails(HistoricalSnapshot.Change change, Iteration iteration, String domain) {
        List<Subscriber> subscribers = DBOperations.getSubscribersFor(domain);
        List<Email> emails = new ArrayList<>();
        HashMap<String, Object> model = new HashMap<>();
        model.put("change", change);
        model.put("tests", TestUtils.getComplianceTestMap());
        model.put("iteration", iteration);
        model.put("rootUrl", rootUrl);
        for (Subscriber subscriber : subscribers) {
            model.put("subscriber", subscriber);
            String html = getProcessedTemplate("emails/change_report.ftl", model);
            String plainText = getProcessedTemplate("emails/change_report_text.ftl", model);
            emails.add(
                    buildEmail(
                            from,
                            subscriber.getEmail(),
                            "Changes in " + domain + "'s XMPP compliance results",
                            html,
                            plainText
                    )
            );
        }
        return emails;
    }

    public List<Email> buildCredentialRemovalEmails(Credential credential) {
        List<Subscriber> subscribers = DBOperations.getSubscribersFor(credential.getDomain());
        List<Email> emails = new ArrayList<>();
        HashMap<String, Object> model = new HashMap<>();
        model.put("credential", credential);
        model.put("domain", credential.getDomain());
        model.put("rootUrl", rootUrl);

        for (Subscriber subscriber : subscribers) {
            model.put("subscriber", subscriber);
            String html = getProcessedTemplate("emails/authentication_failed.ftl", model);
            String plainText = getProcessedTemplate("emails/authentication_failed_text.ftl", model);
            emails.add(
                    buildEmail(
                            from,
                            subscriber.getEmail(),
                            "Authentication failed for " + credential.getJid().toString(),
                            html,
                            plainText
                    )
            );
        }
        return emails;
    }

    public List<Email> buildResultsNotAvailableMails(String domain, Iteration iteration) {
        List<Subscriber> subscribers = DBOperations.getSubscribersFor(domain);
        String timeSince = TimeUtils.getTimeSince(iteration.getBegin());
        List<Email> emails = new ArrayList<>();
        HashMap<String, Object> model = new HashMap<>();
        model.put("iteration", iteration);
        model.put("timeSince", timeSince);
        model.put("domain", domain);
        model.put("rootUrl", rootUrl);
        for (Subscriber subscriber : subscribers) {
            model.put("subscriber", subscriber);
            String html = getProcessedTemplate("emails/results_unavailable.ftl", model);
            String plainText = getProcessedTemplate("emails/results_unavailable_text.ftl", model);
            emails.add(
                    buildEmail(
                            from,
                            subscriber.getEmail(),
                            "Error while running Compliance Tester for " + domain,
                            html,
                            plainText
                    )
            );
        }
        return emails;
    }

    public List<Email> buildCredentialUpdateEmails(Credential credential) {
        List<Subscriber> subscribers = DBOperations.getSubscribersFor(credential.getDomain());
        List<Email> emails = new ArrayList<>();
        HashMap<String, Object> model = new HashMap<>();
        model.put("credential", credential);
        model.put("domain", credential.getDomain());
        model.put("rootUrl", rootUrl);

        for (Subscriber subscriber : subscribers) {
            model.put("subscriber", subscriber);
            String html = getProcessedTemplate("emails/new_credentials.ftl", model);
            String plainText = getProcessedTemplate("emails/new_credentials_text.ftl", model);
            emails.add(
                    buildEmail(
                            from,
                            subscriber.getEmail(),
                            "Credentials updated for " + credential.getDomain(),
                            html,
                            plainText
                    )
            );
        }
        return emails;
    }

    private String getProcessedTemplate(String templateName, HashMap<String, Object> model) {
        StringWriter stringWriter = new StringWriter();
        try {
            configuration.getTemplate(templateName).process(model, stringWriter);
        } catch (TemplateException | IOException e) {
            e.printStackTrace();
        }
        return stringWriter.toString();
    }

}
