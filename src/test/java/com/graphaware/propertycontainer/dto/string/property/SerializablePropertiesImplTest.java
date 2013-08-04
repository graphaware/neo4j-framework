/*
 * Copyright (c) 2013 GraphAware
 *
 * This file is part of GraphAware.
 *
 * GraphAware is free software: you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received a copy of
 * the GNU General Public License along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 */

package com.graphaware.propertycontainer.dto.string.property;

import com.graphaware.propertycontainer.dto.AbstractTest;
import org.junit.Test;

import java.util.Collections;

import static junit.framework.Assert.*;
import static org.neo4j.graphdb.Direction.OUTGOING;
import static org.neo4j.graphdb.DynamicRelationshipType.withName;

/**
 * Unit test for {@link SerializablePropertiesImpl}.
 */
public class SerializablePropertiesImplTest extends AbstractTest {

    @Test
    public void shouldBeCorrectlyConstructedFromNode() {
        setUp();

        StringProperties properties = new SerializablePropertiesImpl(database.getNodeById(1));

        assertEquals("value1", properties.get("prop1"));
        assertEquals("2", properties.get("prop2"));
        assertEquals(2, properties.size());
    }

    @Test
    public void shouldBeCorrectlyConstructedFromRelationship() {
        setUp();

        StringProperties properties = new SerializablePropertiesImpl(database.getNodeById(1).getSingleRelationship(withName("test"), OUTGOING));

        assertEquals("10000434132", properties.get("prop3"));
        assertEquals("[3, 4, 5]", properties.get("prop4"));
        assertEquals(2, properties.size());
    }

