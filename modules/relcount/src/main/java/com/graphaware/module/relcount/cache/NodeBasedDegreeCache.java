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

import com.graphaware.common.description.property.LiteralPropertiesDescription;
import com.graphaware.common.description.relationship.DetachedRelationshipDescription;
import com.graphaware.common.description.relationship.DetachedRelationshipDescriptionImpl;
import com.graphaware.common.wrapper.NodeWrapper;
import com.graphaware.module.relcount.RelationshipCountConfiguration;
import com.graphaware.runtime.config.BaseRuntimeConfigured;
import com.graphaware.runtime.config.RuntimeConfigured;
import org.apache.log4j.Logger;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import java.util.HashMap;
import java.util.Map;

import static com.graphaware.common.util.DirectionUtils.resolveDirection;

/**
 * {@link DegreeCache} that caches degrees using {@link DegreeCachingNode}s.
 * <p/>
 * It has its own "cache" of {@link DegreeCachingNode}s in order to optimize database reads/writes.
 */
public class NodeBasedDegreeCache extends BaseRuntimeConfigured implements DegreeCache, RuntimeConfigured {

    private static final Logger LOG = Logger.getLogger(NodeBasedDegreeCache.class);

    private static final ThreadLocal<Map<Long, DegreeCachingNode>> nodeCache = new ThreadLocal<>();

    private final String id;
    private final RelationshipCountConfiguration relationshipCountConfiguration;

    /**
     * Construct a new cache.
     *
     * @param id                          of the module this cache belongs to.
     * @param relationshipCountConfiguration strategies for degree caching.
     */
    public NodeBasedDegreeCache(String id, RelationshipCountConfiguration relationshipCountConfiguration) {
        this.id = id;
        this.relationshipCountConfiguration = relationshipCountConfiguration;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void startCaching() {
        if (nodeCache.get() != null) {
            throw new IllegalStateException("Previous caching hasn't been ended!");
        }

        nodeCache.set(new HashMap<Long, DegreeCachingNode>());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void endCaching() {
        ThreadLocal<Map<Long, DegreeCachingNode>> nodeCache = NodeBasedDegreeCache.nodeCache;

        if (nodeCache.get() == null) {
            throw new IllegalStateException("No caching has been started!");
        }

        try {
            for (DegreeCachingNode node : nodeCache.get().values()) {
                node.flush();
            }
        } finally {
            //no need to catch, exception will propagate and rollback transaction, but we must indicate end of caching
            nodeCache.set(null);
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleCreatedRelationship(Relationship relationship, Node pointOfView, Direction defaultDirection) {
        throwExceptionIfDirectionIsNullOrBoth(defaultDirection);

        DetachedRelationshipDescription createdRelationship = new DetachedRelationshipDescriptionImpl(
                relationship.getType(),
                resolveDirection(relationship, pointOfView, defaultDirection),
                new LiteralPropertiesDescription(relationship));

        int relationshipWeight = relationshipCountConfiguration.getWeighingStrategy().getRelationshipWeight(relationship, pointOfView);

        DegreeCachingNode cachingNode = cachingNode(unwrap(pointOfView));
        cachingNode.incrementDegree(createdRelationship, relationshipWeight);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleDeletedRelationship(Relationship relationship, Node pointOfView, Direction defaultDirection) {
        throwExceptionIfDirectionIsNullOrBoth(defaultDirection);

        DetachedRelationshipDescription deletedRelationship = new DetachedRelationshipDescriptionImpl(
                relationship.getType(),
                resolveDirection(relationship, pointOfView, defaultDirection),
                new LiteralPropertiesDescription(relationship));

        int relationshipWeight = relationshipCountConfiguration.getWeighingStrategy().getRelationshipWeight(relationship, pointOfView);

        DegreeCachingNode cachingNode = cachingNode(unwrap(pointOfView));
        cachingNode.decrementDegree(deletedRelationship, relationshipWeight);
    }

    /**
     * Get an instance of caching node from cache, or create a new one and put it in cache.
     *
     * @param node for which a caching node should be obtained.
     * @return caching node.
     */
    private DegreeCachingNode cachingNode(Node node) {
        Map<Long, DegreeCachingNode> nodeCache = NodeBasedDegreeCache.nodeCache.get();

        if (nodeCache == null) {
            throw new IllegalStateException("No caching has been started!");
        }

        if (!nodeCache.containsKey(node.getId())) {
            nodeCache.put(node.getId(), newDegreeCachingNode(node, getConfig().createPrefix(id), relationshipCountConfiguration));
        }

        return nodeCache.get(node.getId());
    }

    /**
     * Create a new instance of {@link DegreeCachingNode}, representing the given node.
     *
     * @param node to represent.
     * @return degree caching node.
     */
    protected DegreeCachingNode newDegreeCachingNode(Node node, String prefix, RelationshipCountConfiguration configuration) {
        return new DegreeCachingNode(node, prefix, configuration);
    }

    /**
     * Unwrap a potentially decorated Neo4j node.
     *
     * @param node to unwrap.
     * @return node with no decorators around it.
     */
    private Node unwrap(Node node) {
        if (node instanceof NodeWrapper) {
            return ((NodeWrapper) node).getWrapped();
        }

        LOG.warn("Unwrapping a non-wrapper node...");
        return node.getGraphDatabase().getNodeById(node.getId());
    }

    /**
     * Check that the given direction is not null or {@link org.neo4j.graphdb.Direction#BOTH} and throw an exception if it is.
     *
     * @param direction to check.
     * @throws IllegalArgumentException in case direction is null or {@link org.neo4j.graphdb.Direction#BOTH}.
     */
    private void throwExceptionIfDirectionIsNullOrBoth(Direction direction) {
        if (direction == null || direction.equals(Direction.BOTH)) {
            throw new IllegalArgumentException("Default direction must not be null or BOTH. This is a bug.");
        }
    }
}
