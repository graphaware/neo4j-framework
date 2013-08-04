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
import com.graphaware.propertycontainer.dto.string.property.ImmutablePropertiesImpl;
import com.graphaware.propertycontainer.dto.string.property.SerializablePropertiesImpl;
import org.junit.Test;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import java.util.Collections;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.fail;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.neo4j.graphdb.Direction.*;
import static org.neo4j.graphdb.DynamicRelationshipType.withName;
import static org.neo4j.helpers.collection.MapUtil.stringMap;

/**
 * Unit test for {@link ImmutableDirectedRelationshipImpl}.
 */
public class ImmutableDirectedRelationshipImplTest extends AbstractTest {

    @Override
    protected void additionalSetup() {
        database.getNodeById(2).createRelationshipTo(database.getNodeById(1), withName("test2"));
    }

    @Test
    public void shouldBeConstructedFromRelationshipAndNode() {
        setUp();

        ImmutableDirectedRelationship relationship = new ImmutableDirectedRelationshipImpl(database.getNodeById(1).getSingleRelationship(withName("test"), OUTGOING), database.getNodeById(1));

        assertEquals("test", relationship.getType().name());
        assertEquals(OUTGOING, relationship.getDirection());
        assertEquals(new SerializablePropertiesImpl("prop3#10000434132#prop4#[3, 4, 5]", "#").getProperties(), relationship.getProperties().getProperties());
    }

    @Test
    public void shouldBeConstructedFromRelationshipAndNode2() {
        setUp();

        ImmutableDirectedRelationship relationship = new ImmutableDirectedRelationshipImpl(database.getNodeById(1).getSingleRelationship(withName("test2"), INCOMING), database.getNodeById(1));

        assertEquals("test2", relationship.getType().name());
        assertEquals(INCOMING, relationship.getDirection());
        assertEquals(new SerializablePropertiesImpl("", "#").getProperties(), relationship.getProperties().getProperties());
    }

    @Test
    public void shouldBeConstructedFromRelationshipNodeAndProperties() {
        setUp();

        ImmutablePropertiesImpl p = new ImmutablePropertiesImpl(Collections.singletonMap("key", "value"));
        ImmutableDirectedRelationship relationship = new ImmutableDirectedRelationshipImpl(database.getNodeById(1).getSingleRelationship(withName("test"), OUTGOING), database.getNodeById(1), p);

        assertEquals("test", relationship.getType().name());
        assertEquals(OUTGOING, relationship.getDirection());
        assertEquals(1, relationship.getProperties().size());
        assertEquals("value", relationship.getProperties().get("key"));
    }

    @Test
    public void shouldBeConstructedFromRelationshipNodeAndPropertiesAsMap() {
        setUp();

        ImmutableDirectedRelationship relationship = new ImmutableDirectedRelationshipImpl(database.getNodeById(1).getSingleRelationship(withName("test"), OUTGOING), database.getNodeById(1), Collections.singletonMap("key", "value"));

        assertEquals("test", relationship.getType().name());
        assertEquals(OUTGOING, relationship.getDirection());
        assertEquals(1, relationship.getProperties().size());
        assertEquals("value", relationship.getProperties().get("key"));
    }

    @Test
    public void shouldBeConstructedFromRelationshipTypeDirectionAndPropertiesAsMap() {
        setUp();

        ImmutableDirectedRelationship relationship = new ImmutableDirectedRelationshipImpl(withName("test"), OUTGOING, Collections.singletonMap("key", "value"));

        assertEquals("test", relationship.getType().name());
        assertEquals(OUTGOING, relationship.getDirection());
        assertEquals(1, relationship.getProperties().size());
        assertEquals("value", relationship.getProperties().get("key"));
    }

    @Test
    public void shouldBeConstructedFromRelationshipTypeAndDirection() {
        ImmutableDirectedRelationship relationship = new ImmutableDirectedRelationshipImpl(withName("test"), OUTGOING);

        assertEquals("test", relationship.getType().name());
        assertEquals(OUTGOING, relationship.getDirection());
        assertTrue(relationship.getProperties().isEmpty());
    }

    @Test
    public void shouldBeConstructedFromRelationshipTypeDirectionAndProperties() {
        ImmutableDirectedRelationship relationship = new ImmutableDirectedRelationshipImpl(withName("test"), OUTGOING, new SerializablePropertiesImpl("key1#value1#key2#value2", "#"));

        assertEquals("test", relationship.getType().name());
        assertEquals(OUTGOING, relationship.getDirection());
        assertEquals(new SerializablePropertiesImpl("key1#value1#key2#value2", "#").getProperties(), relationship.getProperties().getProperties());
    }

    @Test
    public void shouldBeConstructedFromAnotherRelationship() {
        setUp();

        ImmutableDirectedRelationship relationship = new ImmutableDirectedRelationshipImpl(new ImmutableDirectedRelationshipImpl(database.getNodeById(1).getSingleRelationship(withName("test"), OUTGOING), database.getNodeById(1)));

        assertEquals("test", relationship.getType().name());
        assertEquals(OUTGOING, relationship.getDirection());
        assertEquals(new SerializablePropertiesImpl("prop3#10000434132#prop4#[3, 4, 5]", "#").getProperties(), relationship.getProperties().getProperties());
    }

