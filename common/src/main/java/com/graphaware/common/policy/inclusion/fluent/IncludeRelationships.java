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

package com.graphaware.common.policy.inclusion.fluent;

import com.graphaware.common.description.predicate.Predicate;
import com.graphaware.common.description.property.DetachedPropertiesDescription;
import com.graphaware.common.description.property.WildcardPropertiesDescription;
import com.graphaware.common.policy.inclusion.RelationshipInclusionPolicy;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Relationship;

import java.util.Collections;

/**
 * An implementation of {@link RelationshipInclusionPolicy} that is entirely configurable using its fluent interface.
 */
public class IncludeRelationships extends BaseIncludeRelationships<IncludeRelationships> {

    /**
     * Get a relationship inclusion policy that includes all relationships as the base-line for further configuration.
     * <p/>
     * Note that if you want to simply include all relationships, it is more efficient to use {@link com.graphaware.common.policy.inclusion.all.IncludeAllRelationships}.
     *
     * @return a policy including all relationships.
     */
    public static IncludeRelationships all() {
        return new IncludeRelationships(Direction.BOTH, new String[0], new WildcardPropertiesDescription(Collections.<String, Predicate>emptyMap()));
    }

    /**
     * Create a new policy.
     *
     * @param direction             that matching relationships must have, {@link Direction#BOTH} for both.
     * @param relationshipTypes     one of which the matching relationships must have, empty for all.
     * @param propertiesDescription of the matching relationships.
     */
    protected IncludeRelationships(Direction direction, String[] relationshipTypes, DetachedPropertiesDescription propertiesDescription) {
        super(direction, relationshipTypes, propertiesDescription);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected IncludeRelationships newInstance(Direction direction, String... relationshipTypes) {
        return new IncludeRelationships(direction, relationshipTypes, getPropertiesDescription());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected IncludeRelationships newInstance(DetachedPropertiesDescription propertiesDescription) {
        return new IncludeRelationships(getDirection(), getRelationshipTypes(), propertiesDescription);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Iterable<Relationship> doGetAll(GraphDatabaseService database) {
        return database.getAllRelationships();
    }
}
