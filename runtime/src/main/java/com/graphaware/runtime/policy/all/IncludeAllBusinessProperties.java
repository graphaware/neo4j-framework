/*
 * Copyright (c) 2013-2017 GraphAware
 *
 * This file is part of the GraphAware Framework.
 *
 * GraphAware Framework is free software: you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received a copy of
 * the GNU General Public License along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 */

package com.graphaware.runtime.policy.all;

import com.graphaware.common.policy.inclusion.PropertyInclusionPolicy;
import com.graphaware.runtime.config.RuntimeConfiguration;
import org.neo4j.graphdb.PropertyContainer;

/**
 * Base-class for all {@link PropertyInclusionPolicy}
 * implementations that include arbitrary business / application level
 * properties (up to subclasses to decide which ones), but exclude any
 * {@link com.graphaware.runtime.GraphAwareRuntime}/{@link com.graphaware.runtime.module.TxDrivenModule} internal properties.
 */
public abstract class IncludeAllBusinessProperties<T extends PropertyContainer> implements PropertyInclusionPolicy<T> {

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean include(String key, T propertyContainer) {
        if (key.startsWith(RuntimeConfiguration.GA_PREFIX)) {
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
}
