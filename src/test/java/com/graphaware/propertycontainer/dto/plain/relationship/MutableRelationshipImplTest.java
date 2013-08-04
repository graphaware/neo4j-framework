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
import com.graphaware.propertycontainer.dto.common.relationship.MutableRelationship;
import com.graphaware.propertycontainer.dto.plain.property.MutablePropertiesImpl;
import org.junit.Test;
import org.neo4j.graphdb.Relationship;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.neo4j.graphdb.Direction.OUTGOING;
import static org.neo4j.graphdb.DynamicRelationshipType.withName;

/**
 * Unit test for {@link com.graphaware.propertycontainer.dto.string.relationship.MutableRelationshipImpl}.
 */
public class MutableRelationshipImplTest extends AbstractTest {

    @Test
    public void shouldBeConstructedFromRelationship() {
        setUp();

        Relationship r = database.getNodeById(1).getSingleRelationship(withName("test"), OUTGOING);
        MutableRelationship relationship = new MutableRelationshipImpl(r);

        assertEquals("test", relationship.getType().name());
        assertEquals(2, relationship.getProperties().size());
        assertEquals(10000434132L, relationship.getProperties().get("prop3"));
        assertTrue(Arrays.equals(new long[]{3, 4, 5}, (long[]) relationship.getProperties().get("prop4")));
    }

    @Test
    public void shouldBeConstructedFromRelationshipAndProperties() {
        setUp();

        Relationship r = database.getNodeById(1).getSingleRelationship(withName("test"), OUTGOING);
        MutablePropertiesImpl p = new MutablePropertiesImpl(Collections.<String, Object>singletonMap("key", "value"));
        MutableRelationship relationship = new MutableRelationshipImpl(r, p);

        assertEquals("test", relationship.getType().name());
        assertEquals(1, relationship.getProperties().size());
        assertEquals("value", relationship.getProperties().get("key"));
    }

    @Test
    public void shouldBeConstructedFromRelationshipAndPropertiesAsMap() {
        setUp();

        Relationship r = database.getNodeById(1).getSingleRelationship(withName("test"), OUTGOING);
        MutableRelationship relationship = new MutableRelationshipImpl(r, Collections.<String, Object>singletonMap("key", "value"));

        assertEquals("test", relationship.getType().name());
        assertEquals(1, relationship.getProperties().size());
        assertEquals("value", relationship.getProperties().get("key"));
    }

    @Test
    public void shouldBeConstructedFromRelationshipType() {
        setUp();

        MutableRelationship relationship = new MutableRelationshipImpl(withName("test"));

        assertTrue(relationship.getProperties().isEmpty());
    }

    @Test
    public void shouldBeConstructedFromRelationshipTypeAndProperties() {
        MutablePropertiesImpl p = new MutablePropertiesImpl(Collections.<String, Object>singletonMap("key", "value"));
        MutableRelationship relationship = new MutableRelationshipImpl(withName("test"), p);

        assertEquals("test", relationship.getType().name());
        assertEquals(1, relationship.getProperties().size());
        assertEquals("value", relationship.getProperties().get("key"));
    }

    @Test
    public void shouldBeConstructedFromRelationshipTypeAndPropertiesAsMap() {
        MutableRelationship relationship = new MutableRelationshipImpl(withName("test"), Collections.<String, Object>singletonMap("key", "value"));

        assertEquals("test", relationship.getType().name());
        assertEquals(1, relationship.getProperties().size());
        assertEquals("value", relationship.getProperties().get("key"));
    }

    @Test
    public void shouldBeConstructedFromAnotherRelationshipRepresentation() {
        setUp();

        Relationship r = database.getNodeById(1).getSingleRelationship(withName("test"), OUTGOING);
        MutablePropertiesImpl p = new MutablePropertiesImpl(Collections.<String, Object>singletonMap("key", "value"));
        MutableRelationship relationship = new MutableRelationshipImpl(new MutableRelationshipImpl(r, p));

        assertEquals("test", relationship.getType().name());
        assertEquals(1, relationship.getProperties().size());
        assertEquals("value", relationship.getProperties().get("key"));
    }

    @Test
    public void propertiesCanBeSet() {
        MutableRelationship relationship = new MutableRelationshipImpl(withName("test"), Collections.<String, Object>singletonMap("key", "value"));

        relationship.setProperty("key2", "value2");
        assertEquals(2, relationship.getProperties().size());
        assertEquals("value", relationship.getProperties().get("key"));
        assertEquals("value2", relationship.getProperties().get("key2"));
    }

    @Test
    public void propertiesCanBeRemoved() {
        MutableRelationship relationship = new MutableRelationshipImpl(withName("test"), Collections.<String, Object>singletonMap("key", "value"));

        relationship.removeProperty("key");
        assertEquals(0, relationship.getProperties().size());
    }
}
