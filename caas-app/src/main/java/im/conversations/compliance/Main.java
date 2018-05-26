package im.conversations.compliance;


import im.conversations.compliance.pojo.Configuration;
import im.conversations.compliance.web.Controller;
import im.conversations.compliance.web.TestLiveWebsocket;
import im.conversations.compliance.xmpp.PeriodicTestRunner;
import spark.TemplateEngine;
import spark.template.freemarker.FreeMarkerEngine;

import static spark.Spark.*;

public class Main {
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
        get("/add/", Controller.getAdd, templateEngine);
        post("/add/", Controller.postAdd);
        get("/live/:domain/", Controller.getLive, templateEngine);
        get("/server/:domain/", Controller.getServer, templateEngine);
        PeriodicTestRunner.getInstance();
    }
}
