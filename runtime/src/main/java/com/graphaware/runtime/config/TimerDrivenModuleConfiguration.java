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

/**
 * Encapsulates all configuration of a single {@link com.graphaware.runtime.module.TimerDrivenModule}. Modules that need
 * no configuration should use {@link NullTimerDrivenModuleConfiguration}. Otherwise, start with
 * {@link FluentTimerDrivenModuleConfiguration}.
 */
public interface TimerDrivenModuleConfiguration {

    /**
     * Get the instance role policy used by this module. If unsure, return {@link com.graphaware.runtime.config.TimerDrivenModuleConfiguration.InstanceRolePolicy#MASTER_ONLY}.
     *
     * @return policy.
     */
    InstanceRolePolicy getInstanceRolePolicy();
}
