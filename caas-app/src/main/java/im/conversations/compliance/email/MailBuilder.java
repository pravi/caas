package im.conversations.compliance.email;

import freemarker.template.Configuration;
import freemarker.template.TemplateException;
import org.simplejavamail.email.Email;
import org.simplejavamail.email.EmailBuilder;
import spark.template.freemarker.FreeMarkerEngine;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;

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

    public Email buildVerificationEmail(String to, String code) {
        StringWriter stringWriter = new StringWriter();
        try {
            configuration.getTemplate("verification.ftl").
                    process(new HashMap<String, String>() {
                        {
                            put("code", code);
                            put("rootUrl", rootUrl);
                        }
                    }, stringWriter);
        } catch (TemplateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String message = stringWriter.toString();
        return buildEmail(from, to, "Verify your E-Mail address", message);
    }
}
