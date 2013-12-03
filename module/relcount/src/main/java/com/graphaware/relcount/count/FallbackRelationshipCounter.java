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

package com.graphaware.relcount.count;

import com.graphaware.common.description.relationship.RelationshipDescription;
import com.graphaware.framework.config.DefaultFrameworkConfiguration;
import com.graphaware.framework.config.FrameworkConfiguration;
import com.graphaware.relcount.module.RelationshipCountRuntimeModule;
import com.graphaware.relcount.module.RelationshipCountStrategies;
import com.graphaware.relcount.module.RelationshipCountStrategiesImpl;
import org.apache.log4j.Logger;
import org.neo4j.graphdb.Node;

/**
 * {@link RelationshipCounter} that counts matching relationships by first trying to use {@link CachedRelationshipCounter}
 * and if that fails (i.e., throws a {@link UnableToCountException}), resorts to {@link NaiveRelationshipCounter}.
 * <p/>
 * It should be used in conjunction with {@link com.graphaware.relcount.module.RelationshipCountRuntimeModule}
 * registered with {@link com.graphaware.framework.GraphAwareRuntime}. The easiest and recommended way to create
 * and instance of this counter is by calling {@link com.graphaware.relcount.module.RelationshipCountRuntimeModule#fallbackCounter()}.
 * <p/>
 * This counter always returns a count, never throws {@link UnableToCountException}.
 * <p/>
 * About fallback: Fallback to naive approach only happens if it is detected that compaction has taken place
 * (see {@link com.graphaware.relcount.cache.NodeBasedDegreeCache}) and the needed granularity has
 * been compacted out. There is a performance penalty to this fallback.
 * To avoid it, make sure the compaction threshold is set correctly. No fallback happens when a {@link com.graphaware.tx.event.improved.strategy.RelationshipInclusionStrategy} has been used that explicitly excludes
 * the relationships being counted (0 is returned). If you prefer an exception to fallback, use {@link CachedRelationshipCounter}.
 */
public class FallbackRelationshipCounter implements RelationshipCounter {

    private static final Logger LOG = Logger.getLogger(FallbackRelationshipCounter.class);

    private final String id;
    private final FrameworkConfiguration config;
    private final RelationshipCountStrategies strategies;

    /**
     * Construct a new relationship counter with default settings. Use this constructor when
     * {@link com.graphaware.framework.GraphAwareRuntime} is used with default configuration, only a single
     * instance of {@link com.graphaware.relcount.module.RelationshipCountRuntimeModule} is registered, and
     * no custom {@link com.graphaware.relcount.module.RelationshipCountStrategies} are in use. If unsure, it is always easy and correct to instantiate
     * this counter through {@link com.graphaware.relcount.module.RelationshipCountRuntimeModule#fallbackCounter()} .
     */
    public FallbackRelationshipCounter() {
        this(RelationshipCountRuntimeModule.FULL_RELCOUNT_DEFAULT_ID, DefaultFrameworkConfiguration.getInstance(), RelationshipCountStrategiesImpl.defaultStrategies());
    }

    /**
     * Construct a new relationship counter with granular settings. It is always easier and recommended to use
     * {@link com.graphaware.relcount.module.RelationshipCountRuntimeModule#fallbackCounter()} instead.
     *
     * @param id         of the {@link com.graphaware.relcount.module.RelationshipCountRuntimeModule} used to cache relationship counts.
     * @param config     used with the {@link com.graphaware.framework.GraphAwareRuntime}.
     * @param strategies for counting relationships, provided to the {@link com.graphaware.relcount.module.RelationshipCountRuntimeModule}.
     */
    public FallbackRelationshipCounter(String id, FrameworkConfiguration config, RelationshipCountStrategies strategies) {
        this.id = id;
        this.strategies = strategies;
        this.config = config;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int count(Node node, RelationshipDescription description) {
        try {
            return new CachedRelationshipCounter(id, config, strategies).count(node, description);
        } catch (UnableToCountException e) {
            LOG.warn("Unable to count relationships with description: " + description.toString() +
                    " for node " + node.toString() + ". Falling back to naive approach");
            return new NaiveRelationshipCounter(strategies).count(node, description);
        }
    }
}
