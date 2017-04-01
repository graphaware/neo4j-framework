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

package com.graphaware.tx.event.improved.data.filtered;

import com.graphaware.common.policy.inclusion.InclusionPolicies;
import com.graphaware.common.policy.inclusion.PropertyContainerInclusionPolicy;
import com.graphaware.common.policy.inclusion.PropertyInclusionPolicy;
import com.graphaware.common.util.Change;
import com.graphaware.tx.event.improved.data.NodeTransactionData;
import com.graphaware.tx.event.improved.propertycontainer.filtered.FilteredNode;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;

import java.util.Set;

/**
 * {@link FilteredPropertyContainerTransactionData} for {@link org.neo4j.graphdb.Node}s.
 */
public class FilteredNodeTransactionData extends FilteredPropertyContainerTransactionData<Node> implements NodeTransactionData {

    private final NodeTransactionData wrapped;

    /**
     * Construct filtered node transaction data.
     *
     * @param wrapped    wrapped node transaction data.
     * @param policies for filtering.
     */
    public FilteredNodeTransactionData(NodeTransactionData wrapped, InclusionPolicies policies) {
        super(policies);
        this.wrapped = wrapped;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected NodeTransactionData getWrapped() {
        return wrapped;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Node filtered(Node original) {
        return new FilteredNode(original, policies);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected PropertyContainerInclusionPolicy<Node> getPropertyContainerInclusionPolicy() {
        return policies.getNodeInclusionPolicy();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected PropertyInclusionPolicy<Node> getPropertyInclusionPolicy() {
        return policies.getNodePropertyInclusionPolicy();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasLabelBeenAssigned(Node node, Label label) {
        return getWrapped().hasLabelBeenAssigned(node, label);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Label> assignedLabels(Node node) {
        return getWrapped().assignedLabels(node);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasLabelBeenRemoved(Node node, Label label) {
        return getWrapped().hasLabelBeenRemoved(node, label);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Label> removedLabels(Node node) {
        return getWrapped().removedLabels(node);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Label> labelsOfDeletedNode(Node node) {
        return getWrapped().labelsOfDeletedNode(node);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean hasChanged(Change<Node> candidate) {
        return super.hasChanged(candidate) || !assignedLabels(candidate.getPrevious()).isEmpty() || !removedLabels(candidate.getPrevious()).isEmpty();
    }
}
