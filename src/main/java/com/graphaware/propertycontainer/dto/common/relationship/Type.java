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

package com.graphaware.propertycontainer.dto.common.relationship;

import org.neo4j.graphdb.Relationship;

/**
 * The simplest possible implementation of {@link HasType}. Could be used as {@link org.neo4j.graphdb.Relationship}
 * representation when not interested in properties (and directions).
 */
public class Type extends BaseHasType implements HasType {

    /**
     * Construct a relationship representation.
     *
     * @param relationship Neo4j relationship to represent.
     */
    public Type(Relationship relationship) {
        super(relationship);
    }

    /**
     * Construct a relationship representation from another one.
     *
     * @param relationship relationships representation.
     */
    public Type(HasTypeAndDirection relationship) {
        super(relationship);
    }
}
