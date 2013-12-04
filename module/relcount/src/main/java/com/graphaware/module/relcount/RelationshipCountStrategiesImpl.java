package com.graphaware.module.relcount;


import com.graphaware.common.strategy.*;
import com.graphaware.runtime.strategy.IncludeAllBusinessRelationshipProperties;
import com.graphaware.runtime.strategy.IncludeAllBusinessRelationships;
import com.graphaware.module.relcount.cache.DegreeCachingStrategy;
import com.graphaware.module.relcount.cache.SingleNodePropertyDegreeCachingStrategy;
import com.graphaware.module.relcount.compact.CompactionStrategy;
import com.graphaware.module.relcount.compact.ThresholdBasedCompactionStrategy;
import com.graphaware.module.relcount.count.OneForEach;
import com.graphaware.module.relcount.count.WeighingStrategy;

/**
 * {@link RelationshipCountStrategies}, providing static factory method for a default configuration and "with"
 * methods for fluently overriding these with custom strategies.
 */
public class RelationshipCountStrategiesImpl extends BaseInclusionStrategies<RelationshipCountStrategiesImpl> implements RelationshipCountStrategies {

    private static final int DEFAULT_COMPACTION_THRESHOLD = 20;

    private final DegreeCachingStrategy degreeCachingStrategy;
    private final CompactionStrategy compactionStrategy;
    private final WeighingStrategy weighingStrategy;

    /**
     * Create default strategies.
     *
     * @return default strategies.
     */
    public static RelationshipCountStrategiesImpl defaultStrategies() {
        return new RelationshipCountStrategiesImpl(
                IncludeNoNodes.getInstance(),
                IncludeNoNodeProperties.getInstance(),
                IncludeAllBusinessRelationships.getInstance(),
                IncludeAllBusinessRelationshipProperties.getInstance(),
                new SingleNodePropertyDegreeCachingStrategy(),
                new ThresholdBasedCompactionStrategy(DEFAULT_COMPACTION_THRESHOLD),
                OneForEach.getInstance()
        );
    }

    /**
     * Constructor.
     *
     * @param nodeInclusionStrategy         strategy.
     * @param nodePropertyInclusionStrategy strategy.
     * @param relationshipInclusionStrategy strategy.
     * @param relationshipPropertyInclusionStrategy
     *                                      strategy.
     * @param weighingStrategy              strategy.
     */
    private RelationshipCountStrategiesImpl(NodeInclusionStrategy nodeInclusionStrategy, NodePropertyInclusionStrategy nodePropertyInclusionStrategy, RelationshipInclusionStrategy relationshipInclusionStrategy, RelationshipPropertyInclusionStrategy relationshipPropertyInclusionStrategy, DegreeCachingStrategy degreeCachingStrategy, CompactionStrategy compactionStrategy, WeighingStrategy weighingStrategy) {
        super(nodeInclusionStrategy, nodePropertyInclusionStrategy, relationshipInclusionStrategy, relationshipPropertyInclusionStrategy);
        this.degreeCachingStrategy = degreeCachingStrategy;
        this.compactionStrategy = compactionStrategy;
        this.weighingStrategy = weighingStrategy;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected RelationshipCountStrategiesImpl newInstance(NodeInclusionStrategy nodeInclusionStrategy, NodePropertyInclusionStrategy nodePropertyInclusionStrategy, RelationshipInclusionStrategy relationshipInclusionStrategy, RelationshipPropertyInclusionStrategy relationshipPropertyInclusionStrategy) {
        return new RelationshipCountStrategiesImpl(nodeInclusionStrategy, nodePropertyInclusionStrategy, relationshipInclusionStrategy, relationshipPropertyInclusionStrategy, getDegreeCachingStrategy(), getCompactionStrategy(), getWeighingStrategy());
    }

    /**
     * Reconfigure this instance to use a custom degree caching strategy.
     *
     * @param degreeCachingStrategy to use.
     * @return reconfigured strategies.
     */
    public RelationshipCountStrategiesImpl with(DegreeCachingStrategy degreeCachingStrategy) {
        return new RelationshipCountStrategiesImpl(getNodeInclusionStrategy(), getNodePropertyInclusionStrategy(), getRelationshipInclusionStrategy(), getRelationshipPropertyInclusionStrategy(), degreeCachingStrategy, getCompactionStrategy(), getWeighingStrategy());
    }

    /**
     * Reconfigure this instance to use a custom compaction strategy.
     *
     * @param compactionStrategy to use.
     * @return reconfigured strategies.
     */
    public RelationshipCountStrategiesImpl with(CompactionStrategy compactionStrategy) {
        return new RelationshipCountStrategiesImpl(getNodeInclusionStrategy(), getNodePropertyInclusionStrategy(), getRelationshipInclusionStrategy(), getRelationshipPropertyInclusionStrategy(), getDegreeCachingStrategy(), compactionStrategy, getWeighingStrategy());
    }

    /**
     * Reconfigure this instance to use a {@link ThresholdBasedCompactionStrategy} with a different threshold.
     *
     * @param threshold to use.
     * @return reconfigured strategies.
     */
    public RelationshipCountStrategiesImpl withThreshold(int threshold) {
        return new RelationshipCountStrategiesImpl(getNodeInclusionStrategy(), getNodePropertyInclusionStrategy(), getRelationshipInclusionStrategy(), getRelationshipPropertyInclusionStrategy(), getDegreeCachingStrategy(), new ThresholdBasedCompactionStrategy(threshold), getWeighingStrategy());
    }

    /**
     * Reconfigure this instance to use a custom relationship weighing strategy.
     *
     * @param weighingStrategy to use.
     * @return reconfigured strategies.
     */
    public RelationshipCountStrategiesImpl with(WeighingStrategy weighingStrategy) {
        return new RelationshipCountStrategiesImpl(getNodeInclusionStrategy(), getNodePropertyInclusionStrategy(), getRelationshipInclusionStrategy(), getRelationshipPropertyInclusionStrategy(), getDegreeCachingStrategy(), getCompactionStrategy(), weighingStrategy);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DegreeCachingStrategy getDegreeCachingStrategy() {
        return degreeCachingStrategy;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompactionStrategy getCompactionStrategy() {
        return compactionStrategy;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WeighingStrategy getWeighingStrategy() {
        return weighingStrategy;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String asString() {
        return super.asString() + ";"
                + degreeCachingStrategy.asString() + ";"
                + compactionStrategy.asString() + ";"
                + weighingStrategy.asString();
    }
}
