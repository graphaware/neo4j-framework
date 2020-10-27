/*
 * Copyright (c) 2013-2020 GraphAware
 *
 * This file is part of the GraphAware Framework.
 *
 * GraphAware Framework is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software Foundation, either
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
import com.graphaware.common.policy.inclusion.EntityInclusionPolicy;
import com.graphaware.common.policy.inclusion.PropertyInclusionPolicy;
import com.graphaware.common.util.Change;
import com.graphaware.tx.event.improved.data.NodeTransactionData;
import com.graphaware.tx.event.improved.entity.filtered.FilteredNode;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;

import java.util.Set;

/**
 * {@link FilteredEntityTransactionData} for {@link org.neo4j.graphdb.Node}s.
 */
public class FilteredNodeTransactionData extends FilteredEntityTransactionData<Node> implements NodeTransactionData {

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

    @Override
    protected NodeTransactionData getWrapped() {
        return wrapped;
    }

    @Override
    protected Node filtered(Node original) {
        return new FilteredNode(original, policies);
    }

    @Override
    protected EntityInclusionPolicy<Node> getEntityInclusionPolicy() {
        return policies.getNodeInclusionPolicy();
    }

    @Override
    protected PropertyInclusionPolicy<Node> getPropertyInclusionPolicy() {
        return policies.getNodePropertyInclusionPolicy();
    }

    @Override
    public boolean hasLabelBeenAssigned(Node node, Label label) {
        return getWrapped().hasLabelBeenAssigned(node, label);
    }

    @Override
    public Set<Label> assignedLabels(Node node) {
        return getWrapped().assignedLabels(node);
    }

    @Override
    public boolean hasLabelBeenRemoved(Node node, Label label) {
        return getWrapped().hasLabelBeenRemoved(node, label);
    }

    @Override
    public Set<Label> removedLabels(Node node) {
        return getWrapped().removedLabels(node);
    }

    @Override
    public Set<Label> labelsOfDeletedNode(Node node) {
        return getWrapped().labelsOfDeletedNode(node);
    }

    @Override
    protected boolean hasChanged(Change<Node> candidate) {
        return super.hasChanged(candidate) || !assignedLabels(candidate.getPrevious()).isEmpty() || !removedLabels(candidate.getPrevious()).isEmpty();
    }
}
