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
import org.junit.Test;
import org.neo4j.graphdb.Direction;

import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.neo4j.graphdb.DynamicRelationshipType.withName;

/**
 * Unit test for {@link com.graphaware.propertycontainer.dto.string.property.BaseCopyMakingSerializableProperties}.
 */
public class CopyMakingSerializablePropertiesImplTest extends AbstractTest {

    @Test
    public void shouldBeConstructedFromPropertyContainer() {
        setUp();

        ImmutableProperties<String> properties = new CopyMakingSerializablePropertiesImpl(database.getNodeById(1));
        assertEquals(2, properties.size());

        properties = new CopyMakingSerializablePropertiesImpl(database.getNodeById(1).getSingleRelationship(withName("test"), Direction.OUTGOING));
        assertEquals(2, properties.size());
    }

    @Test
    public void shouldCorrectlyExcludeProperty() {
        ImmutableProperties<String> properties = new CopyMakingSerializablePropertiesImpl("key1#value1#key2#value2", "#").without("key1");

        assertEquals("value2", properties.get("key2"));
        assertEquals(1, properties.size());
    }

    @Test
    public void whenMissingPropertyToExcludeReturnIdentity() {
        CopyMakingSerializablePropertiesImpl properties = new CopyMakingSerializablePropertiesImpl("key1#value1#key2#value2", "#").without("keyX");

        assertEquals("value1", properties.get("key1"));
        assertEquals("value2", properties.get("key2"));
        assertEquals(2, properties.size());
        assertTrue(properties.equals(properties.without("keyX")));
    }

    @Test
    public void shouldCorrectlyAddProperty() {
        ImmutableProperties<String> properties = new CopyMakingSerializablePropertiesImpl("key1#value1#key2#value2", "#").with("key3", "value3");

        assertEquals("value1", properties.get("key1"));
        assertEquals("value2", properties.get("key2"));
        assertEquals("value3", properties.get("key3"));
        assertEquals(3, properties.size());
    }
}
