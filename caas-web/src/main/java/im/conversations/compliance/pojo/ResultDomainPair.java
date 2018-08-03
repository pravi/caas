package im.conversations.compliance.pojo;

import java.util.List;

public class ResultDomainPair {
    String domain;
    List<Result> results;

    public ResultDomainPair(String domain, List<Result> results) {
        this.domain = domain;
        this.results = results;
    }

    public String getDomain() {
        return domain;
    }

    public List<Result> getResults() {
        return results;
    }
}
