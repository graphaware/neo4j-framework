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

package com.graphaware.tx.event.improved.data.lazy;

import com.graphaware.tx.event.improved.data.NodeTransactionData;
import com.graphaware.tx.event.improved.data.TransactionDataContainer;
import com.graphaware.tx.event.improved.propertycontainer.snapshot.NodeSnapshot;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.event.LabelEntry;
import org.neo4j.graphdb.event.PropertyEntry;
import org.neo4j.graphdb.event.TransactionData;
import org.neo4j.logging.Log;
import org.slf4j.Logger;
import com.graphaware.common.log.LoggerFactory;

import java.util.*;

/**
 * {@link LazyPropertyContainerTransactionData} for {@link org.neo4j.graphdb.Node}s.
 */
public class LazyNodeTransactionData extends LazyPropertyContainerTransactionData<Node> implements NodeTransactionData {
    private static final Log LOG = LoggerFactory.getLogger(LazyNodeTransactionData.class);

    private final TransactionData transactionData;
    private final TransactionDataContainer transactionDataContainer;

    private Map<Long, Set<Label>> assignedLabels = null;
    private Map<Long, Set<Label>> removedLabels = null;
    private Map<Long, Set<Label>> deletedNodeLabels = null;

    /**
     * Construct node transaction data from Neo4j {@link org.neo4j.graphdb.event.TransactionData}.
     *
     * @param transactionData          provided by Neo4j.
     * @param transactionDataContainer containing {@link com.graphaware.tx.event.improved.data.PropertyContainerTransactionData}..
     */
    public LazyNodeTransactionData(TransactionData transactionData, TransactionDataContainer transactionDataContainer) {
        this.transactionData = transactionData;
        this.transactionDataContainer = transactionDataContainer;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Node oldSnapshot(Node original) {
        return new NodeSnapshot(original, transactionDataContainer);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Node newSnapshot(Node original) {
        return original;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Iterable<Node> created() {
        return transactionData.createdNodes();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Iterable<Node> deleted() {
        return transactionData.deletedNodes();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Iterable<PropertyEntry<Node>> assignedProperties() {
        return transactionData.assignedNodeProperties();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Iterable<PropertyEntry<Node>> removedProperties() {
        return transactionData.removedNodeProperties();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasLabelBeenAssigned(Node node, Label label) {
        initializeChanged();

        if (!hasBeenChanged(node)) {
//            LOG.warn(node + " has not been changed but the caller thinks it should have assigned labels.");
            return false;
        }

        if (!assignedLabels.containsKey(node.getId())) {
            return false;
        }

        return assignedLabels.get(node.getId()).contains(label);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Label> assignedLabels(Node node) {
        initializeChanged();

        if (!hasBeenChanged(node)) {
            LOG.warn(node + " has not been changed but the caller thinks it should have assigned labels.");
            return Collections.emptySet();
        }

        if (!assignedLabels.containsKey(node.getId())) {
            return Collections.emptySet();
        }

        return Collections.unmodifiableSet(assignedLabels.get(node.getId()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasLabelBeenRemoved(Node node, Label label) {
        initializeChanged();

        if (!hasBeenChanged(node)) {
            LOG.warn(node + " has not been changed but the caller thinks it should have removed labels.");
            return false;
        }

        if (!removedLabels.containsKey(node.getId())) {
            return false;
        }

        return removedLabels.get(node.getId()).contains(label);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Label> removedLabels(Node node) {
        initializeChanged();

        if (!hasBeenChanged(node)) {
            LOG.warn(node + " has not been changed but the caller thinks it should have removed labels.");
            return Collections.emptySet();
        }

        if (!removedLabels.containsKey(node.getId())) {
            return Collections.emptySet();
        }

        return Collections.unmodifiableSet(removedLabels.get(node.getId()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Label> labelsOfDeletedNode(Node node) {
        initializeChanged();

        if (!hasBeenDeleted(node)) {
            LOG.error(node + " has not been deleted but the caller thinks it has! This is a bug.");
            throw new IllegalStateException(node + " has not been deleted but the caller thinks it has! This is a bug.");
        }

        if (!deletedNodeLabels.containsKey(node.getId())) {
            return Collections.emptySet();
        }

        return Collections.unmodifiableSet(deletedNodeLabels.get(node.getId()));
    }

    @Override
    protected void doInitializeChanged() {
        assignedLabels = new HashMap<>();
        removedLabels = new HashMap<>();
        deletedNodeLabels = new HashMap<>();

        Map<Long, Node> potentiallyChangedNodes = new HashMap<>();

        for (LabelEntry labelEntry : transactionData.assignedLabels()) {
            Node node = labelEntry.node();

            if (hasBeenCreated(node)) {
                continue;
            }

            if (!assignedLabels.containsKey(node.getId())) {
                assignedLabels.put(node.getId(), new HashSet<Label>());
            }

            assignedLabels.get(node.getId()).add(labelEntry.label());

            potentiallyChangedNodes.put(node.getId(), node);
        }

        for (LabelEntry labelEntry : transactionData.removedLabels()) {
            Node node = labelEntry.node();

            if (hasBeenDeleted(node)) {
                if (!deletedNodeLabels.containsKey(node.getId())) {
                    deletedNodeLabels.put(node.getId(), new HashSet<Label>());
                }
                deletedNodeLabels.get(node.getId()).add(labelEntry.label());
                continue;
            }

            if (!removedLabels.containsKey(node.getId())) {
                removedLabels.put(node.getId(), new HashSet<Label>());
            }
            removedLabels.get(node.getId()).add(labelEntry.label());

            potentiallyChangedNodes.put(node.getId(), node);
        }

        for (Long nodeId : assignedLabels.keySet()) {
            registerChange(potentiallyChangedNodes.get(nodeId));
        }

        for (Long nodeId : removedLabels.keySet()) {
            registerChange(potentiallyChangedNodes.get(nodeId));
        }
    }
}
