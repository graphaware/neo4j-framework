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

package com.graphaware.common.description.relationship;

import com.graphaware.common.description.property.LazyPropertiesDescription;
import com.graphaware.common.util.DirectionUtils;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

/**
 * The most specific {@link RelationshipDescription} of a {@link Relationship} that lazily consults the underlying
 * {@link Relationship}. It returns {@link com.graphaware.common.description.property.LazyPropertiesDescription} when
 * asked for properties.
 */
public class LazyRelationshipDescription extends BaseRelationshipDescription<LazyPropertiesDescription> implements RelationshipDescription {

    /**
     * Construct a new relationship description as the most specific description of the given relationship.
     *
     * @param relationship to construct the most specific relationship description from.
     * @param pointOfView  node whose point of view this relationship description is being constructed from.
     */
    public LazyRelationshipDescription(Relationship relationship, Node pointOfView) {
        super(relationship.getType().name(), DirectionUtils.resolveDirection(relationship, pointOfView), new LazyPropertiesDescription(relationship));
    }
}
