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

package com.graphaware.propertycontainer.dto.string.relationship;

import com.graphaware.propertycontainer.dto.AbstractTest;
import com.graphaware.propertycontainer.dto.string.property.SerializableProperties;
import com.graphaware.propertycontainer.dto.string.property.SerializablePropertiesImpl;
import org.junit.Test;

import java.util.Collections;

import static junit.framework.Assert.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.neo4j.graphdb.Direction.INCOMING;
import static org.neo4j.graphdb.Direction.OUTGOING;
import static org.neo4j.graphdb.DynamicRelationshipType.withName;

/**
 * Unit test for {@link SerializableRelationshipImpl}.
 */
public class SerializableRelationshipImplTest extends AbstractTest {

    @Override
    protected void additionalSetup() {
        database.getNodeById(2).createRelationshipTo(database.getNodeById(1), withName("test2"));
    }

    @Test
    public void shouldBeConstructedFromRelationship() {
        setUp();

        SerializableRelationship relationship = new SerializableRelationshipImpl(database.getNodeById(1).getSingleRelationship(withName("test"), OUTGOING));

        assertEquals("test", relationship.getType().name());
        assertEquals(new SerializablePropertiesImpl("prop3#10000434132#prop4#[3, 4, 5]", "#"), relationship.getProperties());
    }

    @Test
    public void shouldBeConstructedFromRelationship2() {
        setUp();

        SerializableRelationship relationship = new SerializableRelationshipImpl(database.getNodeById(1).getSingleRelationship(withName("test2"), INCOMING));

        assertEquals("test2", relationship.getType().name());
        assertEquals(new SerializablePropertiesImpl("", "#"), relationship.getProperties());
    }

    @Test
    public void shouldBeConstructedFromRelationshipAndProperties() {
        setUp();

        SerializableProperties p = new SerializablePropertiesImpl(Collections.singletonMap("key", "value"));
        SerializableRelationship relationship = new SerializableRelationshipImpl(database.getNodeById(1).getSingleRelationship(withName("test"), OUTGOING), p);

        assertEquals("test", relationship.getType().name());
        assertEquals(1, relationship.getProperties().size());
        assertEquals("value", relationship.getProperties().get("key"));
    }

    @Test
    public void shouldBeConstructedFromRelationshipAndPropertiesAsMap() {
        setUp();

        SerializableRelationship relationship = new SerializableRelationshipImpl(database.getNodeById(1).getSingleRelationship(withName("test"), OUTGOING), Collections.singletonMap("key", "value"));

        assertEquals("test", relationship.getType().name());
        assertEquals(1, relationship.getProperties().size());
        assertEquals("value", relationship.getProperties().get("key"));
    }

    @Test
    public void shouldBeConstructedFromRelationshipTypeAndPropertiesAsMap() {
        setUp();

        SerializableRelationship relationship = new SerializableRelationshipImpl(withName("test"), Collections.singletonMap("key", "value"));

        assertEquals("test", relationship.getType().name());
        assertEquals(1, relationship.getProperties().size());
        assertEquals("value", relationship.getProperties().get("key"));
    }

    @Test
    public void shouldBeConstructedFromRelationshipTypeAndDirection() {
        SerializableRelationship relationship = new SerializableRelationshipImpl(withName("test"));

        assertEquals("test", relationship.getType().name());
        assertTrue(relationship.getProperties().isEmpty());
    }

    @Test
    public void shouldBeConstructedFromRelationshipTypeDirectionAndProperties() {
        SerializableRelationship relationship = new SerializableRelationshipImpl(withName("test"), new SerializablePropertiesImpl("key1#value1#key2#value2", "#"));

        assertEquals("test", relationship.getType().name());
        assertEquals(new SerializablePropertiesImpl("key1#value1#key2#value2", "#"), relationship.getProperties());
    }

