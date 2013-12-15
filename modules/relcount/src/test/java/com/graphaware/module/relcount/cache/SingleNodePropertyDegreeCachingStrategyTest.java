package com.graphaware.module.relcount.cache;

/**
 * Unit test for {@link com.graphaware.module.relcount.cache.SingleNodePropertyDegreeCachingStrategy}.
 */
public class SingleNodePropertyDegreeCachingStrategyTest extends DegreeCachingStrategyTest {

    protected DegreeCachingStrategy strategy() {
        return new SingleNodePropertyDegreeCachingStrategy();
    }
}
