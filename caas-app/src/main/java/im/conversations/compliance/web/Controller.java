package im.conversations.compliance.web;

import com.google.gson.Gson;
import im.conversations.compliance.annotations.ComplianceTest;
import im.conversations.compliance.persistence.ServerStore;
import im.conversations.compliance.persistence.TestResultStore;
import im.conversations.compliance.pojo.*;
import im.conversations.compliance.utils.JsonReader;
import im.conversations.compliance.utils.TimeUtils;
import im.conversations.compliance.xmpp.CredentialVerifier;
import im.conversations.compliance.xmpp.OneOffTestRunner;
import im.conversations.compliance.xmpp.utils.TestUtils;
import spark.ModelAndView;
import spark.Route;
import spark.TemplateViewRoute;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Controller {

    private static final Gson gson = JsonReader.gson;

    public static TemplateViewRoute getRoot = (request, response) -> {
        return new ModelAndView(null, "root.ftl");
    };

    public static TemplateViewRoute getTests = (request, response) -> {
        return new ModelAndView(null, "tests.ftl");
    };

    public static TemplateViewRoute getAbout = (request, response) -> {
        return new ModelAndView(null, "about.ftl");
    };

    public static TemplateViewRoute getAdd = ((request, response) -> {
        HashMap<String, Object> model = new HashMap<>();
        return new ModelAndView(model, "add.ftl");
    });

    public static Route postAdd = (request, response) -> {
        String jid = request.queryParams("jid");
        String password = request.queryParams("password");
        boolean listedServer = Boolean.parseBoolean(request.queryParams("listed"));
        final Credential credential;
        ServerResponse postResponse;
        try {
            credential = new Credential(jid, password);
        } catch (Exception ex) {
            postResponse = new ServerResponse(
                    false,
                    "ERROR: Invalid credentials provided",
                    null);
            return gson.toJson(postResponse);
        }

        List<Credential> credentials = ServerStore.INSTANCE.getCredentials();
        Predicate<Credential> jidMatches = it -> it.getJid().toString().equals(credential.getJid().toString());
        Predicate<Credential> passwordMatches = it -> it.getPassword().equals(credential.getPassword());
        if (credentials != null && credentials.stream().filter(jidMatches).anyMatch(passwordMatches)) {
            postResponse = new ServerResponse(
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
            postResponse = new ServerResponse(
                    false,
                    "ERROR: Credentials could not be verified",
                    null);
            return gson.toJson(postResponse);
        }
        // If credentials are verified, and there was some old credential with the same jid, remove it
        else if (credentials != null) {
            credentials.stream().filter(jidMatches).findFirst().ifPresent(ServerStore.INSTANCE::removeCredential);
        }

        //Check if domain exists, if not add to domains table
        boolean domainAdded = ServerStore.INSTANCE.addOrUpdateServer(new Server(credential.getDomain(), listedServer));
        if (!domainAdded) {
            postResponse = new ServerResponse(
                    false,
                    "ERROR: Could not add/update domain to the database",
                    null
            );
            return gson.toJson(postResponse);
        }

        // Add credentials to database
        boolean dbAdded = ServerStore.INSTANCE.addCredential(credential);
        if (!dbAdded) {
            postResponse = new ServerResponse(
                    false,
                    "ERROR: Could not add server with the provided credentials to the database",
                    null
            );
            return gson.toJson(postResponse);
        }

        postResponse = new ServerResponse(
                true,
                "Successfully added credentials",
                "/live/" + credential.getDomain()
        );
        return gson.toJson(postResponse);
    };

    public static TemplateViewRoute getLive = (request, response) -> {
        HashMap<String, Object> model = new HashMap<>();
        String domain = request.params("domain");
        Credential credential = ServerStore.INSTANCE.getCredentials()
                .stream()
                .filter(it -> it.getDomain().equals(domain))
                .findFirst().orElse(null);
        if (credential == null) {
            model.put("error_code", 404);
            model.put("error_msg", "No credentials for " + domain + " found in database");
            return new ModelAndView(model, "error.ftl");
        }
        OneOffTestRunner.runOneOffTestsFor(credential);
        model.put("domain", domain);
        return new ModelAndView(model, "live.ftl");
    };

    public static TemplateViewRoute getServer = (request, response) -> {
        HashMap<String, Object> model = new HashMap<>();
        String domain = request.params("domain");
        Server server = ServerStore.INSTANCE.getServer(domain);
        if (server == null) {
            model.put("error_code", 404);
            model.put("error_msg", "Credentials unavailable for " + domain);
            return new ModelAndView(model, "error.ftl");
        }
        List<Result> results;
        try {
            results = TestResultStore.INSTANCE.getResultsFor(domain);
        } catch (Exception ex) {
            model.put("error_code", 404);
            model.put("error_msg", "Results unavailable for " + domain + ". Tests might still be running");
            return new ModelAndView(model, "error.ftl");
        }
        List<String> failedTests = results.stream()
                .filter(result -> !result.isSuccess())
                .map(result -> result.getTest().short_name())
                .collect(Collectors.toList());

        WebUtils.addResultStats(model,results);

        ServerHelp serverHelp = Help.getInstance().getHelpFor(server.getSoftwareName()).orElse(null);
        if (serverHelp != null) {
            List<TestHelp> helps = serverHelp.getTestsHelp().stream()
                    .filter(th -> failedTests.contains(th.getName()))
                    .collect(Collectors.toList());
            model.put("helps", helps);
        }
        Instant lastRun = TestResultStore.INSTANCE.getLastRunFor(domain);
        model.put("domain", domain);
        model.put("results", results);
        model.put("historic_data", gson.toJson(TestResultStore.INSTANCE.getHistoricalSnapshotsForServer(domain)));
        model.put("softwareName", server.getSoftwareName());
        model.put("softwareVersion", server.getSoftwareVersion());
        model.put("timeSince", TimeUtils.getTimeSince(lastRun));
        model.put("timestamp", lastRun);
        String imageUrl = WebUtils.getRootUrlFrom(request) + "/badge/" + domain;
        model.put("badgeCode", "<a href='"+ request.url() + "'><img src='" + imageUrl + "'></a>");
        return new ModelAndView(model, "server.ftl");
    };

    public static TemplateViewRoute getTest = (request, response) -> {
        ComplianceTest test = TestUtils.getTestFrom(request.params("test"));
        HashMap<String, Object> model = new HashMap<>();
        if (test == null) {
            model.put("error_code", 404);
            model.put("error_msg", "Test " + request.params("test") + " not found");
            return new ModelAndView(model, "error.ftl");
        }
        model.put("test", test);
        Map<String, Boolean> results;
        try {
            results = TestResultStore.INSTANCE.getServerResultHashMapFor(test.short_name());
        } catch (Exception ex) {
            ex.printStackTrace();
            model.put("error_code", 404);
            model.put("error_msg", "Error getting results for " + test.full_name());
            return new ModelAndView(model, "error.ftl");
        }
        if (!results.isEmpty()) {
            int passed = results.entrySet().stream()
                    .map(it -> it.getValue() ? 1 : 0)
                    .reduce((it, ac) -> ac + it)
                    .get();
            int percent = (int) (passed * 100 / results.size());
            model.put("stats", new HashMap<String, String>() {
                {
                    put("Servers compliant", percent + "%");
                }
            });
        }
        model.put("results", results);
        model.put("historic_data", gson.toJson(TestResultStore.INSTANCE.getHistoricalSnapshotsForTest(test.short_name())));
        return new ModelAndView(model, "test.ftl");
    };

    public static TemplateViewRoute getBadge = (request, response) -> {
        response.type("image/svg+xml");
        HashMap<String, Object> model = new HashMap<>();
        String domain = request.params("domain");
        try {
            List<Result> results = TestResultStore.INSTANCE.getResultsFor(domain);
            WebUtils.addResultStats(model,results);
            String resultLink = WebUtils.getRootUrlFrom(request) + "/server/" + domain;
            model.put("domain",domain);
            model.put("resultLink",resultLink);
        } catch (Exception ex) {
        }
        return new ModelAndView(model, "badge.ftl");
    };

    public static TemplateViewRoute getHistoric = (request, response) -> {
        String domain = request.params("domain");
        HashMap<String, Object> model = new HashMap<>();
        model.put("domain", domain);
        return new ModelAndView(model, "historic.ftl");
    };
}
