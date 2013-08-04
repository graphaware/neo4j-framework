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

package com.graphaware.test;

import com.graphaware.tx.executor.single.SimpleTransactionExecutor;
import com.graphaware.tx.executor.single.TransactionCallback;
import com.graphaware.tx.executor.single.TransactionExecutor;
import org.neo4j.graphdb.*;

/**
 * A convenient test data builder with fluent API.
 */
public class TestDataBuilder {

    private final TransactionExecutor executor;

    private Node lastNode;
    private Relationship lastRelationship;

    public TestDataBuilder(GraphDatabaseService database) {
        this.executor = new SimpleTransactionExecutor(database);
    }

    /**
     * Create a node.
     *
     * @return this.
     */
    public TestDataBuilder node() {
        lastRelationship = null;
        lastNode = executor.executeInTransaction(new TransactionCallback<Node>() {
            @Override
            public Node doInTransaction(GraphDatabaseService database) {
                return database.createNode();
            }
        });

        return this;
    }

    /**
     * Create a relationship from the last created node to a node with specific ID. Creates its own transaction.
     *
     * @param nodeId to create the relationship to.
     * @param type   of the relationship as String.
     * @return this.
     */
    public TestDataBuilder relationshipTo(final long nodeId, final String type) {
        if (lastNode == null) {
            throw new IllegalStateException("Illegal usage! There's no node to create the relationship from. Please call node() first.");
        }

        final Node node = lastNode;

        lastRelationship = executor.executeInTransaction(new TransactionCallback<Relationship>() {
            @Override
            public Relationship doInTransaction(GraphDatabaseService database) {
                return node.createRelationshipTo(database.getNodeById(nodeId), DynamicRelationshipType.withName(type));
            }
        });

        return this;
    }

    /**
     * Create a relationship to the last created node from a node with specific ID. Creates its own transaction.
     *
     * @param nodeId to create the relationship from.
     * @param type   of the relationship as String.
     * @return this.
     */
    public TestDataBuilder relationshipFrom(final long nodeId, final String type) {
        if (lastNode == null) {
            throw new IllegalStateException("Illegal usage! There's no node to create the relationship tp. Please call node() first.");
        }

        final Node node = lastNode;

        lastRelationship = executor.executeInTransaction(new TransactionCallback<Relationship>() {
            @Override
            public Relationship doInTransaction(GraphDatabaseService database) {
                return database.getNodeById(nodeId).createRelationshipTo(node, DynamicRelationshipType.withName(type));
            }
        });

        return this;
    }

    /**
     * Set a property on the last created node or relationship. Creates its own transaction.
     *
     * @param key   key.
     * @param value value.
     * @return this.
     */
    public TestDataBuilder setProp(final String key, final Object value) {
        if (lastNode == null && lastRelationship == null) {
            throw new IllegalStateException("Illegal usage! There's nothing to set the property on. Please call node() or createRelationship() first.");
        }

        final PropertyContainer propertyContainer = lastRelationship != null ? lastRelationship : lastNode;
        executor.executeInTransaction(new TransactionCallback<Void>() {
            @Override
            public Void doInTransaction(GraphDatabaseService database) {
                propertyContainer.setProperty(key, value);
                return null;
            }
        });

        return this;
    }
}
