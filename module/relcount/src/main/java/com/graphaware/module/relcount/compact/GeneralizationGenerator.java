package com.graphaware.module.relcount.compact;

import com.graphaware.common.description.relationship.DetachedRelationshipDescription;

import java.util.*;

import static com.graphaware.common.description.predicate.Predicates.any;

/**
 * Component that produces generalizations of given {@link DetachedRelationshipDescription}s based on their
 * {@link PropertyChangeFrequency}s, so that the produced generalization results in maximum possible compaction of
 * cached degrees.
 * <p/>
 * Given properties A of relationship type T1, B of T2, and C of T1 with property change frequencies decreasing in this
 * order, the following generalizations are attempted in this order:
 * <p/>
 * 1) generalizations of all {@link DetachedRelationshipDescription}s with relationship type = T1 with A set to {@link com.graphaware.common.description.predicate.Any}.
 * 2) generalizations of all {@link DetachedRelationshipDescription}s with relationship type = T2 with B set to {@link com.graphaware.common.description.predicate.Any}.
 * 3) generalizations of all {@link DetachedRelationshipDescription}s with relationship type = T1 with C set to {@link com.graphaware.common.description.predicate.Any}.
 * 4) generalizations of all {@link DetachedRelationshipDescription}s with relationship type = T1 with A and C set to {@link com.graphaware.common.description.predicate.Any}.
 * <p/>
 * As soon as one element in a set of attempted generalizations results in a compaction, the element that results in
 * maximum compaction is returned. In the above example, if this already happens in point 1), no further generalizations
 * are attempted.
 */
class GeneralizationGenerator {

    private final Set<DetachedRelationshipDescription> descriptions;
    private final Iterator<PropertyChangeFrequency> frequencies;
    private final Map<String, List<Set<String>>> usedPropertySetsByType = new HashMap<>();

    /**
     * Construct a new generalizer.
     *
     * @param descriptions from which to create generalizations.
     * @param frequencies  of properties of the above descriptions.
     */
    GeneralizationGenerator(Set<DetachedRelationshipDescription> descriptions, List<PropertyChangeFrequency> frequencies) {
        this.descriptions = descriptions;
        this.frequencies = frequencies.iterator();
    }

    /**
     * Generate the best generalization that will result in a compaction.
     *
     * @return best generalization.
     */
    public DetachedRelationshipDescription generate() {
        if (!frequencies.hasNext()) {
            return null;
        }

        PropertyChangeFrequency frequency = frequencies.next();

        DetachedRelationshipDescription attempt = generate(frequency.getType(), createNewPropertySets(frequency));

        if (attempt == null) {
            return generate();
        }

        return attempt;
    }

    private DetachedRelationshipDescription generate(String type, List<Set<String>> newPropertySets) {
        for (Set<String> newPropertySet : newPropertySets) {
            int maxMatches = 1;
            DetachedRelationshipDescription result = null;

            for (DetachedRelationshipDescription candidate : descriptions) {
                if (!candidate.getType().name().equals(type)) {
                    continue;
                }

                DetachedRelationshipDescription generalizedDescription = candidate;
                for (String property : newPropertySet) {
                    generalizedDescription = generalizedDescription.with(property, any());
                }

                int matches = 0;
                for (DetachedRelationshipDescription description : descriptions) {
                    if (description.isMoreSpecificThan(generalizedDescription)) {
                        matches++;
                    }
                }

                if (matches > maxMatches) {
                    maxMatches = matches;
                    result = generalizedDescription;
                }
            }

            if (result != null) {
                return result;
            }
        }

        return null;
    }

    private List<Set<String>> createNewPropertySets(PropertyChangeFrequency frequency) {
        if (!usedPropertySetsByType.containsKey(frequency.getType())) {
            usedPropertySetsByType.put(frequency.getType(), new LinkedList<Set<String>>());
        }

        List<Set<String>> newPropertySets = new LinkedList<>();

        newPropertySets.add(Collections.singleton(frequency.getProperty()));
        for (Set<String> usedPropertySet : usedPropertySetsByType.get(frequency.getType())) {
            Set<String> newPropertySet = new HashSet<>(usedPropertySet);
            newPropertySet.add(frequency.getProperty());
            newPropertySets.add(newPropertySet);
        }

        usedPropertySetsByType.get(frequency.getType()).addAll(newPropertySets);

        return newPropertySets;
    }
}
