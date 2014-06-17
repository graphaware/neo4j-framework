package com.graphaware.generator.relationship;

import com.graphaware.common.util.SameTypePair;
import com.graphaware.generator.distribution.DegreeDistribution;
import com.graphaware.generator.distribution.InvalidDistributionException;
import org.neo4j.graphdb.GraphDatabaseService;

import java.util.List;

/**
 * Abstract base-class for {@link RelationshipGenerator} implementations.
 */
public abstract class BaseRelationshipGenerator implements RelationshipGenerator {

    /**
     * {@inheritDoc}
     */
    @Override
    public List<SameTypePair<Integer>> generateEdges(DegreeDistribution distribution) throws InvalidDistributionException {
        // If the distribution is graphical => exist a sub-distribution which is graphical
        if (!isValidDistribution(distribution)) {
            throw new InvalidDistributionException("The supplied distribution is not graphical");
        }

        return doGenerateEdges(distribution);
    }

    /**
     * Check if the given distribution is valid for the specific generator implementation.
     *
     * @param distribution to check.
     * @return true iff the distribution is valid.
     */
    protected abstract boolean isValidDistribution(DegreeDistribution distribution);

    /**
     * Perform the actual edge generation.
     *
     * @param distribution to base the generation on.
     * @return generated edges as pair of node IDs that should be connected.
     */
    protected abstract List<SameTypePair<Integer>> doGenerateEdges(DegreeDistribution distribution);
}
