package com.graphaware.runtime.config.function;

import com.graphaware.common.strategy.NodeInclusionStrategy;
import com.graphaware.common.strategy.expression.SpelNodeInclusionStrategy;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit test for {@link StringToNodeInclusionStrategy}.
 */
public class StringToNodeInclusionStrategyTest {

    @Test
    public void shouldConstructStrategyFromClassName() {
        NodeInclusionStrategy strategy = StringToNodeInclusionStrategy.getInstance().apply("com.graphaware.runtime.config.function.TestNodeInclusionStrategy");

        assertNotNull(strategy);
        assertTrue(strategy instanceof TestNodeInclusionStrategy);
    }

    @Test
    public void shouldConstructSpelStrategy() {
        NodeInclusionStrategy strategy = StringToNodeInclusionStrategy.getInstance().apply("hasLabel('Test')");

        assertEquals(new SpelNodeInclusionStrategy("hasLabel('Test')"), strategy);
    }

    @Test
    public void shouldConstructSpelStrategy2() {
        NodeInclusionStrategy strategy = StringToNodeInclusionStrategy.getInstance().apply("isType('R1')");

        assertEquals(new SpelNodeInclusionStrategy("isType('R1')"), strategy);
    }
}
