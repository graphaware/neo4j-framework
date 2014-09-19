package com.graphaware.common.policy.composite;

import com.graphaware.common.policy.RelationshipInclusionPolicy;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

/**
 * {@link CompositePropertyContainerInclusionPolicy} for {@link Relationship}s.
 */
public final class CompositeRelationshipInclusionPolicy extends CompositePropertyContainerInclusionPolicy<Relationship, RelationshipInclusionPolicy> implements RelationshipInclusionPolicy {

    public static CompositeRelationshipInclusionPolicy of(RelationshipInclusionPolicy... policies) {
        return new CompositeRelationshipInclusionPolicy(policies);
    }

    private CompositeRelationshipInclusionPolicy(RelationshipInclusionPolicy[] policies) {
        super(policies);
    }

    @Override
    public boolean include(Relationship relationship, Node pointOfView) {
        for (RelationshipInclusionPolicy policy : policies) {
            if (!policy.include(relationship, pointOfView)) {
                return false;
            }
        }

        return true;
    }
}
