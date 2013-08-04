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
import com.graphaware.propertycontainer.dto.common.relationship.ImmutableDirectedRelationship;
import com.graphaware.propertycontainer.dto.common.relationship.ImmutableRelationship;
import com.graphaware.propertycontainer.dto.string.property.CopyMakingSerializablePropertiesImpl;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.neo4j.graphdb.Direction.INCOMING;
import static org.neo4j.graphdb.Direction.OUTGOING;
import static org.neo4j.graphdb.DynamicRelationshipType.withName;

/**
 * Unit test for {@link BaseCopyMakingSerializableDirectedRelationship}.
 */
public class BaseCopyMakingSerializableDirectedRelationshipTest extends AbstractTest {

    @Override
    protected void additionalSetup() {
        database.getNodeById(2).createRelationshipTo(database.getNodeById(1), withName("test2"));
    }

    @Test
    public void shouldBeConstructedFromRelationshipAndNode() {
        setUp();

        ImmutableDirectedRelationship relationship = new TestBaseCopyMakingSerializableDirectedRelationship(database.getNodeById(1).getSingleRelationship(withName("test"), OUTGOING), database.getNodeById(1));

        assertEquals("test", relationship.getType().name());
        assertEquals(OUTGOING, relationship.getDirection());
        assertEquals(new CopyMakingSerializablePropertiesImpl("_PRE_prop3#10000434132#prop4#[3, 4, 5]", "_PRE_", "#"), relationship.getProperties());
    }

    @Test
    public void shouldBeConstructedFromRelationshipAndNode2() {
        setUp();

        ImmutableDirectedRelationship relationship = new TestBaseCopyMakingSerializableDirectedRelationship(database.getNodeById(1).getSingleRelationship(withName("test2"), INCOMING), database.getNodeById(1));

        assertEquals("test2", relationship.getType().name());
        assertEquals(INCOMING, relationship.getDirection());
        assertEquals(new CopyMakingSerializablePropertiesImpl("", "#"), relationship.getProperties());
    }

    @Test
    public void shouldBeConstructedFromRelationshipNodeAndProperties() {
        setUp();

        CopyMakingSerializablePropertiesImpl p = new CopyMakingSerializablePropertiesImpl(Collections.singletonMap("key", "value"));
        ImmutableDirectedRelationship relationship = new TestBaseCopyMakingSerializableDirectedRelationship(database.getNodeById(1).getSingleRelationship(withName("test"), OUTGOING), database.getNodeById(1), p);

        assertEquals("test", relationship.getType().name());
        assertEquals(OUTGOING, relationship.getDirection());
        assertEquals(1, relationship.getProperties().size());
        assertEquals("value", relationship.getProperties().get("key"));
    }

    @Test
    public void shouldBeConstructedFromRelationshipNodeAndPropertiesAsMap() {
        setUp();

        ImmutableDirectedRelationship relationship = new TestBaseCopyMakingSerializableDirectedRelationship(database.getNodeById(1).getSingleRelationship(withName("test"), OUTGOING), database.getNodeById(1), Collections.singletonMap("key", "value"));

        assertEquals("test", relationship.getType().name());
        assertEquals(OUTGOING, relationship.getDirection());
        assertEquals(1, relationship.getProperties().size());
        assertEquals("value", relationship.getProperties().get("key"));
    }

    @Test
    public void shouldBeConstructedFromRelationshipTypeDirectionAndPropertiesAsMap() {
        setUp();

        ImmutableDirectedRelationship relationship = new TestBaseCopyMakingSerializableDirectedRelationship(withName("test"), OUTGOING, Collections.singletonMap("key", "value"));

        assertEquals("test", relationship.getType().name());
        assertEquals(OUTGOING, relationship.getDirection());
        assertEquals(1, relationship.getProperties().size());
        assertEquals("value", relationship.getProperties().get("key"));
    }

    @Test
    public void shouldBeConstructedFromRelationshipTypeAndDirection() {
        ImmutableDirectedRelationship relationship = new TestBaseCopyMakingSerializableDirectedRelationship(withName("test"), OUTGOING);

        assertEquals("test", relationship.getType().name());
        assertEquals(OUTGOING, relationship.getDirection());
        assertTrue(relationship.getProperties().isEmpty());
    }

