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

import com.graphaware.common.policy.inclusion.NodePropertyInclusionPolicy;
import com.graphaware.common.policy.inclusion.PropertyInclusionPolicy;
import com.graphaware.common.serialize.Serializer;
import com.graphaware.common.serialize.SingletonSerializer;
import com.graphaware.runtime.config.RuntimeConfiguration;
import org.neo4j.graphdb.Node;

/**
 * {@link Node} {@link PropertyInclusionPolicy} that includes arbitrary business / application level
 * properties (up to subclasses to decide which ones), but excludes any
 * {@link com.graphaware.runtime.GraphAwareRuntime}/{@link com.graphaware.runtime.module.TxDrivenModule} internal properties.
 */
public final class IncludeAllBusinessNodeProperties implements NodePropertyInclusionPolicy {

    static {
        Serializer.register(IncludeAllBusinessNodeProperties.class, new SingletonSerializer());
    }

    private static final NodePropertyInclusionPolicy INSTANCE = new IncludeAllBusinessNodeProperties();

    public static NodePropertyInclusionPolicy getInstance() {
        return INSTANCE;
    }

    private IncludeAllBusinessNodeProperties() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean include(String key, Node node) {
        return !key.startsWith(RuntimeConfiguration.GA_PREFIX);
    }
}
