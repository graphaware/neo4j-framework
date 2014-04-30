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

package com.graphaware.tx.event.batch.propertycontainer.database;

import com.graphaware.common.wrapper.RelationshipWrapper;
import com.graphaware.tx.event.batch.data.BatchTransactionData;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.unsafe.batchinsert.TransactionSimulatingBatchGraphDatabase;

/**
 * {@link org.neo4j.graphdb.Relationship} proxy to be used in {@link com.graphaware.tx.event.batch.data.BatchTransactionData}
 * when using {@link org.neo4j.unsafe.batchinsert.TransactionSimulatingBatchGraphDatabase} for batch inserts.
 */
public class BatchDatabaseRelationship extends BatchDatabasePropertyContainer<Relationship> implements Relationship, RelationshipWrapper {

    protected final BatchTransactionData transactionData;

    /**
     * Construct a new proxy relationship.
     *
     * @param id              of the relationship.
     * @param database        currently used for batch inserts.
     * @param transactionData data captured about the fake transaction.
     */
    public BatchDatabaseRelationship(long id, TransactionSimulatingBatchGraphDatabase database, BatchTransactionData transactionData) {
        super(id, database);
        this.transactionData = transactionData;
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
    public Relationship getWrapped() {
        return database.getRelationshipByIdInternal(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setProperty(String key, Object value) {
        transactionData.relationshipPropertyToBeSet(this, key, value);
        super.setProperty(key, value);
        transactionData.relationshipPropertySet(this, key, value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object removeProperty(String key) {
        transactionData.relationshipPropertyToBeRemoved(this, key);
        Object result = null;
        if (hasProperty(key)) { //workaround for https://github.com/neo4j/neo4j/issues/1025
            result = super.removeProperty(key);
        }
        transactionData.relationshipPropertyRemoved(this, key);
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Node wrapNode(Node node) {
        return new BatchDatabaseNode(node.getId(), database, transactionData);
    }
}
