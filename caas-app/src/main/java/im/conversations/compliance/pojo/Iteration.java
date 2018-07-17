package im.conversations.compliance.pojo;

import java.time.Instant;

public class Iteration {
    private final int iterationNumber;
    private final Instant begin;
    private final Instant end;

    public Iteration(int iterationNumber, Instant begin, Instant end) {
        this.iterationNumber = iterationNumber;
        this.begin = begin;
        this.end = end;
    }

    public int getIterationNumber() {
        return iterationNumber;
    }

    public Instant getBegin() {
        return begin;
    }

    public Instant getEnd() {
        return end;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Iteration) {
            Iteration i = (Iteration) o;
            return begin.equals(i.begin) &&
                    end.equals(i.end) &&
                    i.iterationNumber == iterationNumber;
        }
        return false;
    }
}
