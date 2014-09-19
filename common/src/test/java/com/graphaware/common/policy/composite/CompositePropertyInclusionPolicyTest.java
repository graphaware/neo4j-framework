package com.graphaware.common.policy.composite;

import com.graphaware.common.policy.RelationshipPropertyInclusionPolicy;
import com.graphaware.common.policy.all.IncludeAllNodeProperties;
import com.graphaware.common.policy.all.IncludeAllRelationshipProperties;
import com.graphaware.common.policy.none.IncludeNoNodeProperties;
import com.graphaware.common.policy.none.IncludeNoRelationshipProperties;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 *  Unit test for {@link CompositePropertyInclusionPolicy}.
 */
public class CompositePropertyInclusionPolicyTest {

    @Test(expected = IllegalArgumentException.class)
    public void cannotConstructEmptyCompositePolicy() {
        CompositeNodePropertyInclusionPolicy.of(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void cannotConstructEmptyCompositePolicy2() {
        CompositeNodePropertyInclusionPolicy.of();
    }

    @Test(expected = IllegalArgumentException.class)
    public void cannotConstructEmptyCompositePolicy3() {
        CompositeRelationshipPropertyInclusionPolicy.of(new RelationshipPropertyInclusionPolicy[0]);
    }

    @Test
    public void whenAllVoteYesThenTrueIsReturned() {
        assertTrue(CompositeNodePropertyInclusionPolicy.of(IncludeAllNodeProperties.getInstance(), IncludeAllNodeProperties.getInstance()).include("test", null));
        assertTrue(CompositeRelationshipPropertyInclusionPolicy.of(IncludeAllRelationshipProperties.getInstance(), IncludeAllRelationshipProperties.getInstance()).include("test", null));
    }

    @Test
    public void whenOneVotesNoThenFalseIsReturned() {
        assertFalse(CompositeNodePropertyInclusionPolicy.of(IncludeNoNodeProperties.getInstance(), IncludeAllNodeProperties.getInstance()).include("test", null));
        assertFalse(CompositeRelationshipPropertyInclusionPolicy.of(IncludeAllRelationshipProperties.getInstance(), IncludeNoRelationshipProperties.getInstance()).include("test", null));
    }
}
