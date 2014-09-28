package com.graphaware.common.util;

import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Unit test for {@link BoundedSortedList}
 */
public class BoundedSortedListTest {

    @Test
    public void emptyListShouldProduceEmptyList() {
        BoundedSortedList<String, Integer> list = new BoundedSortedList<>(10);
        assertTrue(list.getItems().isEmpty());
    }

    @Test
    public void itemsShouldBeCorrectlySorted() {
        BoundedSortedList<String, Integer> list = new BoundedSortedList<>(3, Collections.<Integer>reverseOrder());

        list.add("one", 10);
        list.add("two", 1);
        list.add("three", 2);
        list.add("four", 4);

        List<String> result = list.getItems();
        assertEquals(3, result.size());

        assertEquals("one", result.get(0));
        assertEquals("four", result.get(1));
        assertEquals("three", result.get(2));

        list.add("five", 1);
        list.add("two", 3);
        list.add("three", 5);
        list.add("two", 6);
        list.add("two", 7);

        result = list.getItems();
        assertEquals(3, result.size());

        assertEquals("one", result.get(0));
        assertEquals("two", result.get(1));
        assertEquals("three", result.get(2));
    }

    @Test
    public void itemsShouldBeCorrectlySorted2() {
        BoundedSortedList<String, Integer> list = new BoundedSortedList<>(3);

        list.add("one", 10);
        list.add("two", 1);
        list.add("three", 2);
        list.add("four", 4);

        List<String> result = list.getItems();
        assertEquals(3, result.size());

        assertEquals("two", result.get(0));
        assertEquals("three", result.get(1));
        assertEquals("four", result.get(2));

        list.add("five", 1);
        list.add("two", 3);
        list.add("three", 5);
        list.add("two", 6);
        list.add("two", 7);

        result = list.getItems();
        assertEquals(3, result.size());

        assertEquals("five", result.get(0));
        assertEquals("three", result.get(1));
        assertEquals("two", result.get(2));
    }

    @Test
    public void itemsShouldBeCorrectlySorted3() {
        BoundedSortedList<String, Integer> list = new BoundedSortedList<>(3, 5);

        list.add("one", 10);
        list.add("two", 1);
        list.add("three", 2);
        list.add("four", 4);

        List<String> result = list.getItems();
        assertEquals(3, result.size());

        assertEquals("two", result.get(0));
        assertEquals("three", result.get(1));
        assertEquals("four", result.get(2));

        list.add("five", 1);
        list.add("two", 3);
        list.add("three", 5);
        list.add("two", 6);
        list.add("two", 7);

        result = list.getItems();
        assertEquals(3, result.size());

        assertEquals("five", result.get(0));
        assertEquals("four", result.get(1));
        assertEquals("three", result.get(2));
    }
}
