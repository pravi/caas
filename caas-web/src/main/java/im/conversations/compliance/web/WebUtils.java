package im.conversations.compliance.web;

import im.conversations.compliance.pojo.Result;
import spark.Request;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public class WebUtils {

    private static String[] WELL_KNOWN_PING_TARGETS = new String[]{"8.8.8.8", "1.1.1.1"};

    /**
     * Gets root url for a request
     * e.g. if request url is "https://compliance.conversations.im/badge/conversations.im",
     * it will return "https://compliance.conversations.im"
     *
     * @param request
     * @return
     */
    public static String getRootUrlFrom(Request request) {
        return request.url().split(request.uri())[0];
    }

    /**
     * Adds pass,result and stats to model
     *
     * @param model   The map to which pass,result and stats will be added
     * @param results The results from which the stats will be calculated
     */
    public static void addResultStats(HashMap<String, Object> model, List<Result> results) {
        int pass, total;
        pass = total = 0;
        for (Result result : results) {
            if (!result.getTest().informational()) {
                if (result.isSuccess()) {
                    pass++;
                }
                total++;
            }
        }
        if(total <= 0) {
            return;
        }
        int percent = pass * 100 / total;
        model.put("pass", pass);
        model.put("total", total);
        model.put("stats", new HashMap<String, String>() {
            {
                put("Specifications compliant", percent + "%");
            }
        });
    }

    public static boolean isConnected() {
        for (String ip : WELL_KNOWN_PING_TARGETS) {
            if (ping(ip)) {
                return true;
            }
        }
        return false;
    }

    private static boolean ping(String ip) {
        try {
            Process ping = Runtime.getRuntime().exec("ping -c 1 " + ip);
            return ping.waitFor() == 0;
        } catch (IOException | InterruptedException e) {
            return false;
        }
    }
}
