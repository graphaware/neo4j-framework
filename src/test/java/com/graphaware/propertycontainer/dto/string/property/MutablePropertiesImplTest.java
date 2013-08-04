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
import com.graphaware.propertycontainer.dto.common.property.MutableProperties;
import org.junit.Test;

import java.util.Collections;

import static junit.framework.Assert.*;

/**
 * Unit test for {@link MutablePropertiesImpl}.
 */
public class MutablePropertiesImplTest extends AbstractTest {

    @Test
    public void shouldBeConstructedFromPropertyContainer() {
        setUp();

        MutableProperties<String> properties = new MutablePropertiesImpl(database.getNodeById(1));
        assertEquals(2, properties.size());
        assertEquals("value1", properties.get("prop1"));
        assertEquals((2), Integer.valueOf(properties.get("prop2")).intValue());

        assertTrue(properties.containsKey("prop1"));
        assertFalse(properties.containsKey("prop3"));

        assertEquals(2, properties.entrySet().size());
    }

    @Test
    public void shouldSetProperty() {
        MutableProperties<String> properties = new MutablePropertiesImpl(Collections.singletonMap("key", "value"));
        properties.setProperty("key2", "value2");

        assertEquals(2, properties.size());
        assertEquals("value", properties.get("key"));
        assertEquals("value2", properties.get("key2"));
    }

    @Test
    public void shouldRemoveProperty() {
        MutableProperties properties = new MutablePropertiesImpl(Collections.singletonMap("key", "value"));

        assertTrue(properties.removeProperty("key"));
        assertEquals(0, properties.size());
        assertFalse(properties.removeProperty("key"));
    }
}
