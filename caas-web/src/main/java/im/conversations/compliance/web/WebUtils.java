package im.conversations.compliance.web;

import im.conversations.compliance.annotations.ComplianceTest;
import im.conversations.compliance.pojo.Configuration;
import im.conversations.compliance.pojo.Result;
import im.conversations.compliance.xmpp.utils.TestUtils;
import spark.Request;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WebUtils {

    private static String[] WELL_KNOWN_PING_TARGETS = new String[]{"8.8.8.8", "1.1.1.1"};

    public static void addRootUrl(HashMap<String, Object> model, Request request) {
        model.put("root_url", getRootUrlFrom(request));
    }

    /**
     * Gets root url for a request
     * e.g. if request url is "https://compliance.conversations.im/badge/conversations.im",
     * it will return "https://compliance.conversations.im"
     *
     * @param request
     * @return
     */
    public static String getRootUrlFrom(Request request) {
        String rootUrl = Configuration.getInstance().getRootURL();
        if (rootUrl == null) {
            final String url = request.url();
            final String path = request.uri();
            rootUrl = url.substring(0, url.length() - path.length());
        }
        return rootUrl;
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
        if (total <= 0) {
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

    public static void addDataForComplianceTable(HashMap<String, Object> model, Map<String, HashMap<String, Boolean>> resultsByServer) {
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
        model.put("tests", complianceTests);
        model.put("percentByServer", percentByServer);
        model.put("resultsByServer", resultsByServer);
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
