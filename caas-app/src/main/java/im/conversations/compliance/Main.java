package im.conversations.compliance;


import im.conversations.compliance.pojo.Configuration;
import im.conversations.compliance.web.Controller;
import spark.TemplateEngine;
import spark.template.freemarker.FreeMarkerEngine;

import static spark.Spark.*;

public class Main {
    public static void main(String[] args) {
        TemplateEngine templateEngine = new FreeMarkerEngine();
        ipAddress(Configuration.getInstance().getIp());
        port(Configuration.getInstance().getPort());
        before((request, response) -> {
            if (!request.pathInfo().endsWith("/")) {
                response.redirect(request.pathInfo() + "/");
            }
        });
        get("/add/", Controller.getAdd, templateEngine);
        post("/add/", Controller.postAdd, templateEngine);
    }
}
