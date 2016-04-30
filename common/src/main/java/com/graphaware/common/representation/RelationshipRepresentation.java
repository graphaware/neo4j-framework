/*
 * Copyright (c) 2013-2016 GraphAware
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

package com.graphaware.common.representation;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;

import java.util.Map;

import static org.neo4j.graphdb.RelationshipType.*;
import static org.springframework.util.Assert.hasLength;

/**
 * {@link PropertyContainerRepresentation} for a {@link Relationship}.
 */
public class RelationshipRepresentation extends PropertyContainerRepresentation<Relationship> {

    private long startNodeGraphId = NEW;
    private long endNodeGraphId = NEW;
    private String type;

    /**
     * Public no-arg constructor (for Jackson et al).
     */
    public RelationshipRepresentation() {
    }

    /**
     * Construct a representation from a Neo4j relationship.
     *
     * @param relationship to create the representation from. Must not be <code>null</code>.
     */
    public RelationshipRepresentation(Relationship relationship) {
        this(relationship, null);
    }

    /**
     * Construct a representation from a Neo4j relationship.
     *
     * @param relationship to create the representation from. Must not be <code>null</code>.
     * @param properties   keys of properties to be included in the representation.
     *                     Can be <code>null</code>, which represents all. Empty array represents none.
     */
    public RelationshipRepresentation(Relationship relationship, String[] properties) {
        super(relationship, properties);
        startNodeGraphId = relationship.getStartNode().getId();
        endNodeGraphId = relationship.getEndNode().getId();
        setType(relationship.getType().name());
    }

    /**
     * Construct a representation of a relationship from its internal Neo4j ID.
     *
     * @param graphId ID.
     */
    public RelationshipRepresentation(long graphId) {
        super(graphId);
    }

    /**
     * Construct a new relationship representation.
     *
     * @param startNodeGraphId start node ID.
     * @param endNodeGraphId   end node ID.
     * @param type             relationship type. Must not be <code>null</code> or empty.
     * @param properties       relationship properties. Can be <code>null</code>, which is equivalent to an empty map.
     */
    public RelationshipRepresentation(long startNodeGraphId, long endNodeGraphId, String type, Map<String, Object> properties) {
        super(properties);
        hasLength(type);
        this.startNodeGraphId = startNodeGraphId;
        this.endNodeGraphId = endNodeGraphId;
        this.type = type;
    }

    /**
     * Construct a new relationship representation.
     * <p>
     * Note that this constructor is only intended for testing.
     *
     * @param graphId          Neo4j relationship ID.
     * @param startNodeGraphId start node ID.
     * @param endNodeGraphId   end node ID.
     * @param type             relationship type. Must not be <code>null</code> or empty.
     * @param properties       relationship properties. Can be <code>null</code>, which is equivalent to an empty map.
     */
    public RelationshipRepresentation(long graphId, long startNodeGraphId, long endNodeGraphId, String type, Map<String, Object> properties) {
        super(graphId, properties);
        hasLength(type);
        this.startNodeGraphId = startNodeGraphId;
        this.endNodeGraphId = endNodeGraphId;
        this.type = type;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Relationship create(GraphDatabaseService database) {
        return database.getNodeById(startNodeGraphId).createRelationshipTo(database.getNodeById(endNodeGraphId), withName(type));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Relationship fetch(GraphDatabaseService database) {
        return database.getRelationshipById(getGraphId());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void checkCanCreate() {
        super.checkCanCreate();

        if (type == null || type.length() == 0) {
            throw new IllegalStateException("Relationship type must not be null or empty");
        }

        if (startNodeGraphId == NEW || endNodeGraphId == NEW) {
            throw new IllegalStateException("Start and End node IDs must be specified");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void checkCanFetch() {
        super.checkCanFetch();

        if (startNodeGraphId != NEW || endNodeGraphId != NEW) {
            throw new IllegalStateException("Must not specify start/end node for existing relationship!");
        }

        if (type != null) {
            throw new IllegalStateException("Must not specify type for existing relationship!");
        }
    }

    //Getters and setters

    public String getType() {
        return type;
    }

    public void setType(String type) {
        hasLength(type);
        this.type = type;
    }

    public long getStartNodeGraphId() {
        return startNodeGraphId;
    }

    public void setStartNodeGraphId(long startNodeGraphId) {
        this.startNodeGraphId = startNodeGraphId;
    }

    public long getEndNodeGraphId() {
        return endNodeGraphId;
    }

    public void setEndNodeGraphId(long endNodeGraphId) {
        this.endNodeGraphId = endNodeGraphId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        RelationshipRepresentation that = (RelationshipRepresentation) o;

        if (startNodeGraphId != that.startNodeGraphId) {
            return false;
        }
        if (endNodeGraphId != that.endNodeGraphId) {
            return false;
        }
        return type.equals(that.type);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (int) (startNodeGraphId ^ (startNodeGraphId >>> 32));
        result = 31 * result + (int) (endNodeGraphId ^ (endNodeGraphId >>> 32));
        result = 31 * result + type.hashCode();
        return result;
    }
}

