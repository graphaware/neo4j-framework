/*
 * Copyright (c) 2013 GraphAware
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

package com.graphaware.module.relcount.cache;

import com.graphaware.runtime.config.DefaultFrameworkConfiguration;
import com.graphaware.module.relcount.count.CachedRelationshipCounter;
import com.graphaware.module.relcount.count.RelationshipCounter;
import com.graphaware.module.relcount.RelationshipCountStrategiesImpl;

import static com.graphaware.module.relcount.RelationshipCountRuntimeModule.FULL_RELCOUNT_DEFAULT_ID;

/**
 * Integration test for {@link com.graphaware.module.relcount.cache.DegreeCachingNode} with {@link SingleNodePropertyDegreeCachingStrategy}.
 */
public class SingleNodePropertyDegreeCachingNodeIntegrationTest extends DegreeCachingNodeIntegrationTest {

    @Override
    protected DegreeCachingNode cachingNode() {
        return new DegreeCachingNode(
                database.getNodeById(0),
                DefaultFrameworkConfiguration.getInstance().createPrefix(FULL_RELCOUNT_DEFAULT_ID),
                RelationshipCountStrategiesImpl.defaultStrategies().with(new SingleNodePropertyDegreeCachingStrategy()));
    }

    @Override
    protected RelationshipCounter counter() {
        return new CachedRelationshipCounter(
                FULL_RELCOUNT_DEFAULT_ID,
                DefaultFrameworkConfiguration.getInstance(),
                RelationshipCountStrategiesImpl.defaultStrategies().with(new SingleNodePropertyDegreeCachingStrategy()));
    }
}
