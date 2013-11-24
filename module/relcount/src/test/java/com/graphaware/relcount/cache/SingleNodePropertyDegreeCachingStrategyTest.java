package com.graphaware.relcount.cache;

/**
 * Unit test for {@link com.graphaware.relcount.cache.SingleNodePropertyDegreeCachingStrategy}.
 */
public class SingleNodePropertyDegreeCachingStrategyTest extends DegreeCachingStrategyTest {

    protected DegreeCachingStrategy strategy() {
        return new SingleNodePropertyDegreeCachingStrategy();
    }
}
