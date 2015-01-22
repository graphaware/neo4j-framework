/*
 * Copyright (c) 2015 GraphAware
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

package com.graphaware.common.policy.spel;

import org.neo4j.graphdb.PropertyContainer;

/**
 * {@link PropertyContainer} wrapper that defines delegating methods usable in SPEL expressions when constructing
 * {@link SpelInclusionPolicy}s.
 */
abstract class PropertyContainerExpressions<T extends PropertyContainer> {

    protected final T propertyContainer;

    PropertyContainerExpressions(T propertyContainer) {
        this.propertyContainer = propertyContainer;
    }

    public boolean hasProperty(String key) {
        return propertyContainer.hasProperty(key);
    }

    public Object getProperty(String key, Object defaultValue) {
        return propertyContainer.getProperty(key, defaultValue);
    }
}
