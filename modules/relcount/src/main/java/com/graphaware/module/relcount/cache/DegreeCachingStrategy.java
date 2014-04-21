package com.graphaware.module.relcount.cache;

import com.graphaware.common.description.relationship.DetachedRelationshipDescription;
import org.neo4j.graphdb.Node;

import java.util.Map;
import java.util.Set;

/**
 * A strategy deciding where and how to cache node degrees.
 */
public interface DegreeCachingStrategy {

    /**
     * Write node degrees to the database (or another persistent store).
     *
     * @param node           for which the degrees are being cached.
     * @param prefix         for metadata written.
     * @param cachedDegrees  the "full picture" - all cached degrees of the node.
     * @param updatedDegrees updated degrees only.
     * @param removedDegrees removed degrees only.
     */
    void writeDegrees(Node node,
                      String prefix,
                      Map<DetachedRelationshipDescription, Integer> cachedDegrees,
                      Set<DetachedRelationshipDescription> updatedDegrees,
                      Set<DetachedRelationshipDescription> removedDegrees);

    /**
     * Read the cached degrees for a node.
     *
     * @param node   to read cached degrees for.
     * @param prefix for metadata read.
     * @return cached degrees.
     */
    Map<DetachedRelationshipDescription, Integer> readDegrees(Node node, String prefix);
}
