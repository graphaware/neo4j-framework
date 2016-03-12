package com.graphaware.common.stopwatch;

public class Period {

    private Long start;

    private Long end;

    public Period(Long start, Long end) {
        this.start = start;
        this.end = end;
    }

    public Long startTime() {
        return start;
    }

    public Long endTime() {
        return end;
    }

    public Long duration() {
        return end - start;
    }

    @Override
    public String toString() {
        return this.duration() + " ms";
    }

}
