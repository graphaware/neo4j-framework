/*
 * Copyright (c) 2014 GraphAware
 *
 * This file is part of GraphAware.
 *
 * GraphAware is free software: you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received a copy of
 * the GNU General Public License along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 */

/**
 * EXPERIMENTAL
 */

package com.graphaware.generator.relationship;

import com.graphaware.common.util.SameTypePair;
import com.graphaware.generator.config.ConfigurationModelConfig;
import com.graphaware.generator.config.DegreeDistribution;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.shuffle;

/**
 * A simple-minded {@link RelationshipGenerator} based on a {@link DegreeDistribution}.
 * Allows multiple edges and self-loops.
 * <p/>
 * For a simple graph generator, see {@link SimpleGraphRelationshipGenerator}.
 *
 * @see {@link SimpleGraphRelationshipGenerator}.
 */
public class ConfigurationModelRelationshipGenerator extends BaseRelationshipGenerator<ConfigurationModelConfig> {

    public ConfigurationModelRelationshipGenerator(ConfigurationModelConfig configuration) {
        super(configuration);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<SameTypePair<Integer>> doGenerateEdges() {
        List<Integer> spread = spreadDistribution(getConfiguration());
        shuffle(spread);

        List<SameTypePair<Integer>> pairs = new ArrayList<>();

        int L = spread.size();
        for (int i = 0; i < L; i += 2) {
            pairs.add(new SameTypePair<>(spread.get(i), spread.get(i + 1)));
        }

        return pairs;
    }

    /**
     * Takes distribution indices and spreads them over degree times for each node, to allow for pairing manipulation.
     *
     * @return spread degrees.
     */
    private List<Integer> spreadDistribution(DegreeDistribution distribution) {
        List<Integer> spread = new ArrayList<>();

        int L = distribution.size();
        for (int k = 0; k < L; ++k) {
            for (int j = 0; j < distribution.get(k); ++j) {
                spread.add(k);
            }
        }

        return spread;
    }
}
