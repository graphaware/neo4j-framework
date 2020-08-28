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

package com.graphaware.runtime.config;

import com.graphaware.common.policy.inclusion.InclusionPolicies;
import com.graphaware.runtime.module.RuntimeModule;
import com.graphaware.runtime.policy.InclusionPoliciesFactory;

/**
 * {@link RuntimeModuleConfiguration} for {@link RuntimeModule}s with no configuration. Singleton.
 */
public final class NullRuntimeModuleConfiguration implements RuntimeModuleConfiguration {

    private static final RuntimeModuleConfiguration INSTANCE = new NullRuntimeModuleConfiguration();
    private final InclusionPolicies inclusionPolicies;

    /**
     * Get instance of this singleton configuration.
     *
     * @return instance.
     */
    public static RuntimeModuleConfiguration getInstance() {
        return INSTANCE;
    }

    private NullRuntimeModuleConfiguration() {
        inclusionPolicies = InclusionPoliciesFactory.allBusiness();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InclusionPolicies getInclusionPolicies() {
        return inclusionPolicies;
    }
}
