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
import com.graphaware.common.policy.role.WritableRole;

/**
 * {@link TxDrivenModuleConfiguration} with fluent interface.
 * Intended for users of Neo4j in embedded mode for programmatic configuration.
 */
public final class FluentTimerDrivenModuleConfiguration extends BaseTimerDrivenModuleConfiguration<FluentTimerDrivenModuleConfiguration> {

    /**
     * Creates an instance with default values, i.e., with {@link WritableRole}.
     *
     * @return The {@link FluentTimerDrivenModuleConfiguration} instance.
     */
    public static FluentTimerDrivenModuleConfiguration defaultConfiguration() {
        return new FluentTimerDrivenModuleConfiguration();
    }

    /**
     * Create a new configuration with {@link WritableRole}.
     */
    private FluentTimerDrivenModuleConfiguration() {
        super(WritableRole.getInstance());
    }

    /**
     * Create a new configuration.
     *
     * @param instanceRolePolicy of the configuration.
     */
    private FluentTimerDrivenModuleConfiguration(InstanceRolePolicy instanceRolePolicy) {
        super(instanceRolePolicy);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected FluentTimerDrivenModuleConfiguration newInstance(InstanceRolePolicy instanceRolePolicy) {
        return new FluentTimerDrivenModuleConfiguration(instanceRolePolicy);
    }
}
