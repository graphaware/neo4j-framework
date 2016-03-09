package com.graphaware.common.stopwatch;

import java.util.ArrayList;
import java.util.List;

public class Event {

    private Long start;

    private List<Period> periods = new ArrayList<>();

    private boolean isStopped = false;

    public Event() {
        start = System.currentTimeMillis();
    }

    public void stop() {
        periods.add(new Period(getLastPeriodTime(), System.currentTimeMillis()));
    }

    public Long getStartTime() {
        return start;
    }

    public Long duration() {
        return getLastPeriodTime() - start;
    }

    public void addPeriod() {
        periods.add(new Period(getLastPeriodTime(), System.currentTimeMillis()));
    }

    public List<Period> getPeriods() {
        return periods;
    }

    private Long getLastPeriodTime() {
        return periods.size() == 0 ? start : periods.get(periods.size()-1).endTime();
    }
}
