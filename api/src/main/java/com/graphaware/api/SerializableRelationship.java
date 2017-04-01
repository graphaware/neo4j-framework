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

package com.graphaware.api;

import com.graphaware.common.transform.NodeIdTransformer;
import com.graphaware.common.transform.RelationshipIdTransformer;
import com.graphaware.common.representation.DetachedRelationship;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Relationship;

import java.util.Map;

/**
 * Serializable {@link DetachedRelationship} with custom ID. It is recommended not to expose Neo4j internal IDs (graphId)
 * outside of the database.
 *
 * @param <ID> type of custom node/relationship IDs.
 */
public class SerializableRelationship<ID> extends DetachedRelationship<ID, SerializableNode<ID>> {

    private ID id;
    private ID startNodeId;
    private ID endNodeId;

    /**
     * Public no-arg constructor (for Jackson et. al.)
     */
    public SerializableRelationship() {
    }

    /**
     * Create a Serializable {@link DetachedRelationship} from a Neo4j relationship. All properties will be included.
     *
     * @param relationship              relationship to create the representation from.
     * @param relationshipIdTransformer ID transformer for relationship IDs.
     * @param nodeIdTransformer         ID transformer for node IDs.
     */
    public SerializableRelationship(Relationship relationship, RelationshipIdTransformer<ID> relationshipIdTransformer, NodeIdTransformer<ID> nodeIdTransformer) {
        this(relationship, null, relationshipIdTransformer, nodeIdTransformer);
    }

    /**
     * Create a Serializable {@link DetachedRelationship} from a Neo4j relationship. All properties will be included.
     *
     * @param relationship              relationship to create the representation from.
     * @param properties                keys of properties to be included in the representation.
     *                                  Can be <code>null</code>, which represents all. Empty array represents none.
     * @param relationshipIdTransformer ID transformer for relationship IDs.
     * @param nodeIdTransformer         ID transformer for node IDs.
     */
    public SerializableRelationship(Relationship relationship, String[] properties, RelationshipIdTransformer<ID> relationshipIdTransformer, NodeIdTransformer<ID> nodeIdTransformer) {
        super(relationship, properties, nodeIdTransformer);

        setId(relationshipIdTransformer.fromContainer(relationship));
        setStartNodeId(nodeIdTransformer.fromContainer(relationship.getStartNode()));
        setEndNodeId(nodeIdTransformer.fromContainer(relationship.getEndNode()));
    }

    /**
     * Create a Serializable {@link DetachedRelationship} from own relationship ID.
     *
     * @param id          of the relationship. Must not be <code>null</code>.
     */
    public SerializableRelationship(ID id) {
        this.id = id;
    }

    /**
     * Construct Serializable {@link DetachedRelationship} of a relationship.
     *
     * @param id                custom ID of the relationship. Can be <code>null</code> to represent a new relationship.
     * @param startNodeId       own start node ID.
     * @param endNodeId         own end node ID.
     * @param type              relationship type.
     * @param properties        relationship properties.
     */
    public SerializableRelationship(ID id, ID startNodeId, ID endNodeId, String type, Map<String, Object> properties) {
        super(NEW, NEW, NEW, type, properties);
        this.id = id;
        this.startNodeId = startNodeId;
        this.endNodeId = endNodeId;
    }

    /**
     * Produce a {@link Relationship} from this representation. This means either fetch the relationship from the
     * given database (iff id is set), or create it.
     *
     * @param database                  to create/fetch relationship in.
     * @param relationshipIdTransformer ID transformer for relationship IDs.
     * @param nodeIdTransformer         ID transformer for node IDs.
     * @return container.
     */
    public Relationship producePropertyContainer(GraphDatabaseService database, RelationshipIdTransformer<ID> relationshipIdTransformer, NodeIdTransformer<ID> nodeIdTransformer) {
        setGraphId(relationshipIdTransformer.toGraphId(id));
        setStartNodeGraphId(nodeIdTransformer.toGraphId(startNodeId));
        setEndNodeGraphId(nodeIdTransformer.toGraphId(endNodeId));
        return super.producePropertyContainer(database);
    }

    public ID getId() {
        return id;
    }

    public void setId(ID id) {
        this.id = id;
    }

    public ID getStartNodeId() {
        return startNodeId;
    }

    public void setStartNodeId(ID startNodeId) {
        this.startNodeId = startNodeId;
    }

    public ID getEndNodeId() {
        return endNodeId;
    }

    public void setEndNodeId(ID endNodeId) {
        this.endNodeId = endNodeId;
    }

    @Override
    protected SerializableNode<ID> startNode(Relationship relationship, NodeIdTransformer<ID> nodeIdTransformer) {
        return new SerializableNode<>(relationship.getStartNode(), nodeIdTransformer);
    }

    @Override
    protected SerializableNode<ID> endNode(Relationship relationship, NodeIdTransformer<ID> nodeIdTransformer) {
        return new SerializableNode<>(relationship.getEndNode(), nodeIdTransformer);
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

        SerializableRelationship<?> that = (SerializableRelationship<?>) o;

        if (id != null ? !id.equals(that.id) : that.id != null) {
            return false;
        }
        if (startNodeId != null ? !startNodeId.equals(that.startNodeId) : that.startNodeId != null) {
            return false;
        }
        return !(endNodeId != null ? !endNodeId.equals(that.endNodeId) : that.endNodeId != null);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (id != null ? id.hashCode() : 0);
        result = 31 * result + (startNodeId != null ? startNodeId.hashCode() : 0);
        result = 31 * result + (endNodeId != null ? endNodeId.hashCode() : 0);
        return result;
    }
}

