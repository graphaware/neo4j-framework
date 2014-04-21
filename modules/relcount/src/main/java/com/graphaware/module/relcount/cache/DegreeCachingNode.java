package com.graphaware.module.relcount.cache;

import com.graphaware.common.description.relationship.DetachedRelationshipDescription;
import com.graphaware.module.relcount.RelationshipCountConfiguration;
import com.graphaware.runtime.NeedsInitializationException;
import org.apache.log4j.Logger;
import org.neo4j.graphdb.Node;

import java.util.*;

/**
 * Node representation that caches its own degrees and compacts them using a {@link com.graphaware.module.relcount.compact.CompactionStrategy}.
 */
public class DegreeCachingNode {

    private static final Logger LOG = Logger.getLogger(DegreeCachingNode.class);

    private final Node node;
    private final String prefix;
    private final RelationshipCountConfiguration configuration;

    private final Map<DetachedRelationshipDescription, Integer> cachedDegrees = new HashMap<>();
    private final Set<DetachedRelationshipDescription> updatedDegrees = new HashSet<>();
    private final Set<DetachedRelationshipDescription> removedDegrees = new HashSet<>();

    /**
     * Construct a new caching node.
     *
     * @param node       represented Neo4j node.
     * @param prefix     of the metadata written to the graph.
     * @param configuration for caching degrees.
     */
    public DegreeCachingNode(Node node, String prefix, RelationshipCountConfiguration configuration) {
        this.node = node;
        this.prefix = prefix;
        this.configuration = configuration;

        cachedDegrees.putAll(configuration.getDegreeCachingStrategy().readDegrees(node, prefix));
    }

    /**
     * ID of the represented Neo4j {@link org.neo4j.graphdb.Node}.
     *
     * @return ID.
     */
    public long getId() {
        return node.getId();
    }

    /**
     * Get all degrees cached by the node.
     *
     * @return cached degrees (key = relationship description, value = count).
     */
    public Map<DetachedRelationshipDescription, Integer> getCachedDegrees() {
        return Collections.unmodifiableMap(cachedDegrees);
    }

    /**
     * Increment the degree of this node with respect to a relationship description by a delta.
     *
     * @param description of a relationship.
     * @param delta       by how many to increment.
     */
    public void incrementDegree(DetachedRelationshipDescription description, int delta) {
        incrementDegree(description, delta, false);
    }

    /**
     * Increment the degree of this node with respect to a relationship description by a delta.
     *
     * @param description       of a relationship.
     * @param delta             by how many to increment.
     * @param preventCompaction true for preventing compaction.
     */
    public void incrementDegree(DetachedRelationshipDescription description, int delta, boolean preventCompaction) {
        for (DetachedRelationshipDescription cachedDescription : cachedDegrees.keySet()) {
            if (cachedDescription.isMoreGeneralThan(description)) {
                int newValue = cachedDegrees.get(cachedDescription) + delta;
                put(cachedDescription, newValue);
                return;
            }
        }

        put(description, delta);

        if (!preventCompaction) {
            configuration.getCompactionStrategy().compactRelationshipCounts(this);
        }
    }

    /**
     * Decrement the degree of this node with respect to a relationship description by a delta.
     *
     * @param description of a relationship.
     * @param delta       by how many to decrement.
     * @throws com.graphaware.runtime.NeedsInitializationException
     *          if the total degree of this node with respect to the given relationships becomes negative.
     */
    public void decrementDegree(DetachedRelationshipDescription description, int delta) {
        for (DetachedRelationshipDescription cachedDescription : cachedDegrees.keySet()) {
            if (cachedDescription.isMoreGeneralThan(description)) {
                int newValue = cachedDegrees.get(cachedDescription) - delta;
                put(cachedDescription, newValue);

                if (newValue <= 0) {
                    delete(cachedDescription);
                }

                if (newValue < 0) {
                    LOG.warn(cachedDescription.toString() + " was out of sync on node " + node.getId());
                    throw new NeedsInitializationException(cachedDescription.toString() + " was out of sync on node " + node.getId());
                }

                return;
            }
        }

        LOG.warn(description.toString() + " was not present on node " + node.getId());
        throw new NeedsInitializationException(description.toString() + " was not present on node " + node.getId());
    }

    /**
     * Apply all the changes to cached degrees to persistent storage.
     */
    public void flush() {
        configuration.getDegreeCachingStrategy().writeDegrees(node, prefix, cachedDegrees, updatedDegrees, removedDegrees);
    }

    /**
     * Update the cached degree with respect to a relationship description.
     *
     * @param description to update.
     * @param value       new value.
     */
    private void put(DetachedRelationshipDescription description, int value) {
        cachedDegrees.put(description, value);
        updatedDegrees.add(description);
        removedDegrees.remove(description);
    }

    /**
     * Delete a cached degree with respect to a relationship description.
     *
     * @param description to delete.
     */
    private void delete(DetachedRelationshipDescription description) {
        cachedDegrees.remove(description);
        updatedDegrees.remove(description);
        removedDegrees.add(description);
    }
}
