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

package com.graphaware.common.policy.inclusion;


import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

/**
 * {@link PropertyContainerInclusionPolicy} for {@link Relationship}s.
 */
public interface RelationshipInclusionPolicy extends PropertyContainerInclusionPolicy<Relationship> {

    /**
     * Include the given relationships from the given node's point of view?
     *
     * @param relationship to make a decision on.
     * @param pointOfView  node looking at the relationship. Must be one of the relationship's nodes.
     * @return true to include, false to exclude.
     */
    boolean include(Relationship relationship, Node pointOfView);

    /**
     * Adapter for implementations that don't care about which node is looking at the relationship.
     */
    abstract class Adapter extends BaseRelationshipInclusionPolicy implements RelationshipInclusionPolicy {

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean include(Relationship relationship, Node pointOfView) {
            return include(relationship);
        }
    }
}
