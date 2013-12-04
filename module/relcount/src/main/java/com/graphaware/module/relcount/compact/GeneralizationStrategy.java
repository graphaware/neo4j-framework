package com.graphaware.module.relcount.compact;

import com.graphaware.common.description.relationship.DetachedRelationshipDescription;

import java.util.Map;

/**
 * A strategy for producing generalizations of cached degrees.
 */
public interface GeneralizationStrategy {

    /**
     * Produce the best generalizations of the cached degrees that will result in compaction. Implementations should
     * determine what "best" means.
     *
     * @param cachedDegrees cached degrees that need to be compacted.
     * @return best generalization.
     */
    DetachedRelationshipDescription produceGeneralization(Map<DetachedRelationshipDescription, Integer> cachedDegrees);
}
