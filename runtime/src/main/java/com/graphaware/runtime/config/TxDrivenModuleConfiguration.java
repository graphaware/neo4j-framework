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

    long NEVER = 0;
    long ALWAYS = Long.MAX_VALUE;

    /**
     * @return until what time in ms since epoch it is ok to re(initialize) the entire module in case the configuration
     * has changed since the last time the module was started, or if it is the first time the module was registered.
     * {@link #NEVER} for never, {@link #ALWAYS} for always.
     */
    long initializeUntil();
}
