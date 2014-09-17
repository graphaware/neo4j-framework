package com.graphaware.common.strategy.expression;

import com.graphaware.common.strategy.NodeCentricRelationshipInclusionStrategy;
import com.graphaware.common.strategy.NodeInclusionStrategy;
import com.graphaware.common.strategy.RelationshipInclusionStrategy;
import org.junit.Test;
import org.neo4j.graphdb.Transaction;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Unit test for {@link SpelRelationshipInclusionStrategy}.
 */
public class SpelRelationshipInclusionStrategyTest extends SpelInclusionStrategyTest {

    private NodeCentricRelationshipInclusionStrategy strategy1 = new SpelRelationshipInclusionStrategy("isType('WORKS_FOR')");
    private NodeCentricRelationshipInclusionStrategy strategy2 = new SpelRelationshipInclusionStrategy("getType() == 'WORKS_FOR'");
    private NodeCentricRelationshipInclusionStrategy strategy3 = new SpelRelationshipInclusionStrategy("isType('LIVES_IN') && isIncoming()");
    private NodeCentricRelationshipInclusionStrategy strategy4 = new SpelRelationshipInclusionStrategy("startNode.hasLabel('Employee')");
    private NodeCentricRelationshipInclusionStrategy strategy5 = new SpelRelationshipInclusionStrategy("otherNode.hasLabel('Employee')");
    private NodeCentricRelationshipInclusionStrategy strategy6 = new SpelRelationshipInclusionStrategy("hasProperty('until')");

    @Test
    public void shouldIncludeCorrectRelationships() {
        try (Transaction tx = database.beginTx()) {
            assertTrue(strategy1.include(michalWorksFor()));
            assertTrue(strategy1.include(vojtaWorksFor()));
            assertFalse(strategy1.include(michalLivesIn()));
            assertFalse(strategy1.include(vojtaLivesIn()));

            assertTrue(strategy2.include(michalWorksFor()));
            assertTrue(strategy2.include(vojtaWorksFor()));
            assertFalse(strategy2.include(michalLivesIn()));
            assertFalse(strategy2.include(vojtaLivesIn()));

            assertTrue(strategy3.include(michalLivesIn(), london()));
            assertFalse(strategy3.include(michalLivesIn(), michal()));
            assertTrue(strategy3.include(vojtaLivesIn(), london()));
            assertFalse(strategy3.include(vojtaLivesIn(), vojta()));
            assertFalse(strategy3.include(michalWorksFor(), michal()));
            assertFalse(strategy3.include(michalWorksFor(), graphaware()));

            assertTrue(strategy4.include(michalLivesIn(), michal()));
            assertTrue(strategy4.include(michalLivesIn(), london()));
            assertTrue(strategy4.include(michalWorksFor(), michal()));
            assertTrue(strategy4.include(michalWorksFor(), graphaware()));
            assertFalse(strategy4.include(vojtaLivesIn(), london()));
            assertFalse(strategy4.include(vojtaLivesIn(), vojta()));

            assertTrue(strategy4.include(michalLivesIn()));
            assertTrue(strategy4.include(michalWorksFor()));
            assertFalse(strategy4.include(vojtaLivesIn()));

            assertFalse(strategy5.include(michalLivesIn(), michal()));
            assertTrue(strategy5.include(michalLivesIn(), london()));
            assertFalse(strategy5.include(michalWorksFor(), michal()));
            assertTrue(strategy5.include(michalWorksFor(), graphaware()));
            assertFalse(strategy5.include(vojtaLivesIn(), london()));
            assertFalse(strategy5.include(vojtaLivesIn(), vojta()));

            assertFalse(strategy6.include(michalLivesIn()));
            assertFalse(strategy6.include(michalWorksFor()));
            assertFalse(strategy6.include(vojtaLivesIn()));
            assertTrue(strategy6.include(vojtaWorksFor()));

            tx.success();
        }
    }

    @Test(expected = Exception.class)
    public void shouldComplainAboutIncorrectUsage1() {
        try (Transaction tx = database.beginTx()) {
            strategy5.include(michalLivesIn(), vojta());
            tx.success();
        }
    }

    @Test(expected = Exception.class)
    public void shouldComplainAboutIncorrectUsage2() {
        try (Transaction tx = database.beginTx()) {
            strategy3.include(michalLivesIn());
            tx.success();
        }
    }

    @Test(expected = Exception.class)
    public void shouldComplainAboutIncorrectUsage3() {
        try (Transaction tx = database.beginTx()) {
            strategy5.include(michalLivesIn());
            tx.success();
        }
    }
}
