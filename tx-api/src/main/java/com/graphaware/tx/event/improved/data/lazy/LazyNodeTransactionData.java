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

package com.graphaware.tx.event.improved.data.lazy;

import com.graphaware.tx.event.improved.api.Change;
import com.graphaware.tx.event.improved.data.NodeTransactionData;
import com.graphaware.tx.event.improved.data.TransactionDataContainer;
import com.graphaware.tx.event.improved.propertycontainer.snapshot.NodeSnapshot;
import org.apache.log4j.Logger;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.event.LabelEntry;
import org.neo4j.graphdb.event.PropertyEntry;
import org.neo4j.graphdb.event.TransactionData;

import java.util.*;

import static com.graphaware.common.util.PropertyContainerUtils.id;

/**
 * {@link LazyPropertyContainerTransactionData} for {@link org.neo4j.graphdb.Node}s.
 */
public class LazyNodeTransactionData extends LazyPropertyContainerTransactionData<Node> implements NodeTransactionData {
    private static final Logger LOG = Logger.getLogger(LazyPropertyContainerTransactionData.class);

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
            LOG.warn(node + " has not been changed but the caller thinks it should have assigned labels.");
            return false;
        }

        if (!assignedLabels.containsKey(id(node))) {
            return false;
        }

        return assignedLabels.get(id(node)).contains(label);
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

        if (!assignedLabels.containsKey(id(node))) {
            return Collections.emptySet();
        }

        return Collections.unmodifiableSet(assignedLabels.get(id(node)));
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

        if (!removedLabels.containsKey(id(node))) {
            return false;
        }

        return removedLabels.get(id(node)).contains(label);
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

        if (!removedLabels.containsKey(id(node))) {
            return Collections.emptySet();
        }

        return Collections.unmodifiableSet(removedLabels.get(id(node)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Label> labelsOfDeletedNode(Node node) {
        initializeChanged();

        if (!hasBeenDeleted(node)) {
            LOG.warn(node + " has not been deleted but the caller thinks it has!");
            return Collections.emptySet();
        }

        if (!deletedNodeLabels.containsKey(id(node))) {
            return Collections.emptySet();
        }

        return Collections.unmodifiableSet(deletedNodeLabels.get(id(node)));
    }

    @Override
    protected void doInitializeChanged() {
        assignedLabels = new HashMap<>();
        removedLabels = new HashMap<>();
        deletedNodeLabels = new HashMap<>();

        for (LabelEntry labelEntry : transactionData.assignedLabels()) {
            Node node = labelEntry.node();

            if (hasBeenCreated(node)) {
                continue;
            }

            if (!assignedLabels.containsKey(id(node))) {
                assignedLabels.put(id(node), new HashSet<Label>());
            }

            assignedLabels.get(id(node)).add(labelEntry.label());

            registerChange(node);
        }

        for (LabelEntry labelEntry : transactionData.removedLabels()) {
            Node node = labelEntry.node();

            if (hasBeenDeleted(node)) {
                if (!deletedNodeLabels.containsKey(id(node))) {
                    deletedNodeLabels.put(id(node), new HashSet<Label>());
                }
                deletedNodeLabels.get(id(node)).add(labelEntry.label());
                continue;
            }

            if (!removedLabels.containsKey(id(node))) {
                removedLabels.put(id(node), new HashSet<Label>());
            }

            removedLabels.get(id(node)).add(labelEntry.label());

            registerChange(node);
        }
    }
}
