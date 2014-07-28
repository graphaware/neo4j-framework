package com.graphaware.runtime.monitor;

import com.graphaware.runtime.schedule.TimingStrategy;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit test for {@link RunningWindowAverage}.
 */
public class RunningWindowAverageTest {

    @Test
    public void shouldReturnCorrectValues() {
        RunningWindowAverage average = new RunningWindowAverage(5, 2000);
        assertEquals(TimingStrategy.UNKNOWN, average.getAverage());

        average.sample(10_000L, 10);
        assertEquals(TimingStrategy.UNKNOWN, average.getAverage());

        average.sample(11_000L, 20);
        assertEquals(10, average.getAverage());

        average.sample(12_000L, 40);
        assertEquals(15, average.getAverage());

        average.sample(12_500L, 45);
        assertEquals(16, average.getAverage()); // 25/1.5

        average.sample(13_000L, 50);
        assertEquals(15, average.getAverage()); // 30/2

        average.sample(13_300L, 70);
        assertEquals(23, average.getAverage()); // 30/1.3

        average.sample(13_600L, 75);
        average.sample(13_800L, 80);
        average.sample(14_000L, 82);
        assertEquals(32, average.getAverage()); // 32/1 (5 samples max)
    }
}
