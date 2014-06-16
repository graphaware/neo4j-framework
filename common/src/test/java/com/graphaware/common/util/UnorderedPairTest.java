package com.graphaware.common.util;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit test for {@link UnorderedPair}.
 */
public class UnorderedPairTest {

    @Test
    public void equalPairsShouldBeEqual() {
        assertTrue(new UnorderedPair<>("test", "test").equals(new UnorderedPair<>("test", "test")));
        assertTrue(new UnorderedPair<>("test1", "test").equals(new UnorderedPair<>("test", "test1")));
        assertTrue(new UnorderedPair<>(null, "test").equals(new UnorderedPair<>(null, "test")));
        assertTrue(new UnorderedPair<>(null, "test").equals(new UnorderedPair<>("test", null)));
        assertTrue(new UnorderedPair<>("test", null).equals(new UnorderedPair<>("test", null)));
        assertTrue(new UnorderedPair<>("test", null).equals(new UnorderedPair<>(null, "test")));
        assertTrue(new UnorderedPair<>(null, null).equals(new UnorderedPair<>(null, null)));
    }

    @Test
    public void unEqualPairsShouldNotBeEqual() {
        assertFalse(new UnorderedPair<>("test1", "test2").equals(new UnorderedPair<>("test1", "test1")));
        assertFalse(new UnorderedPair<>("test1", "test1").equals(new UnorderedPair<>(null, "test1")));
        assertFalse(new UnorderedPair<>("test1", null).equals(new UnorderedPair<>("test1", "test1")));
        assertFalse(new UnorderedPair<>(null, null).equals(new UnorderedPair<>("test1", "test1")));
        assertFalse(new UnorderedPair<>("test1", "test1").equals(new UnorderedPair<>("test1", null)));
        assertFalse(new UnorderedPair<>("test1", "test1").equals(new UnorderedPair<>(null, null)));
    }

    @Test
    public void equalObjectsShouldHaveSameHashCode() {
        assertEquals(new UnorderedPair<>("test", "test").hashCode(), new UnorderedPair<>("test", "test").hashCode());
        assertEquals(new UnorderedPair<>("test1", "test").hashCode(), new UnorderedPair<>("test", "test1").hashCode());
        assertEquals(new UnorderedPair<>(null, "test").hashCode(), new UnorderedPair<>(null, "test").hashCode());
        assertEquals(new UnorderedPair<>(null, "test").hashCode(), new UnorderedPair<>("test", null).hashCode());
        assertEquals(new UnorderedPair<>("test", null).hashCode(), new UnorderedPair<>("test", null).hashCode());
        assertEquals(new UnorderedPair<>("test", null).hashCode(), new UnorderedPair<>(null, "test").hashCode());
        assertEquals(new UnorderedPair<>(null, null).hashCode(), new UnorderedPair<>(null, null).hashCode());
    }

    @Test
    public void shouldBeAbleToPrintPairWithNulls() {
        new UnorderedPair<>(null, null).toString();
    }
}
