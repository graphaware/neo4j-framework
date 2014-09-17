package com.graphaware.common.strategy.expression;

import com.graphaware.common.strategy.RelationshipPropertyInclusionStrategy;
import org.junit.Test;
import org.neo4j.graphdb.Transaction;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Unit test for {@link SpelRelationshipPropertyInclusionStrategy}.
 */
public class SpelRelationshipPropertyInclusionStrategyTest extends SpelInclusionStrategyTest {

    @Test
    public void shouldIncludeCorrectProps() {
        RelationshipPropertyInclusionStrategy strategy1 = new SpelRelationshipPropertyInclusionStrategy("key != 'since'");
        RelationshipPropertyInclusionStrategy strategy2 = new SpelRelationshipPropertyInclusionStrategy("relationship.isType('WORKS_FOR')");

        try (Transaction tx = database.beginTx()) {
            assertFalse(strategy1.include("since", vojtaWorksFor()));
            assertFalse(strategy1.include("since", michalWorksFor()));
            assertTrue(strategy1.include("until", michalWorksFor()));
            assertTrue(strategy1.include("until", vojtaWorksFor()));

            assertTrue(strategy2.include("since", michalWorksFor()));
            assertFalse(strategy2.include("since", michalLivesIn()));
            assertTrue(strategy2.include("since", vojtaWorksFor()));
            assertFalse(strategy2.include("since", vojtaLivesIn()));

            tx.success();
        }
    }
}