    @Test
    public void shouldBeCorrectlyConstructedFromMap() {
        StringProperties properties = new SerializablePropertiesImpl(Collections.singletonMap("key", "value"));

        assertEquals("value", properties.get("key"));
        assertEquals(1, properties.size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldComplainWhenAKeyIsNull() {
        new SerializablePropertiesImpl(Collections.singletonMap((String) null, "value"));
        fail();
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldComplainWhenAKeyIsEmpty() {
        new SerializablePropertiesImpl(Collections.singletonMap("", "value"));
        fail();
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldComplainWhenAKeyIsEmpty2() {
        new SerializablePropertiesImpl(Collections.singletonMap(" ", "value"));
        fail();
    }

    @Test
    public void shouldNotComplainWhenAValueIsNull() {
        StringProperties properties = new SerializablePropertiesImpl(Collections.singletonMap("key", (String) null));

        assertEquals("", properties.get("key"));
        assertEquals(1, properties.size());
    }

    @Test
    public void shouldNotComplainWhenAValueIsEmpty() {
        StringProperties properties = new SerializablePropertiesImpl(Collections.singletonMap("key", ""));

        assertEquals("", properties.get("key"));
        assertEquals(1, properties.size());
    }

    @Test
    public void shouldBeCorrectlyConstructedFromString() {
        StringProperties properties = new SerializablePropertiesImpl("key1#value1#key2#value2", "#");

        assertEquals("value1", properties.get("key1"));
        assertEquals("value2", properties.get("key2"));
        assertEquals(2, properties.size());
    }

    @Test
    public void shouldBeCorrectlyConstructedFromStringWithPrefix() {
        StringProperties properties = new SerializablePropertiesImpl("_PRE_key1#value1#key2#value2", "_PRE_", "#");

        assertEquals("value1", properties.get("key1"));
        assertEquals("value2", properties.get("key2"));
        assertEquals(2, properties.size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldComplainWhenKeyIsEmpty() {
        new SerializablePropertiesImpl("key1#value1##value2", "#");
    }

    @Test
    public void shouldIncludeMissingValueAsEmptyString() {
        StringProperties properties = new SerializablePropertiesImpl("key1#value1#key2", "#");

        assertEquals("value1", properties.get("key1"));
        assertEquals("", properties.get("key2"));
        assertEquals(2, properties.size());
    }

    @Test
    public void shouldIncludeMissingValueAsEmptyString2() {
        StringProperties properties = new SerializablePropertiesImpl("key1##key2", "#");

        assertEquals("", properties.get("key1"));
        assertEquals("", properties.get("key2"));
        assertEquals(2, properties.size());
    }

    @Test
    public void shouldIncludeEmptyValue() {
        StringProperties properties = new SerializablePropertiesImpl("key1#value1#key2#", "#");

        assertEquals("value1", properties.get("key1"));
        assertEquals("", properties.get("key2"));
        assertEquals(2, properties.size());

        properties = new SerializablePropertiesImpl("key1##key2#value2", "#");

        assertEquals("", properties.get("key1"));
        assertEquals("value2", properties.get("key2"));
        assertEquals(2, properties.size());
    }

    @Test
    public void shouldConstructWithNoProps() {
        StringProperties properties = new SerializablePropertiesImpl("", "#");

        assertTrue(properties.isEmpty());
    }

    @Test
    public void shouldConstructWithNoProps2() {
        StringProperties properties = new SerializablePropertiesImpl("_PRE_", "_PRE_", "#");

        assertTrue(properties.isEmpty());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void shouldNotBeAllowedToModify() {
        new SerializablePropertiesImpl("key1#value1#key2#value2", "#").getProperties().remove("key1");
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldComplainAboutIncorrectPrefix() {
        new SerializablePropertiesImpl("_PRE_key1#value1", "_WRN_", "#");
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldComplainAboutNullSeparator() {
        new SerializablePropertiesImpl("_PRE_key1#value1", "_PRE_", null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldComplainAboutEmptySeparator() {
        new SerializablePropertiesImpl("_PRE_key1#value1", "_PRE_", "");
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldComplainAboutEmptySeparator2() {
        new SerializablePropertiesImpl("_PRE_key1#value1", "_PRE_", " ");
    }

    @Test
    public void shouldCorrectlyConvertToString() {
        assertEquals("key1#value1#key2#value2", new SerializablePropertiesImpl("key1#value1#key2#value2", "#").toString("#"));
        assertEquals("_PRE_key1#value1#key2#", new SerializablePropertiesImpl("key1#value1#key2#", "#").toString("_PRE_", "#"));
        assertEquals("key1#value1#key2#", new SerializablePropertiesImpl("key1#value1#key2", "#").toString("#"));
        assertEquals("key1##key2#", new SerializablePropertiesImpl("key1##key2#", "#").toString("#"));
        assertEquals("", new SerializablePropertiesImpl("", "#").toString("#"));
        assertEquals("_PRE_", new SerializablePropertiesImpl("", "#").toString("_PRE_", "#"));
        assertEquals("key#", new SerializablePropertiesImpl(Collections.singletonMap("key", (String) null)).toString("#"));
    }

    @Test
    public void samePropsShouldBeEqual() {
        assertTrue(new SerializablePropertiesImpl("key1#value1#key2#value2", "#").equals(new SerializablePropertiesImpl("key1#value1#key2#value2", "#")));
        assertTrue(new SerializablePropertiesImpl("key2#value2#key1#value1", "#").equals(new SerializablePropertiesImpl("key1#value1#key2#value2", "#")));
        assertTrue(new SerializablePropertiesImpl("key2#value2#key1#value1", "#").equals(new SerializablePropertiesImpl("key1#value1#key2#value2#", "#")));
        assertTrue(new SerializablePropertiesImpl("key2#value2#key1#value1", "#").equals(new SerializablePropertiesImpl("key1#value1#key2#value2######", "#")));
        assertTrue(new SerializablePropertiesImpl("key1##key2#value2", "#").equals(new SerializablePropertiesImpl("_PRE_key1##key2#value2", "_PRE_", "#")));
        assertTrue(new SerializablePropertiesImpl("key1#value1#key2#", "#").equals(new SerializablePropertiesImpl("key1#value1#key2#", "#")));
        assertTrue(new SerializablePropertiesImpl("key1#value1#key2", "#").equals(new SerializablePropertiesImpl("key1_value1_key2_", "_")));
        assertTrue(new SerializablePropertiesImpl("", "#").equals(new SerializablePropertiesImpl("", "#")));
        assertTrue(new SerializablePropertiesImpl("", "#").equals(new SerializablePropertiesImpl("_PRE_", "_PRE_", "#")));
    }

    @Test
    public void differentPropsShouldNotBeEqual() {
        assertFalse(new SerializablePropertiesImpl("key1#value1#key2#value3", "#").equals(new SerializablePropertiesImpl("key1#value1#key2#value2", "#")));
        assertFalse(new SerializablePropertiesImpl("key1#value2#key2#value1", "#").equals(new SerializablePropertiesImpl("key1#value1#key2#value2", "#")));
        assertFalse(new SerializablePropertiesImpl("key1#value1#key2#", "#").equals(new SerializablePropertiesImpl("key1#value1#key2#value2", "#")));
        assertFalse(new SerializablePropertiesImpl("key1#value1#key2#value2", "#").equals(new SerializablePropertiesImpl("key1#value1", "#")));
    }

    @Test
    public void samePropsShouldHaveSameHashCode() {
        assertEquals(new SerializablePropertiesImpl("key1#value1#key2#value2", "#").hashCode(), new SerializablePropertiesImpl("key1#value1#key2#value2", "#").hashCode());
    }

    @Test
    public void differentPropsShouldHaveDifferentHashCode() {
        assertNotSame(new SerializablePropertiesImpl("key1#value1#key2#value2", "#").hashCode(), new SerializablePropertiesImpl("key1#value1#key2#value3", "#").hashCode());
    }
}
