package com.graphaware.generator.config;

/**
 *  Configuration for a {@link com.graphaware.generator.relationship.RelationshipGenerator}.
 */
public interface RelationshipGeneratorConfig {

    /**
     * Returns true iff the config is valid.
     *
     * @return true if the config is valid.
     */
    boolean isValid();
}
