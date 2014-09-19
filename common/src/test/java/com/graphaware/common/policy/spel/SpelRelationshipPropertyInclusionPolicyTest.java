package com.graphaware.common.policy.spel;

import com.graphaware.common.policy.RelationshipPropertyInclusionPolicy;
import org.junit.Test;
import org.neo4j.graphdb.Transaction;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Unit test for {@link com.graphaware.common.policy.spel.SpelRelationshipPropertyInclusionPolicy}.
 */
public class SpelRelationshipPropertyInclusionPolicyTest extends SpelInclusionPolicyTest {

    @Test
    public void shouldIncludeCorrectProps() {
        RelationshipPropertyInclusionPolicy policy1 = new SpelRelationshipPropertyInclusionPolicy("key != 'since'");
        RelationshipPropertyInclusionPolicy policy2 = new SpelRelationshipPropertyInclusionPolicy("relationship.isType('WORKS_FOR')");

        try (Transaction tx = database.beginTx()) {
            assertFalse(policy1.include("since", vojtaWorksFor()));
            assertFalse(policy1.include("since", michalWorksFor()));
            assertTrue(policy1.include("until", michalWorksFor()));
            assertTrue(policy1.include("until", vojtaWorksFor()));

            assertTrue(policy2.include("since", michalWorksFor()));
            assertFalse(policy2.include("since", michalLivesIn()));
            assertTrue(policy2.include("since", vojtaWorksFor()));
            assertFalse(policy2.include("since", vojtaLivesIn()));

            tx.success();
        }
    }
}
