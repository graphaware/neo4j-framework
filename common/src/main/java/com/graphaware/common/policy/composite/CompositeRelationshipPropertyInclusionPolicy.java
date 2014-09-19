package com.graphaware.common.policy.composite;

import com.graphaware.common.policy.RelationshipPropertyInclusionPolicy;
import org.neo4j.graphdb.Relationship;

/**
 * {@link CompositePropertyInclusionPolicy} for {@link Relationship}s.
 */
public final class CompositeRelationshipPropertyInclusionPolicy extends CompositePropertyInclusionPolicy<Relationship> implements RelationshipPropertyInclusionPolicy {

    public static CompositeRelationshipPropertyInclusionPolicy of(RelationshipPropertyInclusionPolicy... policies) {
        return new CompositeRelationshipPropertyInclusionPolicy(policies);
    }

    private CompositeRelationshipPropertyInclusionPolicy(RelationshipPropertyInclusionPolicy[] policies) {
        super(policies);
    }
}
