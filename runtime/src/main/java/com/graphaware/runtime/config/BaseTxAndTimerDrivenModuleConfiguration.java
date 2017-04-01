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

import com.graphaware.common.policy.inclusion.InclusionPolicies;
import com.graphaware.common.policy.role.InstanceRolePolicy;

/**
 * Base-class for {@link TimerDrivenModuleConfiguration} implementations.
 */
public abstract class BaseTxAndTimerDrivenModuleConfiguration<T extends BaseTxAndTimerDrivenModuleConfiguration<T>> extends BaseTxDrivenModuleConfiguration<T> implements TxAndTimerDrivenModuleConfiguration {

    private final InstanceRolePolicy instanceRolePolicy;

    /**
     * Construct a new configuration.
     *
     * @param inclusionPolicies  policies for inclusion of nodes, relationships, and properties for processing by the module. Must not be <code>null</code>.
     * @param initializeUntil    until what time in ms since epoch it is ok to re(initialize) the entire module in case the configuration
     *                           has changed since the last time the module was started, or if it is the first time the module was registered.
     *                           {@link #NEVER} for never, {@link #ALWAYS} for always.
     * @param instanceRolePolicy specifies which role a machine must have in order to run the module with this configuration. Must not be <code>null</code>.
     */
    public BaseTxAndTimerDrivenModuleConfiguration(InclusionPolicies inclusionPolicies, long initializeUntil, InstanceRolePolicy instanceRolePolicy) {
        super(inclusionPolicies, initializeUntil);
        this.instanceRolePolicy = instanceRolePolicy;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected T newInstance(InclusionPolicies inclusionPolicies, long initializeUntil) {
        return newInstance(inclusionPolicies, initializeUntil, getInstanceRolePolicy());
    }

    /**
     * Create a new instance of this {@link TimerDrivenModuleConfiguration} with different inclusion policies.
     *
     * @param inclusionPolicies  of the new instance.
     * @param initializeUntil    of the new instance.
     * @param instanceRolePolicy of the new instance.
     * @return new instance.
     */
    protected abstract T newInstance(InclusionPolicies inclusionPolicies, long initializeUntil, InstanceRolePolicy instanceRolePolicy);

    /**
     * Get instance role policy encapsulated by this configuration.
     *
     * @return policy.
     */
    public InstanceRolePolicy getInstanceRolePolicy() {
        return instanceRolePolicy;
    }

    /**
     * Create w new instance of {@link TimerDrivenModuleConfiguration} with different {@link InstanceRolePolicy}.
     *
     * @param instanceRolePolicy of the new instance.
     * @return new instance.
     */
    public T with(InstanceRolePolicy instanceRolePolicy) {
        return newInstance(getInclusionPolicies(), initializeUntil(), instanceRolePolicy);
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
        if (!super.equals(o)) {
            return false;
        }

        BaseTxAndTimerDrivenModuleConfiguration<?> that = (BaseTxAndTimerDrivenModuleConfiguration<?>) o;

        return instanceRolePolicy == that.instanceRolePolicy;

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + instanceRolePolicy.hashCode();
        return result;
    }
}
