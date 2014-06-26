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

package com.graphaware.common.strategy;

import com.graphaware.common.serialize.Serializer;
import com.graphaware.common.serialize.SingletonSerializer;
import org.neo4j.graphdb.Relationship;

/**
 * Strategy that includes all relationship properties. Singleton.
 */
public class IncludeAllRelationshipProperties extends IncludeAllProperties<Relationship> implements RelationshipPropertyInclusionStrategy {

    private static final RelationshipPropertyInclusionStrategy INSTANCE = new IncludeAllRelationshipProperties();

    static {
        Serializer.register(IncludeAllRelationshipProperties.class, new SingletonSerializer());
    }

    public static RelationshipPropertyInclusionStrategy getInstance() {
        return INSTANCE;
    }

    protected IncludeAllRelationshipProperties() {
    }
}
