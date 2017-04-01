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
import com.graphaware.common.policy.inclusion.RelationshipPropertyInclusionPolicy;
import com.graphaware.common.serialize.Serializer;
import com.graphaware.common.serialize.SingletonSerializer;
import com.graphaware.runtime.config.RuntimeConfiguration;
import org.neo4j.graphdb.Relationship;

/**
 * {@link org.neo4j.graphdb.Node} {@link PropertyInclusionPolicy} that includes arbitrary business / application level
 * properties (up to subclasses to decide which ones), but excludes any
 * {@link com.graphaware.runtime.GraphAwareRuntime}/{@link com.graphaware.runtime.module.TxDrivenModule} internal properties.
 */
public final class IncludeAllBusinessRelationshipProperties implements RelationshipPropertyInclusionPolicy {

    static {
        Serializer.register(IncludeAllBusinessRelationshipProperties.class, new SingletonSerializer());
    }

    private static final RelationshipPropertyInclusionPolicy INSTANCE = new IncludeAllBusinessRelationshipProperties();

    public static RelationshipPropertyInclusionPolicy getInstance() {
        return INSTANCE;
    }

    private IncludeAllBusinessRelationshipProperties() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean include(String key, Relationship relationship) {
        if (key.startsWith(RuntimeConfiguration.GA_PREFIX)) {
            return false;
        }

        return true;
    }
}
