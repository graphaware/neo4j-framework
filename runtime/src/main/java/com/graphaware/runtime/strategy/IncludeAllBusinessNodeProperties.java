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

package com.graphaware.runtime.strategy;

import com.graphaware.common.strategy.IncludeAllNodeProperties;
import com.graphaware.common.strategy.NodePropertyInclusionStrategy;
import com.graphaware.common.strategy.PropertyInclusionStrategy;
import com.graphaware.runtime.config.RuntimeConfiguration;
import org.neo4j.graphdb.Node;

/**
 * {@link Node} {@link PropertyInclusionStrategy} that includes arbitrary business / application level
 * properties (up to subclasses to decide which ones), but excludes any
 * {@link com.graphaware.runtime.GraphAwareRuntime}/{@link com.graphaware.runtime.GraphAwareRuntimeModule} internal properties.
 */
public class IncludeAllBusinessNodeProperties extends IncludeAllNodeProperties {

    private static final NodePropertyInclusionStrategy INSTANCE = new IncludeAllBusinessNodeProperties();

    public static NodePropertyInclusionStrategy getInstance() {
        return INSTANCE;
    }

    protected IncludeAllBusinessNodeProperties() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean include(String key, Node node) {
        if (key.startsWith(RuntimeConfiguration.GA_PREFIX)) {
            return false;
        }

        return super.include(key, node);
    }
}