    @Test
    public void shouldBeConstructedFromSubclass() {
        setUp();

        ImmutableDirectedRelationship relationship = new ImmutableDirectedRelationshipImpl(new SerializableDirectedRelationshipImpl(database.getNodeById(1).getSingleRelationship(withName("test"), OUTGOING), database.getNodeById(1)));

        assertEquals("test", relationship.getType().name());
        assertEquals(OUTGOING, relationship.getDirection());
        assertEquals(new SerializablePropertiesImpl("prop3#10000434132#prop4#[3, 4, 5]", "#").getProperties(), relationship.getProperties().getProperties());
    }

    @Test
    public void sameRelationshipsShouldBeEqual() {
        assertTrue(new ImmutableDirectedRelationshipImpl(new SerializableDirectedRelationshipImpl("test#INCOMING", "#")).equals(new ImmutableDirectedRelationshipImpl(new SerializableDirectedRelationshipImpl("test#INCOMING", "#"))));
        assertTrue(new ImmutableDirectedRelationshipImpl(new SerializableDirectedRelationshipImpl("test#OUTGOING#key1#value1#key2#value2", "#")).equals(new ImmutableDirectedRelationshipImpl(new SerializableDirectedRelationshipImpl("test#OUTGOING#key1#value1#key2#value2", "#"))));
        assertTrue(new ImmutableDirectedRelationshipImpl(new SerializableDirectedRelationshipImpl("test#OUTGOING#key1#value1#key2#value2", "#")).equals(new ImmutableDirectedRelationshipImpl(new SerializableDirectedRelationshipImpl("test#OUTGOING#key1#value1#key2#value2#", "#"))));
        assertTrue(new ImmutableDirectedRelationshipImpl(new SerializableDirectedRelationshipImpl("_PRE_" + "test#INCOMING#key1#value1#key2#", "_PRE_", "#")).equals(new ImmutableDirectedRelationshipImpl(new SerializableDirectedRelationshipImpl("test#INCOMING#key1#value1#key2", "#"))));
    }

    @Test
    public void differentRelationshipsShouldNotBeEqual() {
        assertFalse(new ImmutableDirectedRelationshipImpl(new SerializableDirectedRelationshipImpl("test#OUTGOING", "#")).equals(new ImmutableDirectedRelationshipImpl(new SerializableDirectedRelationshipImpl("test#INCOMING", "#"))));
        assertFalse(new ImmutableDirectedRelationshipImpl(new SerializableDirectedRelationshipImpl("test2#OUTGOING#key1#value1#key2#value2", "#")).equals(new ImmutableDirectedRelationshipImpl(new SerializableDirectedRelationshipImpl("test#OUTGOING#key1#value1#key2#value2", "#"))));
        assertFalse(new ImmutableDirectedRelationshipImpl(new SerializableDirectedRelationshipImpl("test#OUTGOING#key3#value1#key2#value2", "#")).equals(new ImmutableDirectedRelationshipImpl(new SerializableDirectedRelationshipImpl("test#OUTGOING#key1#value1#key2#value2#", "#"))));
    }

    @Test
    public void testMatching() {
        setUp();

        Node node = database.getNodeById(1);
        Relationship r = node.getSingleRelationship(withName("test"), OUTGOING);

        assertTrue(new ImmutableDirectedRelationshipImpl(withName("test"), OUTGOING, stringMap("prop3", "10000434132", "prop4", "[3, 4, 5]")).matches(r, node));
        assertTrue(new ImmutableDirectedRelationshipImpl(withName("test"), BOTH, stringMap("prop3", "10000434132", "prop4", "[3, 4, 5]")).matches(r, node));
        assertFalse(new ImmutableDirectedRelationshipImpl(withName("test"), INCOMING, stringMap("prop3", "10000434132", "prop4", "[3, 4, 5]")).matches(r, node));
        assertFalse(new ImmutableDirectedRelationshipImpl(withName("test2"), OUTGOING, stringMap("prop3", "10000434132", "prop4", "[3, 4, 5]")).matches(r, node));
        assertFalse(new ImmutableDirectedRelationshipImpl(withName("test"), OUTGOING, stringMap("prop3", "10000434132")).matches(r, node));
        assertFalse(new ImmutableDirectedRelationshipImpl(withName("test"), OUTGOING, stringMap("prop3", "10000434133", "prop4", "[3, 4, 5]")).matches(r, node));

        try {
            new ImmutableDirectedRelationshipImpl(withName("test"), OUTGOING, stringMap("prop3", "10000434133", "prop4", "[3, 4, 5]")).matches(r);
            fail();
        } catch (UnsupportedOperationException e) {
            //ok
        }
    }

    @Test
    public void testDirectionResolution() {
        setUp();

        ImmutableDirectedRelationship relationship = new ImmutableDirectedRelationshipImpl(database.getNodeById(2).getSingleRelationship(withName("cycle"), OUTGOING), database.getNodeById(2));

        assertEquals("cycle", relationship.getType().name());
        assertEquals(BOTH, relationship.getDirection());
    }
}
