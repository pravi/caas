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
import spark.TemplateEngine;
import spark.template.freemarker.FreeMarkerEngine;

import static spark.Spark.*;

public class WebLauncher {
    public static void main(String[] args) {
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
        get("/historic/server/:domain/iteration/:iteration/", Controller.getHistoric, templateEngine);
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
