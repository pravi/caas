package im.conversations.compliance.web;

import com.google.gson.Gson;
import im.conversations.compliance.annotations.ComplianceTest;
import im.conversations.compliance.email.MailVerification;
import im.conversations.compliance.persistence.DBOperations;
import im.conversations.compliance.pojo.*;
import im.conversations.compliance.utils.JsonReader;
import im.conversations.compliance.utils.TimeUtils;
import im.conversations.compliance.xmpp.CredentialVerifier;
import im.conversations.compliance.xmpp.OneOffTestRunner;
import im.conversations.compliance.xmpp.utils.TestUtils;
import org.apache.commons.validator.routines.EmailValidator;
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
        Map<String, HashMap<String, Boolean>> resultsByServer = DBOperations.getCurrentResultsByServer();
        HashMap<String, String> percentByServer = new HashMap<>();
        resultsByServer.keySet().forEach(
                domain ->
                {
                    int total = resultsByServer.get(domain).size();
                    int success = resultsByServer.get(domain)
                            .values()
                            .stream()
                            .map(it -> it ? 1 : 0)
                            .reduce((it, val) -> it + val)
                            .get();
                    String percent = (success * 100 / total) + "% (" + success + "/" + total + ")";
                    percentByServer.put(domain, percent);
                }
        );
        List<ComplianceTest> complianceTests = TestUtils.getComplianceTests();
        HashMap<String, Object> model = new HashMap<>();
        model.put("percentByServer", percentByServer);
        model.put("resultsByServer", resultsByServer);
        model.put("tests", complianceTests);
        return new ModelAndView(model, "root.ftl");
    };

    public static TemplateViewRoute getTests = (request, response) -> {
        HashMap<String, Object> model = new HashMap<>();
        model.put("tests", TestUtils.getAllComplianceTests());
        return new ModelAndView(model, "tests.ftl");
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

        List<Credential> credentials = DBOperations.getCredentials();
        Predicate<Credential> jidMatches = it -> it.getJid().toString().equals(credential.getJid().toString());
        Predicate<Credential> passwordMatches = it -> it.getPassword().equals(credential.getPassword());
        if (credentials != null && credentials.stream().filter(jidMatches).anyMatch(passwordMatches)) {
            Server server = DBOperations.getServer(credential.getDomain()).orElse(null);
            if (server != null) {
                DBOperations.setListed(server.getDomain(), listedServer);
                postResponse = new ServerResponse(
                        false,
                        "UPDATE: Server will now be " +
                                (listedServer ? "included in" : "excluded from") +
                                " the public lists and tables",
                        null);
                return gson.toJson(postResponse);
            }
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
            credentials.stream()
                    .filter(jidMatches)
                    .findFirst()
                    .ifPresent(DBOperations::removeCredential);
        }

        //Check if domain exists, if not add to domains table
        if (!DBOperations.getServer(credential.getDomain()).isPresent()) {
            boolean domainAdded = DBOperations.addServer(new Server(credential.getDomain(), listedServer));
            if (!domainAdded) {
                postResponse = new ServerResponse(
                        false,
                        "ERROR: Could not add/update domain to the database",
                        null
                );
                return gson.toJson(postResponse);
            }
        }
        //Update server's listing for new credentials for an existing server
        else {
            DBOperations.setListed(credential.getDomain(), listedServer);
        }

        // Add credentials to database
        boolean dbAdded = DBOperations.addCredential(credential);
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
        Credential credential = DBOperations.getCredentials()
                .stream()
                .filter(it -> it.getDomain().equals(domain))
                .findFirst()
                .orElse(null);

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
        Server server = DBOperations.getServer(domain).orElse(null);
        if (server == null) {
            model.put("error_code", 404);
            model.put("error_msg", "Credentials unavailable for " + domain);
            return new ModelAndView(model, "error.ftl");
        }
        List<Result> results = DBOperations.getCurrentResultsForServer(domain);
        if (results.isEmpty()) {
            model.put("error_code", 404);
            model.put("error_msg", "Results unavailable for " + domain + ". Tests might still be running");
            return new ModelAndView(model, "error.ftl");
        }
        List<String> failedTests = results.stream()
                .filter(result -> !result.isSuccess())
                .map(result -> result.getTest().short_name())
                .collect(Collectors.toList());

        WebUtils.addResultStats(model, results);

        ServerHelp serverHelp = Help.getInstance().getHelpFor(server.getSoftwareName()).orElse(null);
        if (serverHelp != null) {
            List<TestHelp> helps = serverHelp.getTestsHelp().stream()
                    .filter(th -> failedTests.contains(th.getName()))
                    .collect(Collectors.toList());
            model.put("helps", helps);
        }
        Instant lastRun = DBOperations.getLastRunFor(domain);
        model.put("domain", domain);
        model.put("results", results);
        model.put("historic_data", gson.toJson(DBOperations.getHistoricResultsGroupedByServer().get(domain)));
        model.put("softwareName", server.getSoftwareName());
        model.put("softwareVersion", server.getSoftwareVersion());
        model.put("timeSince", TimeUtils.getTimeSince(lastRun));
        model.put("timestamp", lastRun);
        model.put("tests", TestUtils.getComplianceTestMap());
        String imageUrl = WebUtils.getRootUrlFrom(request) + "/badge/" + domain;
        model.put("badgeCode", "<a href='" + request.url() + "'><img src='" + imageUrl + "'></a>");
        boolean mailExists = Configuration.getInstance().getMailConfig() != null;
        model.put("mailExists",mailExists);
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
        Map<String, Boolean> results = DBOperations.getCurrentResultsForTest(test);

        if (results.isEmpty()) {
            model.put("error_code", 404);
            model.put("error_msg", "Results unavailable for " + test.full_name());
            return new ModelAndView(model, "error.ftl");
        } else {
            int passed = results.entrySet().stream()
                    .map(it -> it.getValue() ? 1 : 0)
                    .reduce((it, ac) -> ac + it)
                    .get();
            int percent = passed * 100 / results.size();
            model.put("stats", new HashMap<String, String>() {
                {
                    put("Servers compliant", percent + "%");
                }
            });
        }
        model.put("results", results);
        model.put("historic_data", gson.toJson(
                DBOperations.getHistoricResultsGroupedByTest()
                        .get(test.short_name())
        ));
        return new ModelAndView(model, "test.ftl");
    };

    public static TemplateViewRoute getBadge = (request, response) -> {
        response.type("image/svg+xml");
        HashMap<String, Object> model = new HashMap<>();
        String domain = request.params("domain");
        List<Result> results = DBOperations.getCurrentResultsForServer(domain);
        WebUtils.addResultStats(model, results);
        String resultLink = WebUtils.getRootUrlFrom(request) + "/server/" + domain;
        model.put("domain", domain);
        model.put("resultLink", resultLink);
        return new ModelAndView(model, "badge.ftl");
    };

    public static TemplateViewRoute getHistoric = (request, response) -> {
        String domain = request.params("domain");
        int iterationNumber = Integer.parseInt(request.params("iteration"));
        HashMap<String, Object> model = new HashMap<>();
        model.put("domain", domain);
        Iteration iteration = DBOperations.getIteration(iterationNumber);
        if (iteration == null) {
            model.put("error_code", 404);
            model.put("error_msg", "ERROR: Invalid historical point requested");
            return new ModelAndView(model, "error.ftl");
        }
        List<Result> results = DBOperations.getHistoricalResultsFor(domain, iterationNumber);
        model.put("iteration", iteration);
        model.put("results", results);
        return new ModelAndView(model, "historic.ftl");
    };

    public static Route getConfirmation = (request, response) -> {
        String code = request.params("code");
        return MailVerification.verifyEmail(code);
    };

    public static Route postSubscription = (request, response) -> {
        MailVerification.removeExpiredRequests();
        String email = request.queryParams("email");
        String domain = request.queryParams("domain");
        String rootUrl = WebUtils.getRootUrlFrom(request);
        ServerResponse serverResponse;
        EmailValidator validator = EmailValidator.getInstance();
        if (validator.isValid(email)) {
            MailVerification.addEmailToList(email, domain);
            serverResponse = new ServerResponse(true, "Verification mail sent", null);
            return gson.toJson(serverResponse);
        }
        serverResponse = new ServerResponse(false, "Invalid email ID", null);
        return gson.toJson(serverResponse);
    };

    public static Route getUnsubscribe = (request, response) -> {
        String code = request.params("code");
        Subscriber subscriber = DBOperations.removeSubscriber(code);
        if(subscriber == null) {
            return "Invalid unsubscription code";
        }
        return "Unsubscribed " + subscriber.getEmail()
                + " from receiving alerts, periodic compliance reports for "
                + subscriber.getDomain();
    };
}
