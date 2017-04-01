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

import com.graphaware.common.description.predicate.Predicate;
import com.graphaware.common.description.property.DetachedPropertiesDescription;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.RelationshipType;

/**
 * Default production implementation of {@link DetachedRelationshipDescription}.
 */
public class DetachedRelationshipDescriptionImpl extends BaseRelationshipDescription<DetachedPropertiesDescription> implements DetachedRelationshipDescription {

    /**
     * Construct a new relationship description.
     *
     * @param relationshipType      relationship type.
     * @param direction             direction.
     * @param propertiesDescription properties description.
     */
    public DetachedRelationshipDescriptionImpl(String relationshipType, Direction direction, DetachedPropertiesDescription propertiesDescription) {
        super(relationshipType, direction, propertiesDescription);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DetachedRelationshipDescription with(String propertyKey, Predicate predicate) {
        return new DetachedRelationshipDescriptionImpl(getType(), getDirection(), getPropertiesDescription().with(propertyKey, predicate));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return getType() + "#" + getDirection() + "#" + getPropertiesDescription().toString();
    }
}
