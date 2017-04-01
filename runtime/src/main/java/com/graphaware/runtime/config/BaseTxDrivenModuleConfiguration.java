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

package com.graphaware.runtime.config;

import com.graphaware.common.policy.inclusion.*;

import static org.springframework.util.Assert.notNull;

/**
 * Base-class for {@link TxDrivenModuleConfiguration} implementations.
 */
public abstract class BaseTxDrivenModuleConfiguration<T extends BaseTxDrivenModuleConfiguration<T>> implements TxDrivenModuleConfiguration {

    private final InclusionPolicies inclusionPolicies;
    private final long initializeUntil;

    /**
     * Construct a new configuration.
     *
     * @param inclusionPolicies policies for inclusion of nodes, relationships, and properties for processing by the module. Must not be <code>null</code>.
     * @deprecated use {@link #BaseTxDrivenModuleConfiguration(InclusionPolicies, long)}
     */
    @Deprecated
    protected BaseTxDrivenModuleConfiguration(InclusionPolicies inclusionPolicies) {
        this(inclusionPolicies, ALWAYS);
    }

    /**
     * Construct a new configuration.
     *
     * @param inclusionPolicies policies for inclusion of nodes, relationships, and properties for processing by the module. Must not be <code>null</code>.
     * @param initializeUntil   until what time in ms since epoch it is ok to re(initialize) the entire module in case the configuration
     *                          has changed since the last time the module was started, or if it is the first time the module was registered.
     *                          {@link #NEVER} for never, {@link #ALWAYS} for always.
     */
    protected BaseTxDrivenModuleConfiguration(InclusionPolicies inclusionPolicies, long initializeUntil) {
        notNull(inclusionPolicies);
        this.inclusionPolicies = inclusionPolicies;
        this.initializeUntil = initializeUntil;
    }

    /**
     * Create a new instance of this {@link TxDrivenModuleConfiguration} with different inclusion policies.
     *
     * @param inclusionPolicies of the new instance.
     * @param initializeUntil   of the new instance.
     * @return new instance.
     */
    protected abstract T newInstance(InclusionPolicies inclusionPolicies, long initializeUntil);

    /**
     * {@inheritDoc}
     */
    @Override
    public InclusionPolicies getInclusionPolicies() {
        return inclusionPolicies;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long initializeUntil() {
        return initializeUntil;
    }

    /**
     * Create a new instance of this {@link TxDrivenModuleConfiguration} with different node inclusion policy.
     *
     * @param nodeInclusionPolicy of the new instance.
     * @return new instance.
     */
    public T with(NodeInclusionPolicy nodeInclusionPolicy) {
        return newInstance(inclusionPolicies.with(nodeInclusionPolicy), initializeUntil);
    }

    /**
     * Create a new instance of this {@link TxDrivenModuleConfiguration} with different node property inclusion policy.
     *
     * @param nodePropertyInclusionPolicy of the new instance.
     * @return new instance.
     */
    public T with(NodePropertyInclusionPolicy nodePropertyInclusionPolicy) {
        return newInstance(inclusionPolicies.with(nodePropertyInclusionPolicy), initializeUntil);
    }

    /**
     * Create a new instance of this {@link TxDrivenModuleConfiguration} with different relationship inclusion policy.
     *
     * @param relationshipInclusionPolicy of the new instance.
     * @return new instance.
     */
    public T with(RelationshipInclusionPolicy relationshipInclusionPolicy) {
        return newInstance(inclusionPolicies.with(relationshipInclusionPolicy), initializeUntil);
    }

    /**
     * Create a new instance of this {@link TxDrivenModuleConfiguration} with different relationship property inclusion policy.
     *
     * @param relationshipPropertyInclusionPolicy of the new instance.
     * @return new instance.
     */
    public T with(RelationshipPropertyInclusionPolicy relationshipPropertyInclusionPolicy) {
        return newInstance(inclusionPolicies.with(relationshipPropertyInclusionPolicy), initializeUntil);
    }

    /**
     * Create a new instance of {@link TxDrivenModuleConfiguration} with different {@link InclusionPolicies}.
     *
     * @param inclusionPolicies of the new instance.
     * @return new instance.
     */
    public T with(InclusionPolicies inclusionPolicies) {
        return newInstance(inclusionPolicies, initializeUntil);
    }

    /**
     * Create a new instance of {@link TxDrivenModuleConfiguration} with different initialize-until setting.
     *
     * @param initializeUntil of the new instance.
     * @return new instance.
     */
    public T withInitializeUntil(long initializeUntil) {
        return newInstance(inclusionPolicies, initializeUntil);
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
