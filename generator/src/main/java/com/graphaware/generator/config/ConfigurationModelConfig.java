package com.graphaware.generator.config;

import java.util.List;

/**
 *
 */
public class ConfigurationModelConfig extends SimpleDegreeDistribution {

    public ConfigurationModelConfig(List<Integer> degrees) {
        super(degrees);
    }

    /**
     * {@inheritDoc}
     *
     * All valid distributions must contain even number of stubs.
     * //todo should this also be in the superclass?
     */
    @Override
    public boolean isValid() {
        // If the distribution is graphical => exist a sub-distribution which is graphical
        int degreeSum = 0;           // Has to be even by the handshaking lemma
        for (int degree : getDegrees()) {
            degreeSum += degree;
        }
        return (degreeSum % 2) == 0;
    }
}
