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

import com.graphaware.common.description.relationship.DetachedRelationshipDescription;
import com.graphaware.common.description.relationship.RelationshipDescription;
import com.graphaware.runtime.config.DefaultFrameworkConfiguration;
import com.graphaware.runtime.config.FrameworkConfiguration;
import com.graphaware.relcount.cache.DegreeCachingNode;
import com.graphaware.relcount.module.RelationshipCountRuntimeModule;
import com.graphaware.relcount.module.RelationshipCountStrategies;
import com.graphaware.relcount.module.RelationshipCountStrategiesImpl;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;

/**
 * {@link RelationshipCounter} that counts matching relationships by looking them up cached in {@link org.neo4j.graphdb.Node}'s properties.
 * <p/>
 * It must be used in conjunction with {@link com.graphaware.relcount.module.RelationshipCountRuntimeModule}
 * registered with {@link com.graphaware.runtime.GraphAwareRuntime}. The easiest and recommended way to create
 * and instance of this counter is by calling {@link com.graphaware.relcount.module.RelationshipCountRuntimeModule#cachedCounter()}.
 * <p/>
 * This counter throws {@link UnableToCountException} if it detects it can not
 * reliably answer the question. This means compaction has taken place and this counter can't serve a request for
 * relationship count this specific. If you still want to count the relationship, either use {@link NaiveRelationshipCounter}
 * or consider increasing the compaction threshold.
 *
 * @see com.graphaware.relcount.compact.CompactionStrategy
 */
public class CachedRelationshipCounter implements RelationshipCounter {

    private final String id;
    private final FrameworkConfiguration config;
    private final RelationshipCountStrategies relationshipCountStrategies;

    /**
     * Construct a new relationship counter. Use this constructor when {@link com.graphaware.runtime.GraphAwareRuntime}
     * is used with default configuration and only a single instance of {@link com.graphaware.relcount.module.RelationshipCountRuntimeModule}
     * is registered. This will be the case for most use cases.
     */
    public CachedRelationshipCounter() {
        this(RelationshipCountRuntimeModule.FULL_RELCOUNT_DEFAULT_ID, DefaultFrameworkConfiguration.getInstance(), RelationshipCountStrategiesImpl.defaultStrategies());
    }

    /**
     * Construct a new relationship counter. Use this constructor when multiple instances of {@link com.graphaware.relcount.module.RelationshipCountRuntimeModule}
     * have been registered with the {@link com.graphaware.runtime.GraphAwareRuntime}, when the
     * {@link com.graphaware.runtime.GraphAwareRuntime} is used with custom configuration, or when custom {@link RelationshipCountStrategies} are used.
     * This should rarely be the case. Alternatively, use {@link com.graphaware.relcount.module.RelationshipCountRuntimeModule#cachedCounter()}.
     *
     * @param id                          of the {@link com.graphaware.relcount.module.RelationshipCountRuntimeModule} used to cache relationship counts.
     * @param config                      used with the {@link com.graphaware.runtime.GraphAwareRuntime}.
     * @param relationshipCountStrategies strategies used for relationship counting.
     */
    public CachedRelationshipCounter(String id, FrameworkConfiguration config, RelationshipCountStrategies relationshipCountStrategies) {
        this.id = id;
        this.config = config;
        this.relationshipCountStrategies = relationshipCountStrategies;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int count(Node node, RelationshipDescription description) {
        int result = 0;

        DegreeCachingNode cachingNode = new DegreeCachingNode(node, config.createPrefix(id), relationshipCountStrategies);

        for (DetachedRelationshipDescription candidate : cachingNode.getCachedDegrees().keySet()) {

            boolean matches = candidate.isMoreSpecificThan(description);

            if (!matches && !candidate.isMutuallyExclusive(description)) {
                throw new UnableToCountException("Unable to count relationships with the following description: "
                        + description.toString()
                        + " Since there are potentially compacted out cached matches," +
                        " it looks like compaction has taken away the granularity you need. Please try to count this kind " +
                        "of relationship with a naive counter. Alternatively, increase the compaction threshold.");
            }

            if (matches) {
                result += cachingNode.getCachedDegrees().get(candidate);
            }
        }

        return result;
    }
}
