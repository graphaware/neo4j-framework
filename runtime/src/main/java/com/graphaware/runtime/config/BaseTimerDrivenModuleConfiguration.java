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

import com.graphaware.common.policy.role.InstanceRolePolicy;

import static org.springframework.util.Assert.notNull;

/**
 * Base-class for {@link TimerDrivenModuleConfiguration} implementations.
 */
public abstract class BaseTimerDrivenModuleConfiguration<T extends BaseTimerDrivenModuleConfiguration<T>> implements TimerDrivenModuleConfiguration {

    private final InstanceRolePolicy instanceRolePolicy;

    /**
     * Construct a new configuration.
     *
     * @param instanceRolePolicy specifies which role a machine must have in order to run the module with this configuration. Must not be <code>null</code>.
     */
    protected BaseTimerDrivenModuleConfiguration(InstanceRolePolicy instanceRolePolicy) {
        notNull(instanceRolePolicy);
        this.instanceRolePolicy = instanceRolePolicy;
    }

    /**
     * Create a new instance of this {@link TimerDrivenModuleConfiguration} with different inclusion policies.
     *
     * @param instanceRolePolicy of the new instance.
     * @return new instance.
     */
    protected abstract T newInstance(InstanceRolePolicy instanceRolePolicy);

    /**
     * Get instance role policy encapsulated by this configuration.
     *
     * @return policy.
     */
    public InstanceRolePolicy getInstanceRolePolicy() {
        return instanceRolePolicy;
    }

    /**
     * Create w new instance of {@link TimerDrivenModuleConfiguration} with different {@link com.graphaware.runtime.config.TimerDrivenModuleConfiguration.InstanceRolePolicy}.
     *
     * @param instanceRolePolicy of the new instance.
     * @return new instance.
     */
    public T with(InstanceRolePolicy instanceRolePolicy) {
        return newInstance(instanceRolePolicy);
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

        BaseTimerDrivenModuleConfiguration that = (BaseTimerDrivenModuleConfiguration) o;

        if (!instanceRolePolicy.equals(that.instanceRolePolicy)) {
            return false;
        }

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return instanceRolePolicy.hashCode();
    }
}
