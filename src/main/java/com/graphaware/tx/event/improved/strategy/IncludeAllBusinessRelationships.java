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

package com.graphaware.tx.event.improved.strategy;

import com.graphaware.framework.GraphAwareFramework;
import com.graphaware.framework.config.FrameworkConfiguration;
import org.neo4j.graphdb.Relationship;

/**
 * Base-class for all {@link com.graphaware.tx.event.improved.strategy.RelationshipInclusionStrategy} that include
 * arbitrary business / application level
 * relationships (up to subclasses to decide which ones), but exclude any {@link GraphAwareFramework}/{@link com.graphaware.framework.GraphAwareModule}
 * internal relationships.
 */
public abstract class IncludeAllBusinessRelationships implements RelationshipInclusionStrategy {

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean include(Relationship relationship) {
        if (relationship.getType().name().startsWith(FrameworkConfiguration.GA_PREFIX)) {
            return false;
        }

        return doInclude(relationship);
    }

    /**
     * Should this relationship be included?
     *
     * @param relationship to check.
     * @return true iff the relationship should be included.
     */
    protected abstract boolean doInclude(Relationship relationship);
}