    @Test
    public void shouldBeConstructedFromAnotherRelationship() {
        setUp();

        SerializableRelationship relationship = new SerializableRelationshipImpl(new SerializableRelationshipImpl(database.getNodeById(1).getSingleRelationship(withName("test"), OUTGOING)));

        assertEquals("test", relationship.getType().name());
        assertEquals(new SerializablePropertiesImpl("prop3#10000434132#prop4#[3, 4, 5]", "#"), relationship.getProperties());
    }

    @Test
    public void shouldBeCorrectlyConstructedFromString() {
        SerializableRelationship relationship = new SerializableRelationshipImpl("test#key1#value1#key2#value2", "#");

        assertEquals("test", relationship.getType().name());
        assertEquals(new SerializablePropertiesImpl("key1#value1#key2#value2", "#"), relationship.getProperties());
    }

    @Test
    public void shouldBeCorrectlyConstructedFromString2() {
        SerializableRelationship relationship = new SerializableRelationshipImpl("_PRE_test#key1#value1#key2#value2", "_PRE_", "#");

        assertEquals("test", relationship.getType().name());
        assertEquals(new SerializablePropertiesImpl("key1#value1#key2#value2", "#"), relationship.getProperties());
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWhenTypeIsEmpty() {
        new SerializableRelationshipImpl("#key1#value1#key2#value2", "#");
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWhenNoInfoProvided() {
        new SerializableRelationshipImpl("", "#");
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWhenNoInfoProvided2() {
        new SerializableRelationshipImpl("_PRE_", "_PRE_");
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWhenPropertiesAreInvalid() {
        new SerializableRelationshipImpl("test##value1#key2#value2", "#");
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWhenWrongPrefix() {
        new SerializableRelationshipImpl("test#key1#value1#key2#value2", "_PRE_", "#");
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldComplainAboutNullSeparator() {
        new SerializableRelationshipImpl("test#OUTGOING", "", null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldComplainAboutEmptySeparator() {
        new SerializableRelationshipImpl("test#OUTGOING", "", "");
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldComplainAboutEmptySeparator2() {
        new SerializableRelationshipImpl("test#OUTGOING", "", " ");
    }

    @Test
    public void shouldCorrectlyConvertToString() {
        assertEquals("_PRE_test#key1#value1#key2#value2", new SerializableRelationshipImpl("test#key1#value1#key2#value2", "#").toString("_PRE_", "#"));
        assertEquals("test", new SerializableRelationshipImpl("test", "#").toString("#"));
        assertEquals("test", new SerializableRelationshipImpl("test", "#").toString(null, "#"));
        assertEquals("test", new SerializableRelationshipImpl("test", "#").toString("", "#"));
        assertEquals("test", new SerializableRelationshipImpl("test#", "#").toString("#"));
    }

    @Test
    public void sameRelationshipsShouldBeEqual() {
        assertTrue(new SerializableRelationshipImpl("_PRE_test", "_PRE_", "#").equals(new SerializableRelationshipImpl("test", null, "#")));
        assertTrue(new SerializableRelationshipImpl("_PRE_test", "_PRE_", "#").equals(new SerializableRelationshipImpl("test", "", "#")));
        assertTrue(new SerializableRelationshipImpl("test#key1#value1#key2#value2", "#").equals(new SerializableRelationshipImpl("test#key1#value1#key2#value2", "#")));
        assertTrue(new SerializableRelationshipImpl("test#key1#value1#key2#", "#").equals(new SerializableRelationshipImpl("test#key1#value1#key2", "#")));
    }

    @Test
    public void differentRelationshipsShouldNotBeEqual() {
        assertFalse(new SerializableRelationshipImpl("test", "#").equals(new SerializableRelationshipImpl("test2", "#")));
        assertFalse(new SerializableRelationshipImpl("test2#key1#value1#key2#value2", "#").equals(new SerializableRelationshipImpl("test#key1#value1#key2#value2", "#")));
        assertFalse(new SerializableRelationshipImpl("test#key3#value1#key2#value2", "#").equals(new SerializableRelationshipImpl("test#key1#value1#key2#value2#", "#")));
    }
}
