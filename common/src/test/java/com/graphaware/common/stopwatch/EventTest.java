package com.graphaware.common.stopwatch;

import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class EventTest {

    @Test
    @Ignore
    public void shouldStartOnCreate() {
        Event event = new Event();
        assertTrue(event.duration().equals(event.getStartTime()));
    }

    @Test
    public void shouldAddPeriodOnStop() throws InterruptedException{
        Event event = new Event();
        sleep(100L);
        event.stop();
        assertTrue(event.duration() >= 100);
        assertEquals(1, event.getPeriods().size());
    }

    @Test
    public void shouldHandleLaps() throws InterruptedException{
        Event event = new Event();
        sleep(100L);
        event.addPeriod();
        sleep(100L);
        event.stop();

        assertEquals(100, event.getPeriods().get(0).duration(), 10L);
        assertEquals(100, event.getPeriods().get(1).duration(), 10L);
        assertEquals(200, event.duration(), 20L);
        assertEquals(2, event.getPeriods().size());
    }

    private void sleep(Long millis) throws InterruptedException{
        Thread.sleep(millis);
    }

}
