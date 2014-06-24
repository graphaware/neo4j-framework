package com.graphaware.runtime.config;

import com.graphaware.common.strategy.*;

/**
 * Base-class for {@link TxDrivenModuleConfiguration} implementations.
 */
public abstract class BaseTxDrivenModuleConfiguration<T extends BaseTxDrivenModuleConfiguration<T>> {

    private final InclusionStrategies inclusionStrategies;

    protected BaseTxDrivenModuleConfiguration(InclusionStrategies inclusionStrategies) {
        this.inclusionStrategies = inclusionStrategies;
    }

    protected abstract T newInstance(InclusionStrategies inclusionStrategies);

    /**
     * Get inclusion strategies encapsulated by this configuration.
     *
     * @return strategies.
     */
    public InclusionStrategies getInclusionStrategies() {
        return inclusionStrategies;
    }

    public T with(NodeInclusionStrategy nodeInclusionStrategy) {
        return newInstance(inclusionStrategies.with(nodeInclusionStrategy));
    }

    public T with(NodePropertyInclusionStrategy nodePropertyInclusionStrategy) {
        return newInstance(inclusionStrategies.with(nodePropertyInclusionStrategy));
    }

    public T with(RelationshipInclusionStrategy relationshipInclusionStrategy) {
        return newInstance(inclusionStrategies.with(relationshipInclusionStrategy));
    }

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
