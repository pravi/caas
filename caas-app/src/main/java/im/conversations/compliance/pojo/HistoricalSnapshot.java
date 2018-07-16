package im.conversations.compliance.pojo;

import java.util.ArrayList;
import java.util.List;

public class HistoricalSnapshot {
    private int iteration;
    private String timestamp;
    private int passed;
    private int total;
    private Change change;

    public HistoricalSnapshot(int iteration, String timestamp, int passed, int total, Change change) {
        this.iteration = iteration;
        this.timestamp = timestamp;
        this.passed = passed;
        this.total = total;
        this.change = change;
    }

    public int getIteration() {
        return iteration;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public int getPassed() {
        return passed;
    }

    public int getTotal() {
        return total;
    }

    public Change getChange() {
        return change;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof HistoricalSnapshot) {
            HistoricalSnapshot hs = (HistoricalSnapshot) o;
            boolean value = hs.iteration == iteration &&
                    hs.timestamp.equals(timestamp) &&
                    hs.change.equals(change) &&
                    hs.total == total &&
                    hs.passed == passed;
            return value;
        }
        return false;
    }

    public static class Change {
        private List<String> pass;
        private List<String> fail;

        public Change() {
            this.pass = new ArrayList<>();
            this.fail = new ArrayList<>();
        }

        public Change(List<String> pass, List<String> fail) {
            this.pass = pass;
            this.fail = fail;
        }

        public List<String> getPass() {
            return pass;
        }

        public List<String> getFail() {
            return fail;
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof Change) {
                Change change = (Change) o;
                if (change.pass.size() != this.pass.size()) {
                    return false;
                }
                if (change.fail.size() != this.fail.size()) {
                    return false;
                }
                for (String p : change.pass) {
                    if (!pass.contains(p)) {
                        return false;
                    }
                }
                for (String f : change.fail) {
                    if (!fail.contains(f)) {
                        return false;
                    }
                }
                return true;
            }
            return false;
        }
    }
}
