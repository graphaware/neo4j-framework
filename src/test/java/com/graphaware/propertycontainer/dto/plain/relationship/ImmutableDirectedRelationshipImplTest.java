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
import com.graphaware.propertycontainer.dto.common.relationship.ImmutableDirectedRelationship;
import com.graphaware.propertycontainer.dto.common.relationship.TypeAndDirection;
import com.graphaware.propertycontainer.dto.plain.property.ImmutablePropertiesImpl;
import com.graphaware.propertycontainer.util.ArrayUtils;
import org.junit.Test;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.graphdb.Relationship;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.fail;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.neo4j.graphdb.Direction.*;
import static org.neo4j.graphdb.DynamicRelationshipType.withName;

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

        ImmutableDirectedRelationship<Object, ?> relationship = new ImmutableDirectedRelationshipImpl(database.getNodeById(1).getSingleRelationship(withName("test"), OUTGOING), database.getNodeById(1));

        assertEquals("test", relationship.getType().name());
        assertEquals(OUTGOING, relationship.getDirection());

        Map<String, Object> props = new HashMap<>();
        props.put("prop3", 10000434132L);
        props.put("prop4", new long[]{3L, 4L, 5L});
        assertTrue(ArrayUtils.arrayFriendlyMapEquals(props, relationship.getProperties().getProperties()));
    }

    @Test
    public void shouldBeConstructedFromRelationshipAndNode2() {
        setUp();

        ImmutableDirectedRelationship relationship = new ImmutableDirectedRelationshipImpl(database.getNodeById(1).getSingleRelationship(withName("test2"), INCOMING), database.getNodeById(1));

        assertEquals("test2", relationship.getType().name());
        assertEquals(INCOMING, relationship.getDirection());
        assertEquals(new ImmutablePropertiesImpl(Collections.<String, Object>emptyMap()).getProperties(), relationship.getProperties().getProperties());
    }

    @Test
    public void shouldBeConstructedFromRelationshipNodeAndProperties() {
        setUp();

        ImmutablePropertiesImpl p = new ImmutablePropertiesImpl(Collections.<String, Object>singletonMap("key", "value"));
        ImmutableDirectedRelationship relationship = new ImmutableDirectedRelationshipImpl(database.getNodeById(1).getSingleRelationship(withName("test"), OUTGOING), database.getNodeById(1), p);

        assertEquals("test", relationship.getType().name());
        assertEquals(OUTGOING, relationship.getDirection());
        assertEquals(1, relationship.getProperties().size());
        assertEquals("value", relationship.getProperties().get("key"));
    }

    @Test
    public void shouldBeConstructedFromRelationshipNodeAndPropertiesAsMap() {
        setUp();

        ImmutableDirectedRelationship relationship = new ImmutableDirectedRelationshipImpl(database.getNodeById(1).getSingleRelationship(withName("test"), OUTGOING), database.getNodeById(1), Collections.<String, Object>singletonMap("key", "value"));

        assertEquals("test", relationship.getType().name());
        assertEquals(OUTGOING, relationship.getDirection());
        assertEquals(1, relationship.getProperties().size());
        assertEquals("value", relationship.getProperties().get("key"));
    }

    @Test
    public void shouldBeConstructedFromRelationshipTypeDirectionAndPropertiesAsMap() {
        setUp();

        ImmutableDirectedRelationship relationship = new ImmutableDirectedRelationshipImpl(withName("test"), OUTGOING, Collections.<String, Object>singletonMap("key", "value"));

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
        Map<String, Object> props = new HashMap<>();
        props.put("prop3", 10000434132L);
        props.put("prop4", new long[]{3, 4, 5});

        ImmutableDirectedRelationship relationship = new ImmutableDirectedRelationshipImpl(withName("test"), OUTGOING, new ImmutablePropertiesImpl(props));

        assertEquals("test", relationship.getType().name());
        assertEquals(OUTGOING, relationship.getDirection());
        assertEquals(props, relationship.getProperties().getProperties());
    }

    @Test
    public void shouldBeConstructedFromAnotherRelationship() {
        setUp();

        ImmutableDirectedRelationship<Object, ?> relationship = new ImmutableDirectedRelationshipImpl(new ImmutableDirectedRelationshipImpl(database.getNodeById(1).getSingleRelationship(withName("test"), OUTGOING), database.getNodeById(1)));

        assertEquals("test", relationship.getType().name());
        assertEquals(OUTGOING, relationship.getDirection());

        Map<String, Object> props = new HashMap<>();
        props.put("prop3", 10000434132L);
        props.put("prop4", new long[]{3, 4, 5});
        assertTrue(ArrayUtils.arrayFriendlyMapEquals(props, relationship.getProperties().getProperties()));
    }

    @Test
    public void sameRelationshipsShouldBeEqual() {
        assertTrue(new ImmutableDirectedRelationshipImpl(withName("test"), INCOMING).equals(new ImmutableDirectedRelationshipImpl(withName("test"), INCOMING)));
        assertTrue(new ImmutableDirectedRelationshipImpl(withName("test"), INCOMING, Collections.<String, Object>singletonMap("key", "value")).equals(new ImmutableDirectedRelationshipImpl(withName("test"), INCOMING, Collections.<String, Object>singletonMap("key", "value"))));
    }

    @Test
    public void differentRelationshipsShouldNotBeEqual() {
        assertFalse(new ImmutableDirectedRelationshipImpl(withName("test"), INCOMING).equals(new ImmutableDirectedRelationshipImpl(withName("test"), OUTGOING)));
        assertFalse(new ImmutableDirectedRelationshipImpl(withName("test"), INCOMING, Collections.<String, Object>singletonMap("key", "value2")).equals(new ImmutableDirectedRelationshipImpl(withName("test"), INCOMING, Collections.<String, Object>singletonMap("key", "value"))));
    }

    @Test
    public void testMatching() {
        setUp();

        Node node = database.getNodeById(1);
        Relationship r = node.getSingleRelationship(withName("test"), OUTGOING);

        Map<String, Object> props = new TreeMap<>();
        props.put("prop3", 10000434132L);
        props.put("prop4", new long[]{3, 4, 5});

        assertTrue(new ImmutableDirectedRelationshipImpl(withName("test"), OUTGOING, props).matches(r, node));
        assertTrue(new ImmutableDirectedRelationshipImpl(withName("test"), BOTH, props).matches(r, node));
        assertFalse(new ImmutableDirectedRelationshipImpl(withName("test"), INCOMING, props).matches(r, node));
        assertFalse(new ImmutableDirectedRelationshipImpl(withName("test2"), OUTGOING, props).matches(r, node));
        assertFalse(new ImmutableDirectedRelationshipImpl(withName("test2"), OUTGOING, Collections.<String, Object>emptyMap()).matches(r, node));

        assertTrue(new ImmutableDirectedRelationshipImpl(withName("test"), OUTGOING, props).matches(new ImmutableDirectedRelationshipImpl(withName("test"), OUTGOING, props)));
        assertTrue(new ImmutableDirectedRelationshipImpl(withName("test"), BOTH, props).matches(new ImmutableDirectedRelationshipImpl(withName("test"), OUTGOING, props)));
        assertTrue(new ImmutableDirectedRelationshipImpl(withName("test"), OUTGOING, props).matches(new ImmutableDirectedRelationshipImpl(withName("test"), BOTH, props)));
        assertFalse(new ImmutableDirectedRelationshipImpl(withName("test"), OUTGOING, props).matches(new ImmutableDirectedRelationshipImpl(withName("test"), INCOMING, props)));
        assertFalse(new ImmutableDirectedRelationshipImpl(withName("test2"), OUTGOING, props).matches(new ImmutableDirectedRelationshipImpl(withName("test"), OUTGOING, props)));
        assertFalse(new ImmutableDirectedRelationshipImpl(withName("test"), OUTGOING, props).matches(new ImmutableDirectedRelationshipImpl(withName("test"), OUTGOING, Collections.<String, Object>singletonMap("prop3", 10000434132L))));

        assertTrue(new ImmutableDirectedRelationshipImpl(withName("test"), OUTGOING, props).matches(new TypeAndDirection(withName("test"), OUTGOING)));
        assertTrue(new ImmutableDirectedRelationshipImpl(withName("test"), BOTH, props).matches(new TypeAndDirection(withName("test"), OUTGOING)));
        assertTrue(new ImmutableDirectedRelationshipImpl(withName("test"), OUTGOING, props).matches(new TypeAndDirection(withName("test"), BOTH)));
        assertFalse(new ImmutableDirectedRelationshipImpl(withName("test"), OUTGOING, props).matches(new TypeAndDirection(withName("test"), INCOMING)));
        assertFalse(new ImmutableDirectedRelationshipImpl(withName("test2"), OUTGOING, props).matches(new TypeAndDirection(withName("test"), OUTGOING)));

        try {
            new ImmutableDirectedRelationshipImpl(withName("test"), OUTGOING, props).matches(r);
            fail();
        } catch (UnsupportedOperationException e) {
            //ok
        }

        try {
            new ImmutableDirectedRelationshipImpl(withName("test"), OUTGOING, props).matches((PropertyContainer) r);
            fail();
        } catch (UnsupportedOperationException e) {
            //ok
        }
    }

    @Test
    public void testDirectionResolution() {
        setUp();

        ImmutableDirectedRelationship relationship = new MutableDirectedRelationshipImpl(database.getNodeById(2).getSingleRelationship(withName("cycle"), OUTGOING), database.getNodeById(2));

        assertEquals("cycle", relationship.getType().name());
        assertEquals(BOTH, relationship.getDirection());
    }
}
