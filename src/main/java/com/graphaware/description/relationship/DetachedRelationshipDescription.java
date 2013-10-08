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

package com.graphaware.description.relationship;

import com.graphaware.description.predicate.Predicate;
import com.graphaware.description.property.FluentPropertiesDescription;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.RelationshipType;

/**
 * A {@link RelationshipDescription} that is immutable and maintains all its data;
 * thus, it can be serialized and stored. It also allows for generating new instances with different predicates, by
 * implementing the {@link com.graphaware.description.property.FluentPropertiesDescription} interface.
 */
public abstract class DetachedRelationshipDescription extends BaseRelationshipDescription<FluentPropertiesDescription> implements FluentRelationshipDescription {

    protected DetachedRelationshipDescription(RelationshipType relationshipType, Direction direction, FluentPropertiesDescription propertiesDescription) {
        super(relationshipType, direction, propertiesDescription);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FluentRelationshipDescription with(String propertyKey, Predicate predicate) {
        return newInstance(getType(), getDirection(), getPropertiesDescription().with(propertyKey, predicate));
    }

    /**
     * Create a new instance of this class with the given predicates.
     *
     * @param predicates to copy.
     * @return new instance.
     */
    protected abstract FluentRelationshipDescription newInstance(RelationshipType relationshipType, Direction direction, FluentPropertiesDescription propertiesDescription);


}
