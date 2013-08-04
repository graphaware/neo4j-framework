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

package com.graphaware.propertycontainer.dto.plain.relationship;

import com.graphaware.propertycontainer.dto.AbstractTest;
import com.graphaware.propertycontainer.dto.common.relationship.ImmutableRelationship;
import com.graphaware.propertycontainer.dto.plain.property.ImmutablePropertiesImpl;
import org.junit.Test;
import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.graphdb.Relationship;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.neo4j.graphdb.Direction.OUTGOING;
import static org.neo4j.graphdb.DynamicRelationshipType.withName;

/**
 * Unit test for {@link ImmutableRelationshipImpl}.
 */
public class ImmutableRelationshipImplTest extends AbstractTest {

    @Test
    public void canBeConstructedFromRelationship() {
        setUp();

        Relationship r = database.getNodeById(1).getSingleRelationship(withName("test"), OUTGOING);
        ImmutableRelationship relationship = new ImmutableRelationshipImpl(r);

        assertEquals("test", relationship.getType().name());
        assertEquals(2, relationship.getProperties().size());
        assertEquals(10000434132L, relationship.getProperties().get("prop3"));
        assertTrue(Arrays.equals(new long[]{3, 4, 5}, (long[]) relationship.getProperties().get("prop4")));
    }

    @Test
    public void canBeConstructedFromRelationshipAndProperties() {
        setUp();

        Relationship r = database.getNodeById(1).getSingleRelationship(withName("test"), OUTGOING);
        ImmutablePropertiesImpl p = new ImmutablePropertiesImpl(Collections.<String, Object>singletonMap("key", "value"));
        ImmutableRelationship relationship = new ImmutableRelationshipImpl(r, p);

        assertEquals("test", relationship.getType().name());
        assertEquals(1, relationship.getProperties().size());
        assertEquals("value", relationship.getProperties().get("key"));
    }

    @Test
    public void canBeConstructedFromRelationshipAndPropertiesAsMap() {
        setUp();

        Relationship r = database.getNodeById(1).getSingleRelationship(withName("test"), OUTGOING);
        ImmutableRelationship relationship = new ImmutableRelationshipImpl(r, Collections.<String, Object>singletonMap("key", "value"));

        assertEquals("test", relationship.getType().name());
        assertEquals(1, relationship.getProperties().size());
        assertEquals("value", relationship.getProperties().get("key"));
    }

    @Test
    public void canBeConstructedFromRelationshipType() {
        setUp();

        ImmutableRelationship relationship = new ImmutableRelationshipImpl(withName("test"));

        assertTrue(relationship.getProperties().isEmpty());
    }

    @Test
    public void canBeConstructedFromRelationshipTypeAndProperties() {
        ImmutablePropertiesImpl p = new ImmutablePropertiesImpl(Collections.<String, Object>singletonMap("key", "value"));
        ImmutableRelationship relationship = new ImmutableRelationshipImpl(withName("test"), p);

        assertEquals("test", relationship.getType().name());
        assertEquals(1, relationship.getProperties().size());
        assertEquals("value", relationship.getProperties().get("key"));
    }

    @Test
    public void canBeConstructedFromRelationshipTypeAndPropertiesAsMap() {
        ImmutableRelationship relationship = new ImmutableRelationshipImpl(withName("test"), Collections.<String, Object>singletonMap("key", "value"));

        assertEquals("test", relationship.getType().name());
        assertEquals(1, relationship.getProperties().size());
        assertEquals("value", relationship.getProperties().get("key"));
    }

    @Test
    public void canBeConstructedFromAnotherRelationshipRepresentation() {
        setUp();

        Relationship r = database.getNodeById(1).getSingleRelationship(withName("test"), OUTGOING);
        ImmutablePropertiesImpl p = new ImmutablePropertiesImpl(Collections.<String, Object>singletonMap("key", "value"));
        ImmutableRelationship relationship = new ImmutableRelationshipImpl(new ImmutableRelationshipImpl(r, p));

        assertEquals("test", relationship.getType().name());
        assertEquals(1, relationship.getProperties().size());
        assertEquals("value", relationship.getProperties().get("key"));
    }

    @Test
    public void testMatching() {
        setUp();

        Relationship r = database.getNodeById(1).getSingleRelationship(withName("test"), OUTGOING);

        Map<String, Object> props = new HashMap<>();
        props.put("prop3", 10000434132L);
        props.put("prop4", new long[]{3, 4, 5});

        assertTrue(new ImmutableRelationshipImpl(withName("test"), props).matches(r));
        assertTrue(new ImmutableRelationshipImpl(withName("test"), props).matches((PropertyContainer) r));
        assertFalse(new ImmutableRelationshipImpl(withName("test2"), props).matches(r));
        assertFalse(new ImmutableRelationshipImpl(withName("test2"), props).matches((PropertyContainer) r));

        assertTrue(new ImmutableRelationshipImpl(withName("test"), props).matches(new ImmutableRelationshipImpl(withName("test"), props)));
        assertFalse(new ImmutableRelationshipImpl(withName("test"), props).matches(new ImmutableRelationshipImpl(withName("test"), Collections.<String, Object>singletonMap("prop3", 10000434132L))));
        assertFalse(new ImmutableRelationshipImpl(withName("test"), props).matches(new ImmutableRelationshipImpl(withName("test2"), props)));

        props.put("prop3", 12312L);
        assertFalse(new ImmutableRelationshipImpl(withName("test"), props).matches(r));

        props.remove("prop4");
        assertFalse(new ImmutableRelationshipImpl(withName("test"), props).matches(r));
    }
}
