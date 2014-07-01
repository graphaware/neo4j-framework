package com.graphaware.generator.relationship;

import com.graphaware.common.util.SameTypePair;
import com.graphaware.generator.config.RelationshipGeneratorConfig;
import com.graphaware.generator.config.DegreeDistribution;
import com.graphaware.generator.config.InvalidConfigException;

import java.util.List;

/**
 * Abstract base-class for {@link RelationshipGenerator} implementations.
 *
 * @param <T> type of accepted configuration.
 */
public abstract class BaseRelationshipGenerator<T extends RelationshipGeneratorConfig> implements RelationshipGenerator<T> {

    /**
     * {@inheritDoc}
     */
    @Override
    public List<? extends SameTypePair<Integer>> generateEdges(T config) throws InvalidConfigException {
        if (!config.isValid()) {
            throw new InvalidConfigException("The supplied config is not valid");
        }

        return doGenerateEdges(config);
    }

    /**
     * Perform the actual edge generation.
     *
     * @param config to base the generation on.
     * @return generated edges as pair of node IDs that should be connected.
     */
    protected abstract List<? extends SameTypePair<Integer>> doGenerateEdges(T config);
}
