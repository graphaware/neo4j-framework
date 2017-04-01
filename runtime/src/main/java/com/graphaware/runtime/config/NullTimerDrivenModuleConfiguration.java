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
import com.graphaware.common.serialize.Serializer;
import com.graphaware.common.serialize.SingletonSerializer;

/**
 * {@link TimerDrivenModuleConfiguration} for {@link com.graphaware.runtime.module.TimerDrivenModule}s with no configuration. Singleton.
 * <p/>
 * Implies the {@link com.graphaware.runtime.module.TimerDrivenModule} will only run on Master or Leader nodes.
 */
public final class NullTimerDrivenModuleConfiguration implements TimerDrivenModuleConfiguration {

    static {
        Serializer.register(NullTimerDrivenModuleConfiguration.class, new SingletonSerializer(), 1010);
    }

    private static final TimerDrivenModuleConfiguration INSTANCE = new NullTimerDrivenModuleConfiguration();

    /**
     * Get instance of this singleton configuration.
     *
     * @return instance.
     */
    public static TimerDrivenModuleConfiguration getInstance() {
        return INSTANCE;
    }

    private NullTimerDrivenModuleConfiguration() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InstanceRolePolicy getInstanceRolePolicy() {
        return WritableRole.getInstance();
    }
}
