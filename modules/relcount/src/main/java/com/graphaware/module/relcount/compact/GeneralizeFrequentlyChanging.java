/*
 * Copyright (c) 2013 GraphAware
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

package com.graphaware.module.relcount.compact;

import com.graphaware.common.description.predicate.Predicate;
import com.graphaware.common.description.relationship.DetachedRelationshipDescription;

import java.util.*;

import static com.graphaware.common.description.predicate.Predicates.any;
import static com.graphaware.common.description.predicate.Predicates.undefined;

/**
 * A {@link GeneralizationStrategy} with a "property change frequency" heuristic.
 * <p/>
 * A human-friendly explanation of what this strategy is trying to achieve is getting rid of (generalizing) properties with
 * frequently changing values (like timestamp on a relationship), whilst keeping the ones that change less frequently,
 * thus providing more value (like strength of a friendship).
 */
class GeneralizeFrequentlyChanging implements GeneralizationStrategy {

    /**
     * {@inheritDoc}
     */
    @Override
    public DetachedRelationshipDescription produceGeneralization(Map<DetachedRelationshipDescription, Integer> cachedDegrees) {
        CachedDegreesStats cachedDegreesStats = new CachedDegreesStats();

        for (DetachedRelationshipDescription description : cachedDegrees.keySet()) {
            cachedDegreesStats.acknowledge(description, cachedDegrees.get(description));
        }

        return new GeneralizationGenerator(cachedDegrees.keySet(), cachedDegreesStats.produceFrequencies()).generate();
    }

    /**
     * Collector of cached degree statistics, which later produces property change frequencies.
     */
    private class CachedDegreesStats {
        private final Map<String, Map<String, Set<Predicate>>> valuesByTypeAndKey = new HashMap<>();
        private final Map<String, Integer> degreeByType = new HashMap<>();
        private final Map<String, Map<String, Integer>> wildcardsByTypeAndKey = new HashMap<>();

        public void acknowledge(DetachedRelationshipDescription description, int degree) {
            initMaps(description.getType().name());
            addDegree(description.getType().name(), degree);
            addValuesAndWildcards(description, degree);
        }

        private void initMaps(String type) {
            if (!degreeByType.containsKey(type)) {
                degreeByType.put(type, 0);
            }
            if (!valuesByTypeAndKey.containsKey(type)) {
                valuesByTypeAndKey.put(type, new HashMap<String, Set<Predicate>>());
            }
            if (!wildcardsByTypeAndKey.containsKey(type)) {
                wildcardsByTypeAndKey.put(type, new HashMap<String, Integer>());
            }
        }

        private void addDegree(String type, int degree) {
            degreeByType.put(type, degreeByType.get(type) + degree);
        }

        private void addValuesAndWildcards(DetachedRelationshipDescription description, int degree) {
            String type = description.getType().name();

            Set<String> allKeysForType = new HashSet<>();
            allKeysForType.addAll(valuesByTypeAndKey.get(type).keySet());
            allKeysForType.addAll(wildcardsByTypeAndKey.get(type).keySet());

            for (String key : description.getPropertiesDescription().getKeys()) {
                if (!valuesByTypeAndKey.get(type).containsKey(key)) {
                    valuesByTypeAndKey.get(type).put(key, new HashSet<Predicate>());
                    if (degreeByType.get(type) > degree) {
                        //key not encountered before, thus there is at least one occurrence of unknown
                        valuesByTypeAndKey.get(type).get(key).add(undefined());
                    }
                }

                if (!wildcardsByTypeAndKey.get(type).containsKey(key)) {
                    wildcardsByTypeAndKey.get(type).put(key, 0);
                }

                allKeysForType.remove(key);
                Predicate value = description.getPropertiesDescription().get(key);
                if (any().equals(value)) {
                    wildcardsByTypeAndKey.get(type).put(key, wildcardsByTypeAndKey.get(type).get(key) + degree);
                } else {
                    valuesByTypeAndKey.get(type).get(key).add(value);
                }
            }

            for (String newKey : allKeysForType) {
                valuesByTypeAndKey.get(type).get(newKey).add(undefined());
            }
        }

        private List<PropertyChangeFrequency> produceFrequencies() {
            Set<PropertyChangeFrequency> propertyChangeFrequencies = new TreeSet<>();
            for (String type : degreeByType.keySet()) {
                for (String key : valuesByTypeAndKey.get(type).keySet()) {
                    propertyChangeFrequencies.add(new PropertyChangeFrequency(type, key,
                            ((double) valuesByTypeAndKey.get(type).get(key).size()
                                    + wildcardsByTypeAndKey.get(type).get(key)) / ((double) degreeByType.get(type) + 1)));
                }
            }

            return new LinkedList<>(propertyChangeFrequencies);
        }
    }
}
