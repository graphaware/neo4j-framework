/*
 * Copyright (c) 2015 GraphAware
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

package com.graphaware.runtime.config;

import com.graphaware.common.policy.*;

import static org.springframework.util.Assert.notNull;

/**
 * Base-class for {@link TxDrivenModuleConfiguration} implementations.
 */
public abstract class BaseTxDrivenModuleConfiguration<T extends BaseTxDrivenModuleConfiguration<T>> implements TxDrivenModuleConfiguration {

    private final InclusionPolicies inclusionPolicies;

    /**
     * Construct a new configuration.
     *
     * @param inclusionPolicies policies for inclusion of nodes, relationships, and properties for processing by the module. Must not be <code>null</code>.
     */
    protected BaseTxDrivenModuleConfiguration(InclusionPolicies inclusionPolicies) {
        notNull(inclusionPolicies);
        this.inclusionPolicies = inclusionPolicies;
    }

    /**
     * Create a new instance of this {@link TxDrivenModuleConfiguration} with different inclusion policies.
     *
     * @param inclusionPolicies of the new instance.
     * @return new instance.
     */
    protected abstract T newInstance(InclusionPolicies inclusionPolicies);

    /**
     * Get inclusion policies encapsulated by this configuration.
     *
     * @return policies.
     */
    public InclusionPolicies getInclusionPolicies() {
        return inclusionPolicies;
    }

    /**
     * Create a new instance of this {@link TxDrivenModuleConfiguration} with different node inclusion policy.
     *
     * @param nodeInclusionPolicy of the new instance.
     * @return new instance.
     */
    public T with(NodeInclusionPolicy nodeInclusionPolicy) {
        return newInstance(inclusionPolicies.with(nodeInclusionPolicy));
    }

    /**
     * Create a new instance of this {@link TxDrivenModuleConfiguration} with different node property inclusion policy.
     *
     * @param nodePropertyInclusionPolicy of the new instance.
     * @return new instance.
     */
    public T with(NodePropertyInclusionPolicy nodePropertyInclusionPolicy) {
        return newInstance(inclusionPolicies.with(nodePropertyInclusionPolicy));
    }

    /**
     * Create a new instance of this {@link TxDrivenModuleConfiguration} with different relationship inclusion policy.
     *
     * @param relationshipInclusionPolicy of the new instance.
     * @return new instance.
     */
    public T with(RelationshipInclusionPolicy relationshipInclusionPolicy) {
        return newInstance(inclusionPolicies.with(relationshipInclusionPolicy));
    }

    /**
     * Create a new instance of this {@link TxDrivenModuleConfiguration} with different relationship property inclusion policy.
     *
     * @param relationshipPropertyInclusionPolicy of the new instance.
     * @return new instance.
     */
    public T with(RelationshipPropertyInclusionPolicy relationshipPropertyInclusionPolicy) {
        return newInstance(inclusionPolicies.with(relationshipPropertyInclusionPolicy));
    }

    /**
     * Create w new instance of {@link TxDrivenModuleConfiguration} with different {@link com.graphaware.common.policy.InclusionPolicies}.
     *
     * @param inclusionPolicies of the new instance.
     * @return new instance.
     */
    public T with(InclusionPolicies inclusionPolicies) {
        return newInstance(inclusionPolicies);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        BaseTxDrivenModuleConfiguration that = (BaseTxDrivenModuleConfiguration) o;

        if (!inclusionPolicies.equals(that.inclusionPolicies)) {
            return false;
        }

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return inclusionPolicies.hashCode();
    }
}
