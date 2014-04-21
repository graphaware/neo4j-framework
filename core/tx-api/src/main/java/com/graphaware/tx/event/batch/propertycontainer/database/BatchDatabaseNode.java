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

import com.graphaware.common.wrapper.NodeWrapper;
import com.graphaware.tx.event.batch.data.BatchTransactionData;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.unsafe.batchinsert.TransactionSimulatingBatchGraphDatabase;

import java.util.LinkedList;
import java.util.List;

import static org.neo4j.helpers.collection.Iterables.append;
import static org.neo4j.helpers.collection.Iterables.toList;

/**
 * {@link org.neo4j.graphdb.Node} proxy to be used in {@link com.graphaware.tx.event.batch.data.BatchTransactionData}
 * when using {@link org.neo4j.unsafe.batchinsert.TransactionSimulatingBatchGraphDatabase} for batch inserts.
 */
public class BatchDatabaseNode extends BatchDatabasePropertyContainer<Node> implements Node, NodeWrapper {

    private final BatchTransactionData transactionData;

    /**
     * Construct a new proxy node.
     *
     * @param id              of the node.
     * @param database        currently used for batch inserts.
     * @param transactionData data captured about the fake transaction.
     */
    public BatchDatabaseNode(long id, TransactionSimulatingBatchGraphDatabase database, BatchTransactionData transactionData) {
        super(id, database);
        this.transactionData = transactionData;
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
    public Node getWrapped() {
        return database.getNodeByIdInternal(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setProperty(String key, Object value) {
        transactionData.nodePropertyToBeSet(this, key, value);
        super.setProperty(key, value);
        transactionData.nodePropertySet(this, key, value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object removeProperty(String key) {
        transactionData.nodePropertyToBeRemoved(this, key);
        Object result = null;
        if (hasProperty(key)) { //workaround for https://github.com/neo4j/neo4j/issues/1025
            result = super.removeProperty(key);
        }
        transactionData.nodePropertyRemoved(this, key);
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addLabel(Label label) {
        List<Label> newLabels = new LinkedList<>(toList(append(label, toList(getLabels()))));
        Label[] labels = newLabels.toArray(new Label[newLabels.size()]);

        transactionData.nodeLabelsToBeSet(this, labels);
        super.addLabel(label);
        transactionData.nodeLabelsSet(this, labels);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeLabel(Label label) {
        List<Label> newLabels = new LinkedList<>();
        for (Label existingLabel : getLabels()) {
            if (!existingLabel.name().equals(label.name())) {
                newLabels.add(existingLabel);
            }
        }

        Label[] labels = newLabels.toArray(new Label[newLabels.size()]);

        transactionData.nodeLabelsToBeSet(this, labels);
        super.removeLabel(label);
        transactionData.nodeLabelsSet(this, labels);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Relationship createRelationshipTo(Node otherNode, RelationshipType type) {
        BatchDatabaseRelationship relationship = new BatchDatabaseRelationship(getWrapped().createRelationshipTo(otherNode, type).getId(), database, transactionData);
        transactionData.relationshipCreated(relationship);
        return relationship;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Relationship wrapRelationship(Relationship relationship) {
        return new BatchDatabaseRelationship(relationship.getId(), database, transactionData);
    }
}
