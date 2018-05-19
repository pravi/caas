package im.conversations.compliance.web;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import im.conversations.compliance.persistence.ServerStore;
import im.conversations.compliance.pojo.Credential;
import im.conversations.compliance.pojo.PostResponse;
import im.conversations.compliance.xmpp.CredentialVerifier;
import spark.ModelAndView;
import spark.Route;
import spark.TemplateViewRoute;

import java.util.HashMap;
import java.util.List;

import static spark.Spark.halt;

public class Controller {

    private static final GsonBuilder gsonBuilder = new GsonBuilder();
    private static final Gson gson = gsonBuilder.create();

    public static TemplateViewRoute getAdd = ((request, response) -> {
        HashMap<String, Object> model = new HashMap<String, Object>();
        return new ModelAndView(model, "add.ftl");
    });

    public static Route postAdd = (request, response) -> {
        String jid = request.queryParams("jid");
        String password = request.queryParams("password");
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

        // Make sure credential is not already present
        List<Credential> credentials = ServerStore.INSTANCE.getCredentials();
        if (credentials != null && credentials.stream().anyMatch(it -> it.getJid().toString().equals(credential.getJid().asBareJid().toString()))) {
            postResponse = new PostResponse(
                    false,
                    "ERROR: Credentials already exist in the database",
                    null);
            return gson.toJson(postResponse);
        }

        //TODO: Check if domain exists, if not add to domains table

        // Verify credentials
        boolean verified = true;
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

        // Add credentials to database
        boolean status = false;
        try {
            ServerStore.INSTANCE.addCredential(credential);
        } catch (Exception ex) {
            ex.printStackTrace();
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
