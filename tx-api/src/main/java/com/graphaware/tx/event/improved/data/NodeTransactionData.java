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

package com.graphaware.tx.event.improved.data;

import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;

import java.util.Set;

/**
 * {@link PropertyContainerTransactionData} for {@link org.neo4j.graphdb.Node}s.
 */
public interface NodeTransactionData extends PropertyContainerTransactionData<Node> {

    /**
     * Check whether a label has been assigned in the transaction.
     *
     * @param node  to check.
     * @param label to check.
     * @return true iff the node has been assigned.
     */
    boolean hasLabelBeenAssigned(Node node, Label label);

    /**
     * Get labels assigned in the transaction.
     *
     * @param node for which to get assigned labels.
     * @return read-only labels created for the given node.
     */
    Set<Label> assignedLabels(Node node);

    /**
     * Check whether a label has been removed in the transaction.
     *
     * @param node  to check.
     * @param label to check.
     * @return true iff the label has been removed.
     */
    boolean hasLabelBeenRemoved(Node node, Label label);

    /**
     * Get labels removed in the transaction.
     *
     * @param node for which to get removed labels.
     * @return read-only labels removed for the given node.
     */
    Set<Label> removedLabels(Node node);

    /**
     * Get labels of a deleted node.
     *
     * @param node deleted node.
     * @return read-only labels of the deleted node.
     */
    Set<Label> labelsOfDeletedNode(Node node);
}
