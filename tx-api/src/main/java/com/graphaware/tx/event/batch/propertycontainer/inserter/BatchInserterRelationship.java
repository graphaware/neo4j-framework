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

package com.graphaware.tx.event.batch.propertycontainer.inserter;

import com.graphaware.common.wrapper.RelationshipWrapper;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.NotFoundException;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.unsafe.batchinsert.BatchInserter;
import org.neo4j.unsafe.batchinsert.BatchRelationship;

/**
 * {@link org.neo4j.graphdb.Relationship} proxy to be used in {@link com.graphaware.tx.event.batch.data.BatchTransactionData} when using
 * {@link com.graphaware.tx.event.batch.api.TransactionSimulatingBatchInserter} for batch inserts.
 */
public class BatchInserterRelationship extends BatchInserterPropertyContainer<Relationship> implements Relationship, RelationshipWrapper {

    private final long startNodeId;
    private final long endNodeId;
    private final RelationshipType type;

    /**
     * Construct a new proxy relationship.
     *
     * @param batchRelationship from Neo4j batch API.
     * @param batchInserter     currently used for batch inserts.
     */
    public BatchInserterRelationship(BatchRelationship batchRelationship, BatchInserter batchInserter) {
        this(batchRelationship.getId(), batchRelationship.getStartNode(), batchRelationship.getEndNode(), batchRelationship.getType(), batchInserter);
    }

    /**
     * Construct a new proxy relationship.
     *
     * @param id            of the relationship.
     * @param startNodeId   ID of the start node.
     * @param endNodeId     ID of the end node.
     * @param type          of the relationship.
     * @param batchInserter currently used for batch inserts.
     */
    public BatchInserterRelationship(long id, long startNodeId, long endNodeId, RelationshipType type, BatchInserter batchInserter) {
        super(id, batchInserter);
        this.startNodeId = startNodeId;
        this.endNodeId = endNodeId;
        this.type = type;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Relationship self() {
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Node getStartNode() {
        return new BatchInserterNode(startNodeId, batchInserter);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Node getEndNode() {
        return new BatchInserterNode(endNodeId, batchInserter);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RelationshipType getType() {
        return type;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasProperty(String key) {
        return batchInserter.relationshipHasProperty(id, key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getProperty(String key) {
        if (!hasProperty(key)) {
            throw new NotFoundException();
        }
        return batchInserter.getRelationshipProperties(id).get(key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setProperty(String key, Object value) {
        batchInserter.setRelationshipProperty(id, key, value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object removeProperty(String key) {
        Object oldValue = getProperty(key, null);
        batchInserter.removeRelationshipProperty(id, key);
        return oldValue;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterable<String> getPropertyKeys() {
        return batchInserter.getRelationshipProperties(id).keySet();
    }
}
