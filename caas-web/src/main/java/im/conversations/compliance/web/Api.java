package im.conversations.compliance.web;

import im.conversations.compliance.persistence.DBOperations;
import spark.Route;

public class Api {
    public static Route getCompliantServers = (request, response) -> {
        StringBuilder str = new StringBuilder();
        for (String server : DBOperations.getCompliantServers()) {
            str.append(server);
            str.append('\n');
        }
        response.type("text/plain");
        response.header("Content-Encoding", "gzip");
        return str.toString();
    };
}
