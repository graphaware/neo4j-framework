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
import com.graphaware.propertycontainer.dto.common.relationship.SerializableTypeAndDirectionImpl;
import org.junit.Test;

import static junit.framework.Assert.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.neo4j.graphdb.Direction.INCOMING;
import static org.neo4j.graphdb.Direction.OUTGOING;
import static org.neo4j.graphdb.DynamicRelationshipType.withName;

/**
 * Unit test for {@link SerializableTypeAndDirectionImpl}.
 */
public class SerializableTypeAndDirectionImplTest extends AbstractTest {

    @Override
    protected void additionalSetup() {
        database.getNodeById(2).createRelationshipTo(database.getNodeById(1), withName("test2"));
    }

    @Test
    public void shouldBeConstructedFromRelationshipAndNode() {
        setUp();

        HasTypeAndDirection relationship = new SerializableTypeAndDirectionImpl(database.getNodeById(1).getSingleRelationship(withName("test"), OUTGOING), database.getNodeById(1));

        assertEquals("test", relationship.getType().name());
        assertEquals(OUTGOING, relationship.getDirection());
    }

    @Test
    public void shouldBeConstructedFromRelationshipAndNode2() {
        setUp();

        HasTypeAndDirection relationship = new SerializableTypeAndDirectionImpl(database.getNodeById(1).getSingleRelationship(withName("test2"), INCOMING), database.getNodeById(1));

        assertEquals("test2", relationship.getType().name());
        assertEquals(INCOMING, relationship.getDirection());
    }

    @Test
    public void shouldBeConstructedFromRelationshipTypeAndDirection() {
        HasTypeAndDirection relationship = new SerializableTypeAndDirectionImpl(withName("test"), OUTGOING);

        assertEquals("test", relationship.getType().name());
        assertEquals(OUTGOING, relationship.getDirection());
    }

    @Test
    public void shouldBeConstructedFromAnotherRelationship() {
        setUp();

        HasTypeAndDirection relationship = new SerializableTypeAndDirectionImpl(new SerializableTypeAndDirectionImpl(database.getNodeById(1).getSingleRelationship(withName("test"), OUTGOING), database.getNodeById(1)));

        assertEquals("test", relationship.getType().name());
        assertEquals(OUTGOING, relationship.getDirection());
    }

    @Test
    public void shouldBeCorrectlyConstructedFromString() {
        HasTypeAndDirection relationship = new SerializableTypeAndDirectionImpl("PREFIX" + "test#OUTGOING", "PREFIX", "#");

        assertEquals("test", relationship.getType().name());
        assertEquals(OUTGOING, relationship.getDirection());
    }

    @Test
    public void shouldBeCorrectlyConstructedFromString2() {
        HasTypeAndDirection relationship = new SerializableTypeAndDirectionImpl("test#OUTGOING#", null, "#");

        assertEquals("test", relationship.getType().name());
        assertEquals(OUTGOING, relationship.getDirection());
    }

    @Test
    public void shouldBeCorrectlyConstructedFromString3() {
        HasTypeAndDirection relationship = new SerializableTypeAndDirectionImpl("test#OUTGOING#whatever", "#");

        assertEquals("test", relationship.getType().name());
        assertEquals(OUTGOING, relationship.getDirection());
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWhenTypeIsEmpty() {
        new SerializableTypeAndDirectionImpl("#OUTGOING", "#");
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWhenDirectionIsInvalid() {
        new SerializableTypeAndDirectionImpl("test#INVALID", "#");
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWhenDirectionIsMissing() {
        new SerializableTypeAndDirectionImpl("test#", "#");
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWhenDirectionIsMissing2() {
        new SerializableTypeAndDirectionImpl("test", "#");
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWhenNoInfoProvided() {
        new SerializableTypeAndDirectionImpl("", "#");
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWhenNoInfoProvided2() {
        new SerializableTypeAndDirectionImpl("PREFIX", "PREFIX", "#");
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWithIncorrectPrefix() {
        new SerializableTypeAndDirectionImpl("test#OUTGOING", "PREFIX", "#");
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldComplainAboutNullSeparator() {
        new SerializableTypeAndDirectionImpl("test#OUTGOING", "", null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldComplainAboutEmptySeparator() {
        new SerializableTypeAndDirectionImpl("test#OUTGOING", "", "");
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldComplainAboutEmptySeparator2() {
        new SerializableTypeAndDirectionImpl("test#OUTGOING", "", " ");
    }

    @Test
    public void shouldCorrectlyConvertToString() {
        assertEquals("_PREFIX_" + "test#INCOMING", new SerializableTypeAndDirectionImpl("test#INCOMING", "#").toString("_PREFIX_", "#"));
        assertEquals("test#INCOMING", new SerializableTypeAndDirectionImpl("test#INCOMING#", "#").toString("#"));
    }

    @Test
    public void sameRelationshipsShouldBeEqual() {
        assertTrue(new SerializableTypeAndDirectionImpl("_PRE_" + "test#INCOMING", "_PRE_", "#").equals(new SerializableTypeAndDirectionImpl("test#INCOMING", "#")));
    }

    @Test
    public void differentRelationshipsShouldNotBeEqual() {
        assertFalse(new SerializableTypeAndDirectionImpl("test#OUTGOING", "#").equals(new SerializableTypeAndDirectionImpl("test#INCOMING", "#")));
        assertFalse(new SerializableTypeAndDirectionImpl("test2#INCOMING", "#").equals(new SerializableTypeAndDirectionImpl("test#INCOMING", "#")));
    }
}
