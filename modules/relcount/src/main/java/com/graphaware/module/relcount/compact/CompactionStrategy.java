package com.graphaware.module.relcount.compact;

import com.graphaware.module.relcount.cache.DegreeCachingNode;

/**
 * Strategy for compacting cached degrees.
 * <p/>
 * For example, a node might have the following cached degrees:
 * - FRIEND_OF, OUTGOING, timestamp = 5.4.2013 : 1x
 * - FRIEND_OF, OUTGOING, timestamp = 6.4.2013 : 3x
 * - FRIEND_OF, OUTGOING, timestamp = 7.4.2013 : 5x
 * <p/>
 * The strategy might decide to compact this into:
 * - FRIEND_OF, OUTGOING, timestamp=anything - 9x
 */
public interface CompactionStrategy {

    /**
     * Compact cached degrees if needed.
     *
     * @param node to compact cached degrees for.
     */
    void compactRelationshipCounts(DegreeCachingNode node);
}
