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

import com.graphaware.common.strategy.IncludeAllRelationships;
import com.graphaware.common.strategy.RelationshipInclusionStrategy;
import com.graphaware.runtime.config.RuntimeConfiguration;
import org.neo4j.graphdb.Relationship;

/**
 * Strategy that includes all business / application level relationships, but exclude any
 * {@link com.graphaware.runtime.GraphAwareRuntime} internal relationships. Singleton.
 */
public final class IncludeAllBusinessRelationships extends IncludeAllRelationships {

    private static final IncludeAllBusinessRelationships INSTANCE = new IncludeAllBusinessRelationships();

    public static IncludeAllBusinessRelationships getInstance() {
        return INSTANCE;
    }

    protected IncludeAllBusinessRelationships() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean include(Relationship relationship) {
        if (relationship.getType().name().startsWith(RuntimeConfiguration.GA_PREFIX)) {
            return false;
        }

        return super.include(relationship);
    }
}
