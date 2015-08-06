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

package com.graphaware.api;

import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Relationship;

import java.util.Map;

/**
 * JSON-serializable representation of a Neo4j relationship.
 */
public class JsonRelationship extends JsonPropertyContainer<Relationship> {

    private long startNodeId = NEW;
    private long endNodeId = NEW;
    private String type;

    /**
     * Public no-arg constructor (for Jackson)
     */
    public JsonRelationship() {
    }

    /**
     * Create a JSON-serializable representation from a Neo4j relationship.
     *
     * @param relationship to create JSON from.
     */
    public JsonRelationship(Relationship relationship) {
        this(relationship, new JsonInput());
    }

    /**
     * Create a JSON-serializable representation from a Neo4j node.
     *
     * @param relationship to create JSON from.
     * @param jsonInput    specifying what to include in the produced JSON.
     */
    public JsonRelationship(Relationship relationship, JsonInput jsonInput) {
        super(relationship, jsonInput.getRelationshipProperties());
        startNodeId = relationship.getStartNode().getId();
        endNodeId = relationship.getEndNode().getId();
        setType(relationship.getType().name());
    }

    /**
     * Construct a representation of a relationship from its internal Neo4j ID.
     *
     * @param id ID.
     */
    public JsonRelationship(long id) {
        super(id);
    }

    /**
     * Construct a new relationship representation.
     *
     * @param startNodeId start node ID.
     * @param endNodeId   end node ID.
     * @param type        relationship type.
     * @param properties  relationship properties.
     */
    public JsonRelationship(long startNodeId, long endNodeId, String type, Map<String, Object> properties) {
        super(properties);
        this.startNodeId = startNodeId;
        this.endNodeId = endNodeId;
        this.type = type;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Relationship create(GraphDatabaseService database) {
        return database.getNodeById(startNodeId).createRelationshipTo(database.getNodeById(endNodeId), DynamicRelationshipType.withName(type));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Relationship fetch(GraphDatabaseService database) {
        return database.getRelationshipById(getId());
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

        if (startNodeId == NEW || endNodeId == NEW) {
            throw new IllegalStateException("Start and End node IDs must be specified");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void checkCanFetch() {
        super.checkCanFetch();

        if (startNodeId != NEW || endNodeId != NEW) {
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
        this.type = type;
    }

    public long getStartNodeId() {
        return startNodeId;
    }

    public void setStartNodeId(long startNodeId) {
        this.startNodeId = startNodeId;
    }

    public long getEndNodeId() {
        return endNodeId;
    }

    public void setEndNodeId(long endNodeId) {
        this.endNodeId = endNodeId;
    }
}

