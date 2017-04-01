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

package com.graphaware.runtime.config.function;

import com.graphaware.common.policy.inclusion.NodePropertyInclusionPolicy;
import com.graphaware.common.policy.inclusion.all.IncludeAllNodeProperties;
import com.graphaware.common.policy.inclusion.composite.CompositeNodePropertyInclusionPolicy;
import com.graphaware.common.policy.inclusion.none.IncludeNoNodeProperties;
import com.graphaware.common.policy.inclusion.spel.SpelNodePropertyInclusionPolicy;
import com.graphaware.runtime.policy.all.IncludeAllBusinessNodeProperties;

/**
 * A {@link StringToInclusionPolicy} that converts String to {@link NodePropertyInclusionPolicy}. Singleton.
 */
public final class StringToNodePropertyInclusionPolicy extends StringToInclusionPolicy<NodePropertyInclusionPolicy> {

    private static StringToNodePropertyInclusionPolicy INSTANCE = new StringToNodePropertyInclusionPolicy();

    public static StringToNodePropertyInclusionPolicy getInstance() {
        return INSTANCE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected NodePropertyInclusionPolicy compositePolicy(NodePropertyInclusionPolicy policy) {
        return CompositeNodePropertyInclusionPolicy.of(IncludeAllBusinessNodeProperties.getInstance(), policy);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected NodePropertyInclusionPolicy spelPolicy(String spel) {
        return new SpelNodePropertyInclusionPolicy(spel);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected NodePropertyInclusionPolicy all() {
        return IncludeAllNodeProperties.getInstance();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected NodePropertyInclusionPolicy none() {
        return IncludeNoNodeProperties.getInstance();
    }
}
