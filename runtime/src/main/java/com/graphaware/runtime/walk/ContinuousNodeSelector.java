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

package com.graphaware.runtime.walk;

import com.graphaware.common.policy.inclusion.NodeInclusionPolicy;
import com.graphaware.runtime.policy.all.IncludeAllBusinessNodes;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.NotFoundException;
import org.neo4j.kernel.impl.storageengine.impl.recordstorage.RecordStorageEngine;
import org.neo4j.kernel.internal.GraphDatabaseAPI;
import org.neo4j.logging.Log;
import com.graphaware.common.log.LoggerFactory;

import java.util.concurrent.atomic.AtomicLong;

/**
 * {@link com.graphaware.runtime.walk.NodeSelector} that selects a {@link org.neo4j.graphdb.Node} by finding the first
 * node matching the provided {@link NodeInclusionPolicy} with ID higher than the last one. In the beginning or when all
 * IDs are exhausted, the selector starts from 0.
 */
public class ContinuousNodeSelector implements NodeSelector {

    private static final Log LOG = LoggerFactory.getLogger(ContinuousNodeSelector.class);

    private final NodeInclusionPolicy inclusionPolicy;
    private final AtomicLong lastId;

    /**
     * Constructs a new {@link com.graphaware.runtime.walk.ContinuousNodeSelector} that selects any node which isn't a
     * framework-internal node.
     */
    public ContinuousNodeSelector() {
        this(IncludeAllBusinessNodes.getInstance());
    }

    /**
     * Constructs a new {@link com.graphaware.runtime.walk.ContinuousNodeSelector} that selects a node that matches
     * the given {@link NodeInclusionPolicy}.
     *
     * @param lastNodeId ID of the last node selected by this selector.
     */
    public ContinuousNodeSelector(long lastNodeId) {
        this(IncludeAllBusinessNodes.getInstance(), lastNodeId);
    }

    /**
     * Constructs a new {@link com.graphaware.runtime.walk.ContinuousNodeSelector} that selects a node that matches
     * the given {@link NodeInclusionPolicy}.
     *
     * @param inclusionPolicy The {@link NodeInclusionPolicy} to consider when selecting
     *                        nodes.
     */
    public ContinuousNodeSelector(NodeInclusionPolicy inclusionPolicy) {
        this(inclusionPolicy, -1);
    }

    /**
     * Constructs a new {@link com.graphaware.runtime.walk.ContinuousNodeSelector} that selects a node that matches
     * the given {@link NodeInclusionPolicy}.
     *
     * @param inclusionPolicy The {@link NodeInclusionPolicy} to consider when selecting
     *                        nodes.
     * @param lastNodeId      ID of the last node selected by this selector.
     */
    public ContinuousNodeSelector(NodeInclusionPolicy inclusionPolicy, long lastNodeId) {
        this.inclusionPolicy = inclusionPolicy;
        this.lastId = new AtomicLong(lastNodeId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Node selectNode(GraphDatabaseService database) {
        int attempt = 0;
        while (true) {
            try {
                if (++attempt == 1000) {
                    LOG.warn("Did not find a suitable node in 1000 attempts. Are you sure the node inclusion policy is set correctly for ContinuousNodeSelector?");
                }
                if (attempt == 1_000_000) {
                    LOG.warn("Did not find a suitable node in 1M attempts. Aborting...");
                    return null;
                }
                Node node = database.getNodeById(nextId(database));
                if (inclusionPolicy.include(node)) {
                    return node;
                }
            } catch (NotFoundException e) {
                //ok
            }
        }
    }

    private long nextId(GraphDatabaseService database) {
        long highestId = ((GraphDatabaseAPI) database).getDependencyResolver().resolveDependency(RecordStorageEngine.class).testAccessNeoStores().getNodeStore().getHighestPossibleIdInUse();
        long nextId = lastId.incrementAndGet();

        if (nextId > highestId) {
            lastId.set(-1);
            nextId = lastId.incrementAndGet();
        }
        return nextId;
    }
}
