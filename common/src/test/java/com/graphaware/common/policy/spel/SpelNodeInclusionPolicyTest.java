package com.graphaware.common.policy.spel;

import com.graphaware.common.policy.NodeInclusionPolicy;
import org.junit.Test;
import org.neo4j.graphdb.Transaction;

import static org.junit.Assert.*;

/**
 * Unit test for {@link com.graphaware.common.policy.spel.SpelNodeInclusionPolicy}.
 */
public class SpelNodeInclusionPolicyTest extends SpelInclusionPolicyTest {

    @Test
    public void shouldIncludeCorrectNodes() {
        NodeInclusionPolicy policy1 = new SpelNodeInclusionPolicy("hasLabel('Employee') || hasProperty('form') || getProperty('age', 0) > 20");
        NodeInclusionPolicy policy2 = new SpelNodeInclusionPolicy("getDegree('OUTGOING') > 1");
        NodeInclusionPolicy policy3 = new SpelNodeInclusionPolicy("getDegree('WORKS_FOR', 'BOTH') > 1");
        NodeInclusionPolicy policy4 = new SpelNodeInclusionPolicy("getDegree('WORKS_FOR', 'incoming') > 1");

        try (Transaction tx = database.beginTx()) {
            assertTrue(policy1.include(michal()));
            assertTrue(policy1.include(graphaware()));
            assertTrue(policy1.include(vojta()));
            assertFalse(policy1.include(london()));

            assertTrue(policy2.include(michal()));
            assertFalse(policy2.include(graphaware()));
            assertTrue(policy2.include(vojta()));
            assertFalse(policy2.include(london()));

            assertFalse(policy3.include(michal()));
            assertTrue(policy3.include(graphaware()));
            assertFalse(policy3.include(vojta()));
            assertFalse(policy3.include(london()));

            assertFalse(policy4.include(michal()));
            assertTrue(policy4.include(graphaware()));
            assertFalse(policy4.include(vojta()));
            assertFalse(policy4.include(london()));

            tx.success();
        }
    }
}
