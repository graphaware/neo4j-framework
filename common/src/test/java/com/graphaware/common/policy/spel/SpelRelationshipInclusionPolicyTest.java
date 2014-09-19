package com.graphaware.common.policy.spel;

import com.graphaware.common.policy.RelationshipInclusionPolicy;
import org.junit.Test;
import org.neo4j.graphdb.Transaction;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Unit test for {@link com.graphaware.common.policy.spel.SpelRelationshipInclusionPolicy}.
 */
public class SpelRelationshipInclusionPolicyTest extends SpelInclusionPolicyTest {

    private RelationshipInclusionPolicy policy1 = new SpelRelationshipInclusionPolicy("isType('WORKS_FOR')");
    private RelationshipInclusionPolicy policy2 = new SpelRelationshipInclusionPolicy("getType() == 'WORKS_FOR'");
    private RelationshipInclusionPolicy policy3 = new SpelRelationshipInclusionPolicy("isType('LIVES_IN') && isIncoming()");
    private RelationshipInclusionPolicy policy4 = new SpelRelationshipInclusionPolicy("startNode.hasLabel('Employee')");
    private RelationshipInclusionPolicy policy5 = new SpelRelationshipInclusionPolicy("otherNode.hasLabel('Employee')");
    private RelationshipInclusionPolicy policy6 = new SpelRelationshipInclusionPolicy("hasProperty('until')");

    @Test
    public void shouldIncludeCorrectRelationships() {
        try (Transaction tx = database.beginTx()) {
            assertTrue(policy1.include(michalWorksFor()));
            assertTrue(policy1.include(vojtaWorksFor()));
            assertFalse(policy1.include(michalLivesIn()));
            assertFalse(policy1.include(vojtaLivesIn()));

            assertTrue(policy2.include(michalWorksFor()));
            assertTrue(policy2.include(vojtaWorksFor()));
            assertFalse(policy2.include(michalLivesIn()));
            assertFalse(policy2.include(vojtaLivesIn()));

            assertTrue(policy3.include(michalLivesIn(), london()));
            assertFalse(policy3.include(michalLivesIn(), michal()));
            assertTrue(policy3.include(vojtaLivesIn(), london()));
            assertFalse(policy3.include(vojtaLivesIn(), vojta()));
            assertFalse(policy3.include(michalWorksFor(), michal()));
            assertFalse(policy3.include(michalWorksFor(), graphaware()));

            assertTrue(policy4.include(michalLivesIn(), michal()));
            assertTrue(policy4.include(michalLivesIn(), london()));
            assertTrue(policy4.include(michalWorksFor(), michal()));
            assertTrue(policy4.include(michalWorksFor(), graphaware()));
            assertFalse(policy4.include(vojtaLivesIn(), london()));
            assertFalse(policy4.include(vojtaLivesIn(), vojta()));

            assertTrue(policy4.include(michalLivesIn()));
            assertTrue(policy4.include(michalWorksFor()));
            assertFalse(policy4.include(vojtaLivesIn()));

            assertFalse(policy5.include(michalLivesIn(), michal()));
            assertTrue(policy5.include(michalLivesIn(), london()));
            assertFalse(policy5.include(michalWorksFor(), michal()));
            assertTrue(policy5.include(michalWorksFor(), graphaware()));
            assertFalse(policy5.include(vojtaLivesIn(), london()));
            assertFalse(policy5.include(vojtaLivesIn(), vojta()));

            assertFalse(policy6.include(michalLivesIn()));
            assertFalse(policy6.include(michalWorksFor()));
            assertFalse(policy6.include(vojtaLivesIn()));
            assertTrue(policy6.include(vojtaWorksFor()));

            tx.success();
        }
    }

    @Test(expected = Exception.class)
    public void shouldComplainAboutIncorrectUsage1() {
        try (Transaction tx = database.beginTx()) {
            policy5.include(michalLivesIn(), vojta());
            tx.success();
        }
    }

    @Test(expected = Exception.class)
    public void shouldComplainAboutIncorrectUsage2() {
        try (Transaction tx = database.beginTx()) {
            policy3.include(michalLivesIn());
            tx.success();
        }
    }

    @Test(expected = Exception.class)
    public void shouldComplainAboutIncorrectUsage3() {
        try (Transaction tx = database.beginTx()) {
            policy5.include(michalLivesIn());
            tx.success();
        }
    }
}
