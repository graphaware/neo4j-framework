package com.graphaware.generator.utils;

import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Set;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Unit test for {@link RandomIndexChoice}.
 */
public class RandomIndexChoiceTest {

    private RandomIndexChoice random = new RandomIndexChoice();

    @Test
    public void shouldChooseRandomNumberFromRangeButNotInOmitList() {
        for (int i = 0; i < 1000; i++) {
            PriorityQueue<Long> omitted = new PriorityQueue<>(Arrays.asList(1L, 2L, 6L));
            long choice = random.randomIndexChoice(10L, omitted);
            assertFalse(omitted.contains(choice));
            assertTrue(choice >= 0);
            assertTrue(choice < 10);
        }
    }

    @Test
    public void shouldChooseRandomNumberFromRangeButNotInOmitSet() {
        for (int i = 0; i < 1000; i++) {
            Set<Integer> omitted = new HashSet<>(Arrays.asList(1, 2, 6));
            int choice = random.randomIndexChoice(10, omitted);
            assertFalse(omitted.contains(choice));
            assertTrue(choice >= 0);
            assertTrue(choice < 10);
        }
    }

    @Test
    public void shouldChooseRandomNumberFromRangeButNotOmitted() {
        for (int i = 0; i < 1000; i++) {
            int choice = random.randomIndexChoice(10, 4);
            assertFalse(choice == 4);
            assertTrue(choice >= 0);
            assertTrue(choice < 10);
        }
    }

    @Test
    public void shouldChooseRandomNumberFromRange() {
        for (int i = 0; i < 1000; i++) {
            int choice = random.randomIndexChoice(10);
            assertTrue(choice >= 0);
            assertTrue(choice < 10);
        }
    }
}
