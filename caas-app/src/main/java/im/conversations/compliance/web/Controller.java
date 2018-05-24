package im.conversations.compliance.web;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import im.conversations.compliance.persistence.ServerStore;
import im.conversations.compliance.pojo.Credential;
import im.conversations.compliance.pojo.Domain;
import im.conversations.compliance.pojo.PostResponse;
import im.conversations.compliance.xmpp.CredentialVerifier;
import spark.ModelAndView;
import spark.Route;
import spark.TemplateViewRoute;

import java.util.HashMap;
import java.util.List;
import java.util.function.Predicate;

import static spark.Spark.halt;

public class Controller {

    private static final GsonBuilder gsonBuilder = new GsonBuilder();
    private static final Gson gson = gsonBuilder.create();

    public static TemplateViewRoute getAdd = ((request, response) -> {
        HashMap<String, Object> model = new HashMap<>();
        return new ModelAndView(model, "add.ftl");
    });

    public static Route postAdd = (request, response) -> {
        String jid = request.queryParams("jid");
        String password = request.queryParams("password");
        boolean listedServer = Boolean.parseBoolean(request.queryParams("listed"));
        final Credential credential;
        PostResponse postResponse;
        try {
            credential = new Credential(jid, password);
        } catch (Exception ex) {
            postResponse = new PostResponse(
                    false,
                    "ERROR: Invalid credentials provided",
                    null);
            return gson.toJson(postResponse);
        }

        List<Credential> credentials = ServerStore.INSTANCE.getCredentials();
        Predicate<Credential> jidMatches = it -> it.getJid().toString().equals(credential.getJid().toString());
        Predicate<Credential> passwordMatches = it -> it.getPassword().equals(credential.getPassword());
        if (credentials != null && credentials.stream().filter(jidMatches).anyMatch(passwordMatches)) {
            postResponse = new PostResponse(
                    false,
                    "ERROR: Credentials already exist in the database",
                    null);
            return gson.toJson(postResponse);
        }


        // Verify credentials
        boolean verified;
        try {
            verified = CredentialVerifier.verifyCredential(credential);
        } catch (Exception ex) {
            verified = false;
            ex.printStackTrace();
        }
        if (!verified) {
            postResponse = new PostResponse(
                    false,
                    "ERROR: Credentials could not be verified",
                    null);
            return gson.toJson(postResponse);
        }
        // If credentials are verified, and there was some old credential with the same jid, remove it
        else if (credentials != null) {
            Credential oldCredential = credentials.stream().filter(jidMatches).findFirst().orElse(null);
            if (oldCredential != null) {
                ServerStore.INSTANCE.removeCredential(oldCredential);
            }
        }

        //Check if domain exists, if not add to domains table
        boolean domainAdded = ServerStore.INSTANCE.addOrUpdateDomain(new Domain(credential.getDomain(), listedServer));
        if (!domainAdded) {
            postResponse = new PostResponse(
                    false,
                    "ERROR: Could not add/update domain to the database",
                    null
            );
            return gson.toJson(postResponse);
        }

        // Add credentials to database
        boolean dbAdded = ServerStore.INSTANCE.addCredential(credential);
        if (!dbAdded) {
            postResponse = new PostResponse(
                    false,
                    "ERROR: Could not add server with the provided credentials to the database",
                    null
            );
            return gson.toJson(postResponse);
        }

        postResponse = new PostResponse(
                true,
                "Successfully added credentials",
                "/live/" + credential.getDomain()
        );
        return gson.toJson(postResponse);
    };

    public static TemplateViewRoute getLive = (request, response) -> {
        //TODO: Implement this
        halt(500, "Not implemented yet");
        return null;
    };
}
