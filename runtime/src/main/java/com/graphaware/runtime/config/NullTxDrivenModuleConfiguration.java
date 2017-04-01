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
import com.graphaware.common.serialize.Serializer;
import com.graphaware.common.serialize.SingletonSerializer;
import com.graphaware.runtime.policy.InclusionPoliciesFactory;

/**
 * {@link TxDrivenModuleConfiguration} for {@link com.graphaware.runtime.module.TxDrivenModule}s with no configuration. Singleton.
 */
public final class NullTxDrivenModuleConfiguration implements TxDrivenModuleConfiguration {

    static {
        Serializer.register(NullTxDrivenModuleConfiguration.class, new SingletonSerializer(), 1000);
    }

    private static final TxDrivenModuleConfiguration INSTANCE = new NullTxDrivenModuleConfiguration();
    private final InclusionPolicies inclusionPolicies;
    private final long initializeUntil;

    /**
     * Get instance of this singleton configuration.
     *
     * @return instance.
     */
    public static TxDrivenModuleConfiguration getInstance() {
        return INSTANCE;
    }

    private NullTxDrivenModuleConfiguration() {
        inclusionPolicies = InclusionPoliciesFactory.allBusiness();
        initializeUntil = ALWAYS;
    }

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
}
