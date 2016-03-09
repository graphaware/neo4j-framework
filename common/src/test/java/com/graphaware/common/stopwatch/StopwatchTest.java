package com.graphaware.common.stopwatch;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class StopwatchTest {

    @Test
    public void shouldHandleEvents() {
        Stopwatch stopwatch = new Stopwatch();
        stopwatch.start("e1");
        stopwatch.start("e2");
        Event e = stopwatch.stop("e1");
        assertEquals(1, e.getPeriods().size());
        assertEquals(e.getStartTime(), e.getPeriods().get(0).startTime());
        Event e2 = stopwatch.stop("e2");
        assertEquals(1, e2.getPeriods().size());
    }

    @Test
    public void shouldHandleLaps() throws InterruptedException{
        Stopwatch stopwatch = new Stopwatch();
        stopwatch.start("e");
        Thread.sleep(100);
        stopwatch.lap("e");
        Thread.sleep(100);
        stopwatch.lap("e");
        Event e = stopwatch.stop("e");

        assertEquals(3, e.getPeriods().size());
        assertEquals(200, e.duration(), 20L);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWhenStopButNotStarted() {
        Stopwatch stopwatch = new Stopwatch();
        stopwatch.stop("e");
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWhenTryingToStartAlreadyStartedEvent() {
        Stopwatch stopwatch = new Stopwatch();
        stopwatch.start("e");
        stopwatch.start("e");
    }
}
