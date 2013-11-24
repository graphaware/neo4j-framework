package com.graphaware.relcount.module;


import com.graphaware.common.strategy.InclusionStrategies;
import com.graphaware.relcount.cache.DegreeCachingStrategy;
import com.graphaware.relcount.compact.CompactionStrategy;
import com.graphaware.relcount.count.WeighingStrategy;

/**
 * Container for strategies and configuration related to relationship counting.
 */
public interface RelationshipCountStrategies extends InclusionStrategies {

    /**
     * @return contained caching strategy.
     */
    DegreeCachingStrategy getDegreeCachingStrategy();

    /**
     * @return contained compaction strategy.
     */
    CompactionStrategy getCompactionStrategy();

    /**
     * @return contained relationship weighing strategy.
     */
    WeighingStrategy getWeighingStrategy();
}
