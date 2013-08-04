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
import com.graphaware.propertycontainer.dto.common.relationship.MutableDirectedRelationship;
import com.graphaware.propertycontainer.dto.string.property.ImmutablePropertiesImpl;
import com.graphaware.propertycontainer.dto.string.property.MutablePropertiesImpl;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static junit.framework.Assert.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.neo4j.graphdb.Direction.*;
import static org.neo4j.graphdb.DynamicRelationshipType.withName;

/**
 * Unit test for {@link com.graphaware.propertycontainer.dto.plain.relationship.ImmutableDirectedRelationshipImpl}.
 */
public class MutableDirectedRelationshipImplTest extends AbstractTest {

    @Override
    protected void additionalSetup() {
        database.getNodeById(2).createRelationshipTo(database.getNodeById(1), withName("test2"));
    }

    @Test
    public void shouldBeConstructedFromRelationshipAndNode() {
        setUp();

        ImmutableDirectedRelationship<String, ?> relationship = new MutableDirectedRelationshipImpl(database.getNodeById(1).getSingleRelationship(withName("test"), OUTGOING), database.getNodeById(1));

        assertEquals("test", relationship.getType().name());
        assertEquals(OUTGOING, relationship.getDirection());

        Map<String, String> props = new HashMap<>();
        props.put("prop3", "10000434132");
        props.put("prop4", "[3, 4, 5]");
        assertTrue(props.equals(relationship.getProperties().getProperties()));
    }

    @Test
    public void shouldBeConstructedFromRelationshipAndNode2() {
        setUp();

        ImmutableDirectedRelationship relationship = new MutableDirectedRelationshipImpl(database.getNodeById(1).getSingleRelationship(withName("test2"), INCOMING), database.getNodeById(1));

        assertEquals("test2", relationship.getType().name());
        assertEquals(INCOMING, relationship.getDirection());
        assertEquals(new ImmutablePropertiesImpl(Collections.<String, String>emptyMap()).getProperties(), relationship.getProperties().getProperties());
    }

    @Test
    public void shouldBeConstructedFromRelationshipNodeAndProperties() {
        setUp();

        MutablePropertiesImpl p = new MutablePropertiesImpl(Collections.<String, String>singletonMap("key", "value"));
        ImmutableDirectedRelationship relationship = new MutableDirectedRelationshipImpl(database.getNodeById(1).getSingleRelationship(withName("test"), OUTGOING), database.getNodeById(1), p);

        assertEquals("test", relationship.getType().name());
        assertEquals(OUTGOING, relationship.getDirection());
        assertEquals(1, relationship.getProperties().size());
        assertEquals("value", relationship.getProperties().get("key"));
    }

    @Test
    public void shouldBeConstructedFromRelationshipNodeAndPropertiesAsMap() {
        setUp();

        ImmutableDirectedRelationship relationship = new MutableDirectedRelationshipImpl(database.getNodeById(1).getSingleRelationship(withName("test"), OUTGOING), database.getNodeById(1), Collections.<String, String>singletonMap("key", "value"));

        assertEquals("test", relationship.getType().name());
        assertEquals(OUTGOING, relationship.getDirection());
        assertEquals(1, relationship.getProperties().size());
        assertEquals("value", relationship.getProperties().get("key"));
    }

    @Test
    public void shouldBeConstructedFromRelationshipTypeDirectionAndPropertiesAsMap() {
        setUp();

        ImmutableDirectedRelationship relationship = new MutableDirectedRelationshipImpl(withName("test"), OUTGOING, Collections.<String, String>singletonMap("key", "value"));

        assertEquals("test", relationship.getType().name());
        assertEquals(OUTGOING, relationship.getDirection());
        assertEquals(1, relationship.getProperties().size());
        assertEquals("value", relationship.getProperties().get("key"));
    }

    @Test
    public void shouldBeConstructedFromRelationshipTypeAndDirection() {
        ImmutableDirectedRelationship relationship = new MutableDirectedRelationshipImpl(withName("test"), OUTGOING);

        assertEquals("test", relationship.getType().name());
        assertEquals(OUTGOING, relationship.getDirection());
        assertTrue(relationship.getProperties().isEmpty());
    }

    @Test
    public void shouldBeConstructedFromRelationshipTypeDirectionAndProperties() {
        Map<String, String> props = new HashMap<>();
        props.put("prop3", "10000434132");
        props.put("prop4", "[3, 4, 5]");

        ImmutableDirectedRelationship relationship = new MutableDirectedRelationshipImpl(withName("test"), OUTGOING, new MutablePropertiesImpl(props));

        assertEquals("test", relationship.getType().name());
        assertEquals(OUTGOING, relationship.getDirection());
        assertEquals(props, relationship.getProperties().getProperties());
    }

    @Test
    public void shouldBeConstructedFromAnotherRelationship() {
        setUp();

        ImmutableDirectedRelationship<String, ?> relationship = new MutableDirectedRelationshipImpl(new ImmutableDirectedRelationshipImpl(database.getNodeById(1).getSingleRelationship(withName("test"), OUTGOING), database.getNodeById(1)));

        assertEquals("test", relationship.getType().name());
        assertEquals(OUTGOING, relationship.getDirection());

        Map<String, String> props = new HashMap<>();
        props.put("prop3", "10000434132");
        props.put("prop4", "[3, 4, 5]");
        assertEquals(props, relationship.getProperties().getProperties());
    }

    @Test
    public void sameRelationshipsShouldBeEqual() {
        assertTrue(new MutableDirectedRelationshipImpl(withName("test"), INCOMING).equals(new MutableDirectedRelationshipImpl(withName("test"), INCOMING)));
        assertTrue(new MutableDirectedRelationshipImpl(withName("test"), INCOMING, Collections.<String, String>singletonMap("key", "value")).equals(new MutableDirectedRelationshipImpl(withName("test"), INCOMING, Collections.<String, String>singletonMap("key", "value"))));
    }

    @Test
    public void differentRelationshipsShouldNotBeEqual() {
        assertFalse(new MutableDirectedRelationshipImpl(withName("test"), INCOMING).equals(new MutableDirectedRelationshipImpl(withName("test"), OUTGOING)));
        assertFalse(new MutableDirectedRelationshipImpl(withName("test"), INCOMING, Collections.<String, String>singletonMap("key", "value2")).equals(new MutableDirectedRelationshipImpl(withName("test"), INCOMING, Collections.<String, String>singletonMap("key", "value"))));
    }

    @Test
    public void propertiesCanBeSet() {
        MutableDirectedRelationship<String, ?> relationship = new MutableDirectedRelationshipImpl(withName("test"), OUTGOING, Collections.<String, String>singletonMap("key", "value"));

        relationship.setProperty("key2", "value2");
        assertEquals(2, relationship.getProperties().size());
        assertEquals("value", relationship.getProperties().get("key"));
        assertEquals("value2", relationship.getProperties().get("key2"));
    }

    @Test
    public void propertiesCanBeRemoved() {
        MutableDirectedRelationship<String, ?> relationship = new MutableDirectedRelationshipImpl(withName("test"), OUTGOING, Collections.<String, String>singletonMap("key", "value"));

        relationship.removeProperty("key");
        assertEquals(0, relationship.getProperties().size());
    }

    @Test
    public void testDirectionResolution() {
        setUp();

        ImmutableDirectedRelationship relationship = new MutableDirectedRelationshipImpl(database.getNodeById(2).getSingleRelationship(withName("cycle"), OUTGOING), database.getNodeById(2));

        assertEquals("cycle", relationship.getType().name());
        assertEquals(BOTH, relationship.getDirection());
    }
}
