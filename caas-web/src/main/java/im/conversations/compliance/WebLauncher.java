package im.conversations.compliance;


import im.conversations.compliance.email.MailBuilder;
import im.conversations.compliance.persistence.DBConnections;
import im.conversations.compliance.persistence.DBOperations;
import im.conversations.compliance.pojo.Configuration;
import im.conversations.compliance.pojo.Help;
import im.conversations.compliance.pojo.MailConfig;
import im.conversations.compliance.web.Controller;
import im.conversations.compliance.web.TestLiveWebsocket;
import im.conversations.compliance.xmpp.PeriodicTestRunner;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.TemplateEngine;
import spark.template.freemarker.FreeMarkerEngine;

import static spark.Spark.*;

public class WebLauncher {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebLauncher.class);

    public static void main(String[] args) {

        Options options = new Options();
        options.addOption(new Option("c", "config", true, "Path to the config file"));
        try {
            CommandLine cmd = new DefaultParser().parse(options, args);
            String configPath = cmd.getOptionValue("c");
            if (configPath != null) {
                Configuration.setFilename(configPath);
            }
        } catch (ParseException e) {
            LOGGER.warn("unable to parse config. using default", e);
        }
        start();
    }

    private static void start() {

        TemplateEngine templateEngine = new FreeMarkerEngine();
        staticFileLocation("/public");
        webSocket("/socket/*", TestLiveWebsocket.class);
        ipAddress(Configuration.getInstance().getIp());
        port(Configuration.getInstance().getPort());
        before((request, response) -> {
            if (!request.pathInfo().endsWith("/")) {
                response.redirect(request.pathInfo() + "/");
            }
        });
        get("/", Controller.getRoot, templateEngine);
        get("/tests/", Controller.getTests, templateEngine);
        get("/about/", Controller.getAbout, templateEngine);
        get("/add/", Controller.getAdd, templateEngine);
        post("/add/", Controller.postAdd);
        get("/live/:domain/", Controller.getLive, templateEngine);
        get("/server/:domain/", Controller.getServer, templateEngine);
        get("/badge/:domain/", Controller.getBadge, templateEngine);
        get("/test/:test/", Controller.getTest, templateEngine);
        get("/historic/server/:domain/iteration/:iteration/", Controller.getHistoricForServer, templateEngine);
        get("/historic/iteration/:iteration/", Controller.getHistoricTable, templateEngine);
        MailConfig mailConfig = Configuration.getInstance().getMailConfig();
        if (mailConfig != null) {
            post("/subscribe/", Controller.postSubscription);
            get("/confirm/:code/", Controller.getConfirmation);
            get("/unsubscribe/:code/",Controller.getUnsubscribe);
            try {
                MailBuilder.init(mailConfig.getFrom(), Configuration.getInstance().getRootURL());
            } catch (Exception ex) {
                ex.printStackTrace();
                System.exit(1);
            }
        }
        DBConnections.init();
        DBOperations.init();
        PeriodicTestRunner.getInstance();
        Help.getInstance();
    }
}
