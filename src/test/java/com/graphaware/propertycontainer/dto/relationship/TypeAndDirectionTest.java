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

package com.graphaware.propertycontainer.dto.relationship;

import com.graphaware.propertycontainer.dto.AbstractTest;
import com.graphaware.propertycontainer.dto.common.relationship.HasTypeAndDirection;
import com.graphaware.propertycontainer.dto.common.relationship.TypeAndDirection;
import com.graphaware.propertycontainer.dto.plain.relationship.ImmutableDirectedRelationshipImpl;
import org.junit.Test;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.fail;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.neo4j.graphdb.Direction.*;
import static org.neo4j.graphdb.DynamicRelationshipType.withName;

/**
 * Unit test for {@link TypeAndDirection}.
 */
public class TypeAndDirectionTest extends AbstractTest {

    @Override
    protected void additionalSetup() {
        database.getNodeById(2).createRelationshipTo(database.getNodeById(1), withName("test2"));
    }

    @Test
    public void shouldBeConstructedFromRelationshipAndNode() {
        setUp();

        HasTypeAndDirection relationship = new TypeAndDirection(database.getNodeById(1).getSingleRelationship(withName("test"), OUTGOING), database.getNodeById(1));

        assertEquals("test", relationship.getType().name());
        assertEquals(OUTGOING, relationship.getDirection());
    }

    @Test
    public void shouldBeConstructedFromRelationshipAndNode2() {
        setUp();

        HasTypeAndDirection relationship = new TypeAndDirection(database.getNodeById(1).getSingleRelationship(withName("test2"), INCOMING), database.getNodeById(1));

        assertEquals("test2", relationship.getType().name());
        assertEquals(INCOMING, relationship.getDirection());
    }

    @Test
    public void shouldBeConstructedFromRelationshipTypeAndDirection() {
        HasTypeAndDirection relationship = new TypeAndDirection(withName("test"), OUTGOING);

        assertEquals("test", relationship.getType().name());
        assertEquals(OUTGOING, relationship.getDirection());
    }

    @Test
    public void shouldBeConstructedFromAnotherRelationship() {
        setUp();

        HasTypeAndDirection relationship = new TypeAndDirection(new ImmutableDirectedRelationshipImpl(database.getNodeById(1).getSingleRelationship(withName("test"), OUTGOING), database.getNodeById(1)));

        assertEquals("test", relationship.getType().name());
        assertEquals(OUTGOING, relationship.getDirection());
    }

    @Test
    public void sameRelationshipsShouldBeEqual() {
        assertTrue(new TypeAndDirection(withName("test"), INCOMING).equals(new TypeAndDirection(withName("test"), INCOMING)));
    }

    @Test
    public void differentRelationshipsShouldNotBeEqual() {
        assertFalse(new TypeAndDirection(withName("test"), INCOMING).equals(new TypeAndDirection(withName("test"), OUTGOING)));
        assertFalse(new TypeAndDirection(withName("test2"), INCOMING).equals(new TypeAndDirection(withName("test"), INCOMING)));
    }

    @Test
    public void testMatching() {
        setUp();

        Node node = database.getNodeById(1);
        Relationship r = node.getSingleRelationship(withName("test"), OUTGOING);

        assertTrue(new TypeAndDirection(withName("test"), OUTGOING).matches(r, node));
        assertTrue(new TypeAndDirection(withName("test"), BOTH).matches(r, node));
        assertFalse(new TypeAndDirection(withName("test"), INCOMING).matches(r, node));
        assertFalse(new TypeAndDirection(withName("test2"), OUTGOING).matches(r, node));

        try {
            new TypeAndDirection(withName("test"), OUTGOING).matches(r);
            fail();
        } catch (UnsupportedOperationException e) {
            //ok
        }

        assertTrue(new TypeAndDirection(withName("test"), OUTGOING).matches(new TypeAndDirection(withName("test"), OUTGOING)));
        assertTrue(new TypeAndDirection(withName("test"), BOTH).matches(new TypeAndDirection(withName("test"), OUTGOING)));
        assertTrue(new TypeAndDirection(withName("test"), OUTGOING).matches(new TypeAndDirection(withName("test"), BOTH)));
        assertFalse(new TypeAndDirection(withName("test"), OUTGOING).matches(new TypeAndDirection(withName("test"), INCOMING)));
        assertFalse(new TypeAndDirection(withName("test3"), OUTGOING).matches(new TypeAndDirection(withName("test"), OUTGOING)));
        assertFalse(new TypeAndDirection(withName("test"), OUTGOING).matches(new TypeAndDirection(withName("test3"), OUTGOING)));
    }

    @Test
    public void testDirectionResolution() {
        setUp();

        HasTypeAndDirection relationship = new TypeAndDirection(database.getNodeById(2).getSingleRelationship(withName("cycle"), OUTGOING), database.getNodeById(2));

        assertEquals("cycle", relationship.getType().name());
        assertEquals(BOTH, relationship.getDirection());
    }
}
