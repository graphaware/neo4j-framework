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

import com.graphaware.common.wrapper.NodeWrapper;
import org.neo4j.graphdb.*;
import org.neo4j.unsafe.batchinsert.BatchInserter;

import java.util.Collections;

/**
 * {@link org.neo4j.graphdb.Node} proxy to be used in {@link com.graphaware.tx.event.batch.data.BatchTransactionData} when using
 * {@link com.graphaware.tx.event.batch.api.TransactionSimulatingBatchInserter} for batch inserts.
 */
public class BatchInserterNode extends BatchInserterPropertyContainer<Node> implements Node, NodeWrapper {

    /**
     * Construct a new proxy node.
     *
     * @param id            of the node.
     * @param batchInserter currently used for batch inserts.
     */
    public BatchInserterNode(long id, BatchInserter batchInserter) {
        super(id, batchInserter);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Node self() {
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasProperty(String key) {
        return batchInserter.nodeHasProperty(id, key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getProperty(String key) {
        if (!hasProperty(key)) {
            throw new NotFoundException();
        }
        return batchInserter.getNodeProperties(id).get(key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setProperty(String key, Object value) {
        batchInserter.setNodeProperty(id, key, value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object removeProperty(String key) {
        Object oldValue = getProperty(key, null);
        batchInserter.removeNodeProperty(id, key);
        return oldValue;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterable<String> getPropertyKeys() {
        return batchInserter.getNodeProperties(id).keySet();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterable<Relationship> getRelationships(Direction direction, RelationshipType... types) {
        return new BatchInserterRelationshipIterator(id, batchInserter, direction, types);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Relationship createRelationshipTo(Node otherNode, RelationshipType type) {
        return new BatchInserterRelationship(batchInserter.createRelationship(id, otherNode.getId(), type, Collections.<String, Object>emptyMap()), id, otherNode.getId(), type, batchInserter);
    }
}