    @Test
    public void shouldBeConstructedFromRelationshipTypeDirectionAndProperties() {
        ImmutableDirectedRelationship relationship = new TestBaseCopyMakingSerializableDirectedRelationship(withName("test"), OUTGOING, new CopyMakingSerializablePropertiesImpl("key1#value1#key2#value2", "#"));

        assertEquals("test", relationship.getType().name());
        assertEquals(OUTGOING, relationship.getDirection());
        assertEquals(new CopyMakingSerializablePropertiesImpl("key1#value1#key2#value2", "#"), relationship.getProperties());
    }

    @Test
    public void shouldBeConstructedFromAnotherRelationship() {
        setUp();

        ImmutableDirectedRelationship relationship = new TestBaseCopyMakingSerializableDirectedRelationship(new TestBaseCopyMakingSerializableDirectedRelationship(database.getNodeById(1).getSingleRelationship(withName("test"), OUTGOING), database.getNodeById(1)));

        assertEquals("test", relationship.getType().name());
        assertEquals(OUTGOING, relationship.getDirection());
        assertEquals(new CopyMakingSerializablePropertiesImpl("prop3#10000434132#prop4#[3, 4, 5]", "#"), relationship.getProperties());
    }

    @Test
    public void shouldBeCorrectlyConstructedFromString() {
        ImmutableDirectedRelationship relationship = new TestBaseCopyMakingSerializableDirectedRelationship("test#OUTGOING#key1#value1#key2#value2", "#");

        assertEquals("test", relationship.getType().name());
        assertEquals(OUTGOING, relationship.getDirection());
        assertEquals(new CopyMakingSerializablePropertiesImpl("key1#value1#key2#value2", "#"), relationship.getProperties());
    }

    @Test
    public void shouldBeCorrectlyConstructedFromString2() {
        ImmutableDirectedRelationship relationship = new TestBaseCopyMakingSerializableDirectedRelationship("_PRE_test#OUTGOING#key1#value1#key2#value2", "_PRE_", "#");

        assertEquals("test", relationship.getType().name());
        assertEquals(OUTGOING, relationship.getDirection());
        assertEquals(new CopyMakingSerializablePropertiesImpl("key1#value1#key2#value2", "#"), relationship.getProperties());
    }

    @Test
    public void shouldCorrectlyExcludeProperty() {
        ImmutableRelationship relationship = new TestBaseCopyMakingSerializableDirectedRelationship(withName("test"), OUTGOING, new CopyMakingSerializablePropertiesImpl("key1#value1#key2#value2", "#")).without("key1");

        assertEquals("value2", relationship.getProperties().get("key2"));
        assertEquals(1, relationship.getProperties().size());
    }

    @Test
    public void whenMissingPropertyToExcludeReturnIdentity() {
        TestBaseCopyMakingSerializableDirectedRelationship relationship = new TestBaseCopyMakingSerializableDirectedRelationship(withName("test"), OUTGOING, new CopyMakingSerializablePropertiesImpl("key1#value1#key2#value2", "#")).without("keyX");

        assertEquals("value1", relationship.getProperties().get("key1"));
        assertEquals("value2", relationship.getProperties().get("key2"));
        assertEquals(2, relationship.getProperties().size());
        assertTrue(relationship.equals(relationship.without("keyX")));
    }

    @Test
    public void shouldCorrectlyAddProperty() {
        ImmutableRelationship relationship = new TestBaseCopyMakingSerializableDirectedRelationship(withName("test"), OUTGOING, new CopyMakingSerializablePropertiesImpl("key1#value1#key2#value2", "#")).with("key3", "value3");

        assertEquals("value1", relationship.getProperties().get("key1"));
        assertEquals("value2", relationship.getProperties().get("key2"));
        assertEquals("value3", relationship.getProperties().get("key3"));
        assertEquals(3, relationship.getProperties().size());
    }
}
