package com.graphaware.common.stopwatch;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PeriodTest {

    @Test
    public void shouldReturnStartAndEndTime() {
        Long t = System.currentTimeMillis();
        Long t2 = System.currentTimeMillis();
        Period period = new Period(t, t2);
        assertEquals(t, period.startTime());
        assertEquals(t2, period.endTime());
    }

    @Test
    public void shouldReturnDuration() {
        Long t = System.currentTimeMillis();
        Long t2 = t + 1000;
        Period period = new Period(t, t2);
        assertEquals(1000, period.duration(), 0L);
    }

}
