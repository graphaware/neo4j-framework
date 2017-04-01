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

import com.graphaware.common.policy.inclusion.NodeInclusionPolicy;
import com.graphaware.common.policy.inclusion.composite.CompositeNodeInclusionPolicy;
import com.graphaware.common.policy.inclusion.none.IncludeNoNodes;
import com.graphaware.common.policy.inclusion.spel.SpelNodeInclusionPolicy;
import com.graphaware.runtime.policy.all.IncludeAllBusinessNodes;

/**
 * A {@link StringToInclusionPolicy} that converts String to {@link NodeInclusionPolicy}. Singleton.
 */
public final class StringToNodeInclusionPolicy extends StringToInclusionPolicy<NodeInclusionPolicy> {

    private static StringToNodeInclusionPolicy INSTANCE = new StringToNodeInclusionPolicy();

    public static StringToNodeInclusionPolicy getInstance() {
        return INSTANCE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected NodeInclusionPolicy compositePolicy(NodeInclusionPolicy policy) {
        return CompositeNodeInclusionPolicy.of(IncludeAllBusinessNodes.getInstance(), policy);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected NodeInclusionPolicy spelPolicy(String spel) {
        return new SpelNodeInclusionPolicy(spel);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected NodeInclusionPolicy all() {
        return IncludeAllBusinessNodes.getInstance();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected NodeInclusionPolicy none() {
        return IncludeNoNodes.getInstance();
    }
}
