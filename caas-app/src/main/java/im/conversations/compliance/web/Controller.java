package im.conversations.compliance.web;

import im.conversations.compliance.persistence.ServerStore;
import im.conversations.compliance.pojo.Credential;
import im.conversations.compliance.xmpp.CredentialVerifier;
import spark.ModelAndView;
import spark.TemplateViewRoute;

import java.util.HashMap;

import static spark.Spark.halt;

public class Controller {
    public static TemplateViewRoute getAdd = ((request, response) -> {
        HashMap<String, Object> model = new HashMap<String, Object>();
        return new ModelAndView(model, "add.ftl");
    });
    public static TemplateViewRoute postAdd = (request, response) -> {
        String jid = request.queryParams("jid");
        String password = request.queryParams("password");
        Credential credentials = new Credential(jid, password);

        // Check for existence of domain in database
        String domain = credentials.getJid().getDomain();
        boolean domainExists = ServerStore.INSTANCE.getCredentials().stream()
                .map(c -> c.getJid().getDomain())
                .anyMatch(c -> c.equals(domain));
        if (domainExists) {
            halt(400, "<p>ERROR: Domain already exists. Click <a href=\"/" + domain + "\">here</a> to check its result</p>");
        }

        // Verify credentials
        boolean verified = true;
        try {
            verified = CredentialVerifier.verifyCredential(credentials);
        } catch (Exception ex) {
            verified = false;
            ex.printStackTrace();
        }
        if (!verified) {
            halt(400, "ERROR: Invalid credentials provided");
        }

        // Add credentials to database
        boolean status = ServerStore.INSTANCE.addCredential(credentials);
        if (status) {
            response.redirect("/live/" + credentials.getJid().getDomain());
        } else {
            halt(400, "ERROR: Could not add server with the provided credentials to the database");
        }
        return null;
    };
}
