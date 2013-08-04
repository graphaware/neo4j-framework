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
import static junit.framework.Assert.assertNotSame;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.neo4j.graphdb.Direction.INCOMING;
import static org.neo4j.graphdb.Direction.OUTGOING;
import static org.neo4j.graphdb.DynamicRelationshipType.withName;

/**
 * Unit test for {@link SerializableDirectedRelationshipImpl}.
 */
public class SerializableDirectedRelationshipImplTest extends AbstractTest {

    @Override
    protected void additionalSetup() {
        database.getNodeById(2).createRelationshipTo(database.getNodeById(1), withName("test2"));
    }

    @Test
    public void shouldBeConstructedFromRelationshipAndNode() {
        setUp();

        SerializableDirectedRelationship relationship = new SerializableDirectedRelationshipImpl(database.getNodeById(1).getSingleRelationship(withName("test"), OUTGOING), database.getNodeById(1));

        assertEquals("test", relationship.getType().name());
        assertEquals(OUTGOING, relationship.getDirection());
        assertEquals(new SerializablePropertiesImpl("prop3#10000434132#prop4#[3, 4, 5]", "#"), relationship.getProperties());
    }

    @Test
    public void shouldBeConstructedFromRelationshipAndNode2() {
        setUp();

        SerializableDirectedRelationship relationship = new SerializableDirectedRelationshipImpl(database.getNodeById(1).getSingleRelationship(withName("test2"), INCOMING), database.getNodeById(1));

        assertEquals("test2", relationship.getType().name());
        assertEquals(INCOMING, relationship.getDirection());
        assertEquals(new SerializablePropertiesImpl("", "#"), relationship.getProperties());
    }

    @Test
    public void shouldBeConstructedFromRelationshipNodeAndProperties() {
        setUp();

        SerializableProperties p = new SerializablePropertiesImpl(Collections.singletonMap("key", "value"));
        SerializableDirectedRelationship relationship = new SerializableDirectedRelationshipImpl(database.getNodeById(1).getSingleRelationship(withName("test"), OUTGOING), database.getNodeById(1), p);

        assertEquals("test", relationship.getType().name());
        assertEquals(OUTGOING, relationship.getDirection());
        assertEquals(1, relationship.getProperties().size());
        assertEquals("value", relationship.getProperties().get("key"));
    }

    @Test
    public void shouldBeConstructedFromRelationshipNodeAndPropertiesAsMap() {
        setUp();

        SerializableDirectedRelationship relationship = new SerializableDirectedRelationshipImpl(database.getNodeById(1).getSingleRelationship(withName("test"), OUTGOING), database.getNodeById(1), Collections.singletonMap("key", "value"));

        assertEquals("test", relationship.getType().name());
        assertEquals(OUTGOING, relationship.getDirection());
        assertEquals(1, relationship.getProperties().size());
        assertEquals("value", relationship.getProperties().get("key"));
    }

    @Test
    public void shouldBeConstructedFromRelationshipTypeDirectionAndPropertiesAsMap() {
        setUp();

        SerializableDirectedRelationship relationship = new SerializableDirectedRelationshipImpl(withName("test"), OUTGOING, Collections.singletonMap("key", "value"));

        assertEquals("test", relationship.getType().name());
        assertEquals(OUTGOING, relationship.getDirection());
        assertEquals(1, relationship.getProperties().size());
        assertEquals("value", relationship.getProperties().get("key"));
    }

    @Test
    public void shouldBeConstructedFromRelationshipTypeAndDirection() {
        SerializableDirectedRelationship relationship = new SerializableDirectedRelationshipImpl(withName("test"), OUTGOING);

        assertEquals("test", relationship.getType().name());
        assertEquals(OUTGOING, relationship.getDirection());
        assertTrue(relationship.getProperties().isEmpty());
    }

    @Test
    public void shouldBeConstructedFromRelationshipTypeDirectionAndProperties() {
        SerializableDirectedRelationship relationship = new SerializableDirectedRelationshipImpl(withName("test"), OUTGOING, new SerializablePropertiesImpl("key1#value1#key2#value2", "#"));

        assertEquals("test", relationship.getType().name());
        assertEquals(OUTGOING, relationship.getDirection());
        assertEquals(new SerializablePropertiesImpl("key1#value1#key2#value2", "#"), relationship.getProperties());
    }

    @Test
    public void shouldBeConstructedFromAnotherRelationship() {
        setUp();

        SerializableDirectedRelationship relationship = new SerializableDirectedRelationshipImpl(new SerializableDirectedRelationshipImpl(database.getNodeById(1).getSingleRelationship(withName("test"), OUTGOING), database.getNodeById(1)));

        assertEquals("test", relationship.getType().name());
        assertEquals(OUTGOING, relationship.getDirection());
        assertEquals(new SerializablePropertiesImpl("prop3#10000434132#prop4#[3, 4, 5]", "#"), relationship.getProperties());
    }

    @Test
    public void shouldBeCorrectlyConstructedFromString() {
        SerializableDirectedRelationship relationship = new SerializableDirectedRelationshipImpl("test#OUTGOING#key1#value1#key2#value2", "#");

        assertEquals("test", relationship.getType().name());
        assertEquals(OUTGOING, relationship.getDirection());
        assertEquals(new SerializablePropertiesImpl("key1#value1#key2#value2", "#"), relationship.getProperties());
    }

