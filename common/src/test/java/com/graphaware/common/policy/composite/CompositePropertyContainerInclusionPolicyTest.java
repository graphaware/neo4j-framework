package com.graphaware.common.policy.composite;

import com.graphaware.common.policy.NodeInclusionPolicy;
import com.graphaware.common.policy.RelationshipInclusionPolicy;
import com.graphaware.common.policy.all.IncludeAllNodes;
import com.graphaware.common.policy.all.IncludeAllRelationships;
import com.graphaware.common.policy.none.IncludeNoNodes;
import com.graphaware.common.policy.none.IncludeNoRelationships;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 *  Unit test for {@link CompositePropertyContainerInclusionPolicy}.
 */
public class CompositePropertyContainerInclusionPolicyTest {

    @Test(expected = IllegalArgumentException.class)
    public void cannotConstructEmptyCompositePolicy() {
        CompositeNodeInclusionPolicy.of(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void cannotConstructEmptyCompositePolicy2() {
        CompositeNodeInclusionPolicy.of();
    }

    @Test(expected = IllegalArgumentException.class)
    public void cannotConstructEmptyCompositePolicy3() {
        CompositeRelationshipInclusionPolicy.of(new RelationshipInclusionPolicy[0]);
    }

    @Test
    public void whenAllVoteYesThenTrueIsReturned() {
        assertTrue(CompositeNodeInclusionPolicy.of(IncludeAllNodes.getInstance(), IncludeAllNodes.getInstance()).include(null));
        assertTrue(CompositeRelationshipInclusionPolicy.of(IncludeAllRelationships.getInstance(), IncludeAllRelationships.getInstance()).include(null));
    }

    @Test
    public void whenOneVotesNoThenFalseIsReturned() {
        assertFalse(CompositeNodeInclusionPolicy.of(IncludeNoNodes.getInstance(), IncludeAllNodes.getInstance()).include(null));
        assertFalse(CompositeRelationshipInclusionPolicy.of(IncludeAllRelationships.getInstance(), IncludeNoRelationships.getInstance()).include(null));
    }
}
