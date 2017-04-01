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

package com.graphaware.common.policy.inclusion.composite;

import com.graphaware.common.policy.inclusion.RelationshipInclusionPolicy;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

/**
 * {@link CompositePropertyContainerInclusionPolicy} for {@link Relationship}s.
 */
public final class CompositeRelationshipInclusionPolicy extends CompositePropertyContainerInclusionPolicy<Relationship, RelationshipInclusionPolicy> implements RelationshipInclusionPolicy {

    public static CompositeRelationshipInclusionPolicy of(RelationshipInclusionPolicy... policies) {
        return new CompositeRelationshipInclusionPolicy(policies);
    }

    private CompositeRelationshipInclusionPolicy(RelationshipInclusionPolicy[] policies) {
        super(policies);
    }

    @Override
    public boolean include(Relationship relationship, Node pointOfView) {
        for (RelationshipInclusionPolicy policy : policies) {
            if (!policy.include(relationship, pointOfView)) {
                return false;
            }
        }

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Iterable<Relationship> doGetAll(GraphDatabaseService database) {
        return database.getAllRelationships();
    }
}
