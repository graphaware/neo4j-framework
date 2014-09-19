package com.graphaware.common.policy.spel;

import com.graphaware.common.policy.NodePropertyInclusionPolicy;
import org.junit.Test;
import org.neo4j.graphdb.Transaction;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Unit test for {@link com.graphaware.common.policy.spel.SpelNodePropertyInclusionPolicy}.
 */
public class SpelNodePropertyInclusionPolicyTest extends SpelInclusionPolicyTest {

    @Test
    public void shouldIncludeCorrectProps() {
        NodePropertyInclusionPolicy policy1 = new SpelNodePropertyInclusionPolicy("key != 'name'");
        NodePropertyInclusionPolicy policy2 = new SpelNodePropertyInclusionPolicy("node.hasLabel('Employee') && key == 'name'");

        try (Transaction tx = database.beginTx()) {
            assertFalse(policy1.include("name", michal()));
            assertFalse(policy1.include("name", vojta()));

            assertTrue(policy2.include("name", michal()));
            assertFalse(policy2.include("name", vojta()));
            assertFalse(policy2.include("name", graphaware()));

            tx.success();
        }
    }
}
