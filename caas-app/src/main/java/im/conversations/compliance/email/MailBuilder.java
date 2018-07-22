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

    private static Email buildEmail(String from, String to, String subject, String html) {
        Email email = EmailBuilder.startingBlank()
                .from("XMPP Compliance Tester", from)
                .to(to)
                .withSubject(subject)
                .withHTMLText(html)
                .buildEmail();
        return email;
    }

    public Email buildVerificationEmail(String to, String code, String domain) {
        StringWriter stringWriter = new StringWriter();
        try {
            configuration.getTemplate("verification.ftl")
                    .process(new HashMap<String, String>() {
                        {
                            put("code", code);
                            put("rootUrl", rootUrl);
                            put("domain", domain);
                        }
                    }, stringWriter);
        } catch (TemplateException | IOException e) {
            e.printStackTrace();
        }
        String message = stringWriter.toString();
        return buildEmail(from, to, "Verify your E-Mail address", message);
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
            StringWriter stringWriter = new StringWriter();
            try {
                model.put("subscriber", subscriber);
                configuration.getTemplate("change_report.ftl").process(model, stringWriter);
            } catch (TemplateException | IOException e) {
                e.printStackTrace();
            }
            String message = stringWriter.toString();
            emails.add(
                    buildEmail(
                            from,
                            subscriber.getEmail(),
                            "Changes in " + domain + "'s XMPP compliance results",
                            message
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
            StringWriter stringWriter = new StringWriter();
            try {
                model.put("subscriber", subscriber);
                configuration.getTemplate("authentication_failed.ftl").process(model, stringWriter);
            } catch (TemplateException | IOException e) {
                e.printStackTrace();
            }
            String message = stringWriter.toString();
            emails.add(
                    buildEmail(
                            from,
                            subscriber.getEmail(),
                            "Authentication failed for " + credential.getJid().toString(),
                            message
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
            StringWriter stringWriter = new StringWriter();
            try {
                model.put("subscriber", subscriber);
                configuration.getTemplate("results_unavailable.ftl").process(model, stringWriter);
            } catch (TemplateException | IOException e) {
                e.printStackTrace();
            }
            String message = stringWriter.toString();
            emails.add(
                    buildEmail(
                            from,
                            subscriber.getEmail(),
                            "Error while running Compliance Tester for " + domain,
                            message
                    )
            );
        }
        return emails;
    }
}
