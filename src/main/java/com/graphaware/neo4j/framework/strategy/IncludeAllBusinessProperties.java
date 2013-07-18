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

package com.graphaware.neo4j.framework.strategy;

import com.graphaware.neo4j.framework.GraphAwareFramework;
import com.graphaware.neo4j.tx.event.strategy.PropertyInclusionStrategy;
import org.neo4j.graphdb.PropertyContainer;

import java.lang.Override;
import java.lang.String;

/**
 * Abstract base-class for all {@link com.graphaware.neo4j.tx.event.strategy.PropertyInclusionStrategy}
 * implementations that include arbitrary business / application level
 * properties (up to subclasses to decide which ones), but exclude any
 * {@link GraphAwareFramework}/{@link com.graphaware.neo4j.framework.GraphAwareModule} internal properties.
 */
public abstract class IncludeAllBusinessProperties<T extends PropertyContainer> implements PropertyInclusionStrategy<T> {

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean include(String key, T propertyContainer) {
        if (key.startsWith(GraphAwareFramework.GA_PREFIX)) {
            return false;
        }

        return doInclude(key, propertyContainer);
    }

    /**
     * Should a property with the given key of the given property container be included for the purposes of transaction
     * data analysis.
     *
     * @param key               of the property.
     * @param propertyContainer containing the property.
     * @return true iff the property should be included.
     */
    protected abstract boolean doInclude(String key, T propertyContainer);

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return this.getClass().getCanonicalName().hashCode();
    }
}
