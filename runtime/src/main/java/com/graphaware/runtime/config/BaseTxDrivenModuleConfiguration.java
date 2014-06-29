package com.graphaware.runtime.config;

import com.graphaware.common.strategy.*;

/**
 * Base-class for {@link TxDrivenModuleConfiguration} implementations.
 */
public abstract class BaseTxDrivenModuleConfiguration<T extends BaseTxDrivenModuleConfiguration<T>> implements TxDrivenModuleConfiguration {

    private final InclusionStrategies inclusionStrategies;

    protected BaseTxDrivenModuleConfiguration(InclusionStrategies inclusionStrategies) {
        this.inclusionStrategies = inclusionStrategies;
    }

    /**
     * Create a new instance of this {@link TxDrivenModuleConfiguration} with different inclusion strategies.
     *
     * @param inclusionStrategies of the new instance.
     * @return new instance.
     */
    protected abstract T newInstance(InclusionStrategies inclusionStrategies);

    /**
     * Get inclusion strategies encapsulated by this configuration.
     *
     * @return strategies.
     */
    public InclusionStrategies getInclusionStrategies() {
        return inclusionStrategies;
    }

    /**
     * Create a new instance of this {@link TxDrivenModuleConfiguration} with different node inclusion strategy.
     *
     * @param nodeInclusionStrategy of the new instance.
     * @return new instance.
     */
    public T with(NodeInclusionStrategy nodeInclusionStrategy) {
        return newInstance(inclusionStrategies.with(nodeInclusionStrategy));
    }

    /**
     * Create a new instance of this {@link TxDrivenModuleConfiguration} with different node property inclusion strategy.
     *
     * @param nodePropertyInclusionStrategy of the new instance.
     * @return new instance.
     */
    public T with(NodePropertyInclusionStrategy nodePropertyInclusionStrategy) {
        return newInstance(inclusionStrategies.with(nodePropertyInclusionStrategy));
    }

    /**
     * Create a new instance of this {@link TxDrivenModuleConfiguration} with different relationship inclusion strategy.
     *
     * @param relationshipInclusionStrategy of the new instance.
     * @return new instance.
     */
    public T with(RelationshipInclusionStrategy relationshipInclusionStrategy) {
        return newInstance(inclusionStrategies.with(relationshipInclusionStrategy));
    }

    /**
     * Create a new instance of this {@link TxDrivenModuleConfiguration} with different relationship property inclusion strategy.
     *
     * @param relationshipPropertyInclusionStrategy of the new instance.
     * @return new instance.
     */
    public T with(RelationshipPropertyInclusionStrategy relationshipPropertyInclusionStrategy) {
        return newInstance(inclusionStrategies.with(relationshipPropertyInclusionStrategy));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BaseTxDrivenModuleConfiguration that = (BaseTxDrivenModuleConfiguration) o;

        if (!inclusionStrategies.equals(that.inclusionStrategies)) return false;

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return inclusionStrategies.hashCode();
    }
}
