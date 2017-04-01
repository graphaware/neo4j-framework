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

import com.graphaware.common.policy.inclusion.RelationshipInclusionPolicy;
import com.graphaware.common.serialize.Serializer;
import com.graphaware.common.serialize.SingletonSerializer;
import com.graphaware.runtime.config.RuntimeConfiguration;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Relationship;

/**
 * Policy that includes all business / application level relationships, but exclude any
 * {@link com.graphaware.runtime.GraphAwareRuntime} internal relationships. Singleton.
 */
public final class IncludeAllBusinessRelationships extends RelationshipInclusionPolicy.Adapter {

    static {
        Serializer.register(IncludeAllBusinessRelationships.class, new SingletonSerializer());
    }

    private static final IncludeAllBusinessRelationships INSTANCE = new IncludeAllBusinessRelationships();

    public static IncludeAllBusinessRelationships getInstance() {
        return INSTANCE;
    }

    private IncludeAllBusinessRelationships() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean include(Relationship relationship) {
        return !relationship.getType().name().startsWith(RuntimeConfiguration.GA_PREFIX);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Iterable<Relationship> doGetAll(GraphDatabaseService database) {
        return database.getAllRelationships();
    }
}
