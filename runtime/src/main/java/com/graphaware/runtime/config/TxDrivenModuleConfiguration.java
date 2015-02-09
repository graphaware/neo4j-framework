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

import com.graphaware.common.policy.InclusionPolicies;

/**
 * Encapsulates all configuration of a single {@link com.graphaware.runtime.module.TxDrivenModule}. Modules that need
 * no configuration should use {@link NullTxDrivenModuleConfiguration}. Otherwise, start with
 * {@link FluentTxDrivenModuleConfiguration}.
 */
public interface TxDrivenModuleConfiguration {

    /**
     * Get the inclusion policies used by this module. If unsure, return {@link com.graphaware.runtime.policy.InclusionPoliciesFactory#allBusiness()}.
     *
     * @return policies.
     */
    InclusionPolicies getInclusionPolicies();
}
