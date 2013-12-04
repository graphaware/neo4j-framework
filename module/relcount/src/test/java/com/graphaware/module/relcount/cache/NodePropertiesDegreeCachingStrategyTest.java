package com.graphaware.module.relcount.cache;

/**
 * Unit test for {@link com.graphaware.module.relcount.cache.NodePropertiesDegreeCachingStrategy}.
 */
public class NodePropertiesDegreeCachingStrategyTest extends DegreeCachingStrategyTest {

    protected DegreeCachingStrategy strategy() {
        return new NodePropertiesDegreeCachingStrategy();
    }
}