    @Test
    public void shouldBeCorrectlyConstructedFromString2() {
        SerializableDirectedRelationship relationship = new SerializableDirectedRelationshipImpl("_PRE_test#OUTGOING#key1#value1#key2#value2", "_PRE_", "#");

        assertEquals("test", relationship.getType().name());
        assertEquals(OUTGOING, relationship.getDirection());
        assertEquals(new SerializablePropertiesImpl("key1#value1#key2#value2", "#"), relationship.getProperties());
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWhenTypeIsEmpty() {
        new SerializableDirectedRelationshipImpl("#OUTGOING#key1#value1#key2#value2", "#");
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWhenDirectionIsInvalid() {
        new SerializableDirectedRelationshipImpl("_PRE_test#INVALID#key1#value1#key2#value2", "_PRE_");
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWhenDirectionIsMissing() {
        new SerializableDirectedRelationshipImpl("test#", "#");
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWhenDirectionIsMissing2() {
        new SerializableDirectedRelationshipImpl("test", "#");
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWhenNoInfoProvided() {
        new SerializableDirectedRelationshipImpl("", "#");
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWhenNoInfoProvided2() {
        new SerializableDirectedRelationshipImpl("_PRE_", "_PRE_");
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWhenPropertiesAreInvalid() {
        new SerializableDirectedRelationshipImpl("test#OUTGOING##value1#key2#value2", "#");
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWhenPrefixIncorrect() {
        new SerializableDirectedRelationshipImpl("test#OUTGOING#key1#value1#key2#value2", "_PRE_", "#");
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldComplainAboutNullSeparator() {
        new SerializableDirectedRelationshipImpl("test#OUTGOING", "", null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldComplainAboutEmptySeparator() {
        new SerializableDirectedRelationshipImpl("test#OUTGOING", "", "");
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldComplainAboutEmptySeparator2() {
        new SerializableDirectedRelationshipImpl("test#OUTGOING", "", " ");
    }

    @Test
    public void shouldCorrectlyConvertToString() {
        assertEquals("test_OUTGOING_key1_value1_key2_value2", new SerializableDirectedRelationshipImpl("test#OUTGOING#key1#value1#key2#value2", "#").toString("_"));
        assertEquals("_PRE_test#INCOMING", new SerializableDirectedRelationshipImpl("test#INCOMING", "#").toString("_PRE_", "#"));
        assertEquals("test#INCOMING", new SerializableDirectedRelationshipImpl("test#INCOMING#", "#").toString("#"));
    }

    @Test
    public void sameRelationshipsShouldBeEqual() {
        assertTrue(new SerializableDirectedRelationshipImpl("_PRE_test#INCOMING", "_PRE_", "#").equals(new SerializableDirectedRelationshipImpl("test#INCOMING", "#")));
        assertTrue(new SerializableDirectedRelationshipImpl("test#OUTGOING#key1#value1#key2#value2", "#").equals(new SerializableDirectedRelationshipImpl("test#OUTGOING#key1#value1#key2#value2", "#")));
        assertTrue(new SerializableDirectedRelationshipImpl("test#OUTGOING#key1#value1#key2#value2", "#").equals(new SerializableDirectedRelationshipImpl("test#OUTGOING#key1#value1#key2#value2#", "#")));
        assertTrue(new SerializableDirectedRelationshipImpl("test#INCOMING#key1#value1#key2#", "#").equals(new SerializableDirectedRelationshipImpl("test#INCOMING#key1#value1#key2", "#")));
    }

    @Test
    public void differentRelationshipsShouldNotBeEqual() {
        assertFalse(new SerializableDirectedRelationshipImpl("test#OUTGOING", "#").equals(new SerializableDirectedRelationshipImpl("test#INCOMING", "#")));
        assertFalse(new SerializableDirectedRelationshipImpl("test2#OUTGOING#key1#value1#key2#value2", "#").equals(new SerializableDirectedRelationshipImpl("test#OUTGOING#key1#value1#key2#value2", "#")));
        assertFalse(new SerializableDirectedRelationshipImpl("test#OUTGOING#key3#value1#key2#value2", "#").equals(new SerializableDirectedRelationshipImpl("test#OUTGOING#key1#value1#key2#value2#", "#")));
    }

    @Test
    public void sameRelationshipsShouldHaveSameHashCode() {
        assertEquals(new SerializableDirectedRelationshipImpl("test#OUTGOING#key1#value1#key2#value2", "#").hashCode(), new SerializableDirectedRelationshipImpl("test#OUTGOING#key1#value1#key2#value2", "#").hashCode());
    }

    @Test
    public void differentRelationshipsShouldHaveDifferentHashCode() {
        assertNotSame(new SerializableDirectedRelationshipImpl("test#OUTGOING#key1#value1#key2#value2", "#").hashCode(), new SerializableDirectedRelationshipImpl("test#OUTGOING#key1#value1#key2#value2", "#").hashCode());
    }
}
