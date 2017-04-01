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

import com.graphaware.common.policy.inclusion.RelationshipPropertyInclusionPolicy;
import com.graphaware.common.representation.AttachedRelationship;
import com.graphaware.common.representation.AttachedRelationshipProperty;
import com.graphaware.common.representation.RelationshipProperty;
import org.neo4j.graphdb.Relationship;

/**
 * {@link RelationshipPropertyInclusionPolicy} based on a SPEL expression. The expression can use methods defined in
 * {@link RelationshipProperty}.
 */
public class SpelRelationshipPropertyInclusionPolicy extends SpelInclusionPolicy implements RelationshipPropertyInclusionPolicy {

    public SpelRelationshipPropertyInclusionPolicy(String expression) {
        super(expression);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean include(String key, Relationship relationship) {
        return (Boolean) exp.getValue(new AttachedRelationshipProperty(key, new AttachedRelationship(relationship)));
    }
}
