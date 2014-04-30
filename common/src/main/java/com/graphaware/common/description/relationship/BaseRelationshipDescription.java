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

package com.graphaware.common.description.relationship;

import com.graphaware.common.description.BasePartiallyComparable;
import com.graphaware.common.description.property.PropertiesDescription;
import com.graphaware.common.util.DirectionUtils;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.RelationshipType;

/**
 * Base class for {@link RelationshipDescription} implementations.
 *
 * @param <P> type of properties description contained by this relationship description.
 */
public abstract class BaseRelationshipDescription<P extends PropertiesDescription> extends BasePartiallyComparable<RelationshipDescription> implements RelationshipDescription {

    private final RelationshipType relationshipType;
    private final Direction direction;
    private final P propertiesDescription;

    /**
     * Construct a new relationship description.
     *
     * @param relationshipType      relationship type.
     * @param direction             direction.
     * @param propertiesDescription properties description.
     */
    protected BaseRelationshipDescription(RelationshipType relationshipType, Direction direction, P propertiesDescription) {
        this.relationshipType = DynamicRelationshipType.withName(relationshipType.name());
        this.direction = direction;
        this.propertiesDescription = propertiesDescription;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isMoreGeneralThan(RelationshipDescription other) {
        return getType().name().equals(other.getType().name())
                && DirectionUtils.matches(getDirection(), other.getDirection())
                && getPropertiesDescription().isMoreGeneralThan(other.getPropertiesDescription());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isMutuallyExclusive(RelationshipDescription other) {
        return !getType().name().equals(other.getType().name())
                || !DirectionUtils.matches(getDirection(), other.getDirection())
                || getPropertiesDescription().isMutuallyExclusive(other.getPropertiesDescription());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected RelationshipDescription self() {
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RelationshipType getType() {
        return relationshipType;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Direction getDirection() {
        return direction;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public P getPropertiesDescription() {
        return propertiesDescription;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BaseRelationshipDescription that = (BaseRelationshipDescription) o;

        if (direction != that.direction) return false;
        if (!propertiesDescription.equals(that.propertiesDescription)) return false;
        if (!relationshipType.name().equals(that.relationshipType.name())) return false;

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int result = relationshipType.name().hashCode();
        result = 31 * result + direction.hashCode();
        result = 31 * result + propertiesDescription.hashCode();
        return result;
    }
}
