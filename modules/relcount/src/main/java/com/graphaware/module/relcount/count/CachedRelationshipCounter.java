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

package com.graphaware.module.relcount.count;

import com.graphaware.common.description.relationship.DetachedRelationshipDescription;
import com.graphaware.common.description.relationship.RelationshipDescription;
import com.graphaware.common.description.serialize.Serializer;
import com.graphaware.module.relcount.RelationshipCountConfiguration;
import com.graphaware.module.relcount.RelationshipCountConfigurationImpl;
import com.graphaware.module.relcount.RelationshipCountRuntimeModule;
import com.graphaware.module.relcount.cache.DegreeCachingNode;
import com.graphaware.runtime.ProductionGraphAwareRuntime;
import com.graphaware.runtime.config.DefaultRuntimeConfiguration;
import com.graphaware.runtime.config.RuntimeConfiguration;
import org.apache.log4j.Logger;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.tooling.GlobalGraphOperations;

import static com.graphaware.runtime.ProductionGraphAwareRuntime.*;

/**
 * {@link RelationshipCounter} that counts matching relationships by looking them up cached in {@link org.neo4j.graphdb.Node}'s properties.
 * <p/>
 * It must be used in conjunction with {@link com.graphaware.module.relcount.RelationshipCountRuntimeModule}
 * registered with {@link com.graphaware.runtime.ProductionGraphAwareRuntime}. The easiest and recommended way to create
 * and instance of this counter is by calling {@link com.graphaware.module.relcount.RelationshipCountRuntimeModule#cachedCounter()}.
 * <p/>
 * This counter throws {@link UnableToCountException} if it detects it can not
 * reliably answer the question. This means compaction has taken place and this counter can't serve a request for
 * relationship count this specific. If you still want to count the relationship, either use {@link NaiveRelationshipCounter}
 * or consider increasing the compaction threshold.
 *
 * @see com.graphaware.module.relcount.compact.CompactionStrategy
 */
public class CachedRelationshipCounter implements RelationshipCounter {

    private static final Logger LOG = Logger.getLogger(CachedRelationshipCounter.class);

    private final String id;
    private final RuntimeConfiguration config;
    private final RelationshipCountConfiguration relationshipCountConfiguration;

    /**
     * Construct a new relationship counter. Use this constructor when {@link com.graphaware.runtime.ProductionGraphAwareRuntime}
     * is used with default configuration and only a single instance of {@link com.graphaware.module.relcount.RelationshipCountRuntimeModule}
     * is registered. This will be the case for most use cases.
     */
    public CachedRelationshipCounter(GraphDatabaseService database) {
        this(database, RelationshipCountRuntimeModule.FULL_RELCOUNT_DEFAULT_ID);
    }

    /**
     * Construct a new relationship counter. Use this constructor when multiple instances of {@link com.graphaware.module.relcount.RelationshipCountRuntimeModule}
     * have been registered with the {@link com.graphaware.runtime.ProductionGraphAwareRuntime}, when the
     * {@link com.graphaware.runtime.ProductionGraphAwareRuntime} is used with custom configuration, or when custom {@link com.graphaware.module.relcount.RelationshipCountConfiguration} are used.
     * This should rarely be the case. Alternatively, use {@link com.graphaware.module.relcount.RelationshipCountRuntimeModule#cachedCounter()}.
     *
     * @param id                             of the {@link com.graphaware.module.relcount.RelationshipCountRuntimeModule} used to cache relationship counts.
     * @param config                         used with the {@link com.graphaware.runtime.ProductionGraphAwareRuntime}.
     * @param relationshipCountConfiguration strategies used for relationship counting.
     */
    public CachedRelationshipCounter(GraphDatabaseService database, String id) {
        this.id = id;
        this.config = DefaultRuntimeConfiguration.getInstance();

        try (Transaction tx = database.beginTx()) {
            if (!GlobalGraphOperations.at(database).getAllNodesWithLabel(RuntimeConfiguration.GA_ROOT).iterator().hasNext()) {
                throw new IllegalStateException("Could not find GraphAware Runtime Root Node - is GraphAware Runtime enabled?");
            }

            Node root = getOrCreateRoot(database);
            String key = config.createPrefix(RUNTIME) + id;
            if (!root.hasProperty(key)) {
                throw new IllegalStateException("Could not find Relationship Count Module configuration - has the module been registered with the Runtime?");
            }
            this.relationshipCountConfiguration = Serializer.fromString(root.getProperty(key).toString(), RelationshipCountConfigurationImpl.class, CONFIG);
            tx.success();
        } catch (RuntimeException e) {
            LOG.error("Could not construct cached relationship counter because its configuration could not be read from the GraphAware Runtime Root Node", e);
            throw e;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int count(Node node, RelationshipDescription description) {
        int result = 0;

        DegreeCachingNode cachingNode = new DegreeCachingNode(node, config.createPrefix(id), relationshipCountConfiguration);

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
