package com.graphaware.common.strategy.expression;

import com.graphaware.common.strategy.NodeInclusionStrategy;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.test.TestGraphDatabaseFactory;

import static org.junit.Assert.*;

/**
 * Unit test for {@link SpelNodeInclusionStrategy}.
 */
public class SpelNodeInclusionStrategyTest extends SpelInclusionStrategyTest {

    @Test
    public void shouldIncludeCorrectNodes() {
        NodeInclusionStrategy strategy1 = new SpelNodeInclusionStrategy("hasLabel('Employee') || hasProperty('form') || getProperty('age', 0) > 20");
        NodeInclusionStrategy strategy2 = new SpelNodeInclusionStrategy("getDegree('OUTGOING') > 1");
        NodeInclusionStrategy strategy3 = new SpelNodeInclusionStrategy("getDegree('WORKS_FOR', 'BOTH') > 1");
        NodeInclusionStrategy strategy4 = new SpelNodeInclusionStrategy("getDegree('WORKS_FOR', 'incoming') > 1");

        try (Transaction tx = database.beginTx()) {
            assertTrue(strategy1.include(michal()));
            assertTrue(strategy1.include(graphaware()));
            assertTrue(strategy1.include(vojta()));
            assertFalse(strategy1.include(london()));

            assertTrue(strategy2.include(michal()));
            assertFalse(strategy2.include(graphaware()));
            assertTrue(strategy2.include(vojta()));
            assertFalse(strategy2.include(london()));

            assertFalse(strategy3.include(michal()));
            assertTrue(strategy3.include(graphaware()));
            assertFalse(strategy3.include(vojta()));
            assertFalse(strategy3.include(london()));

            assertFalse(strategy4.include(michal()));
            assertTrue(strategy4.include(graphaware()));
            assertFalse(strategy4.include(vojta()));
            assertFalse(strategy4.include(london()));

            tx.success();
        }
    }
}
