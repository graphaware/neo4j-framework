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

package com.graphaware.common.policy.inclusion.spel;

import com.graphaware.common.expression.AttachedRelationshipExpressions;
import com.graphaware.common.policy.inclusion.RelationshipInclusionPolicy;
import com.graphaware.common.representation.AttachedRelationship;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.helpers.collection.FilteringIterable;

/**
 * {@link RelationshipInclusionPolicy} based on a SPEL expression. The expression can use methods defined in
 * {@link AttachedRelationshipExpressions}.
 * <p>
 * Note that there are certain methods (like {@link AttachedRelationshipExpressions#getOtherNode()}
 * or {@link AttachedRelationshipExpressions#isOutgoing()}) that rely on providing
 * a node whose point of view the call is being made. These methods only work when calling {@link #include(org.neo4j.graphdb.Relationship, org.neo4j.graphdb.Node)}.
 * {@link IllegalArgumentException} is thrown when an incompatible method is invoked.
 */
public class SpelRelationshipInclusionPolicy extends SpelInclusionPolicy implements RelationshipInclusionPolicy {

    public SpelRelationshipInclusionPolicy(String expression) {
        super(expression);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean include(Relationship relationship) {
        return (Boolean) exp.getValue(new AttachedRelationship(relationship));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean include(Relationship relationship, Node pointOfView) {
        return (Boolean) exp.getValue(new AttachedRelationship(relationship, pointOfView));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterable<Relationship> getAll(GraphDatabaseService database) {
        return new FilteringIterable<>(database.getAllRelationships(), this::include);
    }
}
