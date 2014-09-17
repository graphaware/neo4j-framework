package com.graphaware.common.strategy.expression;

import com.graphaware.common.strategy.NodeInclusionStrategy;
import com.graphaware.common.strategy.NodePropertyInclusionStrategy;
import org.junit.Test;
import org.neo4j.graphdb.Transaction;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Unit test for {@link SpelNodePropertyInclusionStrategy}.
 */
public class SpelNodePropertyInclusionStrategyTest extends SpelInclusionStrategyTest {

    @Test
    public void shouldIncludeCorrectProps() {
        NodePropertyInclusionStrategy strategy1 = new SpelNodePropertyInclusionStrategy("key != 'name'");
        NodePropertyInclusionStrategy strategy2 = new SpelNodePropertyInclusionStrategy("node.hasLabel('Employee') && key == 'name'");

        try (Transaction tx = database.beginTx()) {
            assertFalse(strategy1.include("name", michal()));
            assertFalse(strategy1.include("name", vojta()));

            assertTrue(strategy2.include("name", michal()));
            assertFalse(strategy2.include("name", vojta()));
            assertFalse(strategy2.include("name", graphaware()));

            tx.success();
        }
    }
}
