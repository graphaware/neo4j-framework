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

package com.graphaware.propertycontainer.dto.string.property;

import com.graphaware.propertycontainer.dto.AbstractTest;
import com.graphaware.propertycontainer.dto.common.property.ImmutableProperties;
import com.graphaware.propertycontainer.dto.common.property.MutableProperties;
import org.junit.Test;
import org.neo4j.graphdb.Relationship;

import java.util.Collections;

import static junit.framework.Assert.*;
import static org.neo4j.graphdb.Direction.OUTGOING;
import static org.neo4j.graphdb.DynamicRelationshipType.withName;

/**
 * Unit test for {@link com.graphaware.propertycontainer.dto.plain.property.ImmutablePropertiesImpl}.
 */
public class ImmutablePropertiesImplTest extends AbstractTest {

    @Test
    public void shouldBeConstructedFromPropertyContainer() {
        setUp();

        ImmutableProperties<String> properties = new ImmutablePropertiesImpl(database.getNodeById(1));
        assertEquals(2, properties.size());
        assertEquals("value1", properties.get("prop1"));
        assertEquals((2), Integer.valueOf(properties.get("prop2")).intValue());

        assertTrue(properties.containsKey("prop1"));
        assertFalse(properties.containsKey("prop3"));

        assertEquals(2, properties.entrySet().size());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void shouldBeImmutable() {
        ImmutablePropertiesImpl properties = new ImmutablePropertiesImpl(Collections.singletonMap("key", "value"));
        properties.setProperty("key2", "value2");
    }

    @Test(expected = UnsupportedOperationException.class)
    public void shouldBeImmutable2() {
        ImmutablePropertiesImpl properties = new ImmutablePropertiesImpl(Collections.singletonMap("key", "value"));
        properties.removeProperty("key");
    }

    @Test
    public void verifyMatching() {
        setUp();

        Relationship r = database.getNodeById(1).getSingleRelationship(withName("test"), OUTGOING);
        ImmutableProperties<String> properties = new ImmutablePropertiesImpl(r);

        MutableProperties<String> equalProperties = new MutablePropertiesImpl();
        equalProperties.setProperty("prop3", "10000434132");
        equalProperties.setProperty("prop4", "[3, 4, 5]");

        MutableProperties<String> notSameProperties = new MutablePropertiesImpl();
        notSameProperties.setProperty("prop3", "10000434133");
        notSameProperties.setProperty("prop4", "[3, 4, 6]");

        assertTrue(properties.matches(r));
        assertTrue(properties.matches(properties));
        assertTrue(properties.matches(equalProperties));
        assertTrue(equalProperties.matches(properties));
        assertTrue(equalProperties.matches(r));

        assertFalse(properties.matches(notSameProperties));
        assertFalse(notSameProperties.matches(properties));
        assertFalse(notSameProperties.matches(r));
    }
}
