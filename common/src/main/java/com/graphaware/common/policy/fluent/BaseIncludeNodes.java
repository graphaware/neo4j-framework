/*
 * Copyright (c) 2013-2015 GraphAware
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

package com.graphaware.common.policy.fluent;

import com.graphaware.common.description.property.DetachedPropertiesDescription;
import com.graphaware.common.policy.NodeInclusionPolicy;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.parboiled.common.StringUtils;

/**
 * Abstract base class for {@link NodeInclusionPolicy} implementations with fluent interface,
 * intended to be used programmatically.
 */
public abstract class BaseIncludeNodes<T extends BaseIncludeNodes<T>> extends IncludePropertyContainers<T, Node> implements NodeInclusionPolicy {

    private final Label label;

    /**
     * Create a new policy.
     *
     * @param label                 that matching nodes must have, can be null for all labels.
     * @param propertiesDescription of the matching nodes.
     */
    public BaseIncludeNodes(Label label, DetachedPropertiesDescription propertiesDescription) {
        super(propertiesDescription);
        this.label = label;
    }

    /**
     * Create a new policy from the current one, reconfigured to only match nodes with the given label.
     *
     * @param label that matching nodes must have, can be null for all labels.
     * @return reconfigured policy.
     */
    public T with(String label) {
        if (label == null) {
            return with((Label) null);
        }

        if (StringUtils.isEmpty(label)) {
            throw new IllegalArgumentException("Empty labels are not supported"); //just because it's not a good idea and usually indicates a bug
        }

        return with(DynamicLabel.label(label));
    }

    /**
     * Create a new policy from the current one, reconfigured to only match nodes with the given label.
     *
     * @param label that matching nodes must have, can be null for all labels.
     * @return reconfigured policy.
     */
    public T with(Label label) {
        return newInstance(label);
    }

    /**
     * Create a new instance of this policy with the given label.
     *
     * @param label of the new policy.
     * @return new policy.
     */
    protected abstract T newInstance(Label label);

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean include(Node node) {
        if (label != null && !node.hasLabel(label)) {
            return false;
        }

        return super.include(node);
    }

    /**
     * Get the label with which this policy has been configured.
     *
     * @return label, can be <code>null</code> (representing any label incl. none).
     */
    public Label getLabel() {
        return label;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        BaseIncludeNodes that = (BaseIncludeNodes) o;

        if (label != null ? !label.equals(that.label) : that.label != null) return false;

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (label != null ? label.hashCode() : 0);
        return result;
    }
}
