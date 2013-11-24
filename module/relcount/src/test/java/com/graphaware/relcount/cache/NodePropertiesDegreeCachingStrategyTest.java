package com.graphaware.relcount.cache;

/**
 * Unit test for {@link com.graphaware.relcount.cache.NodePropertiesDegreeCachingStrategy}.
 */
public class NodePropertiesDegreeCachingStrategyTest extends DegreeCachingStrategyTest {

    protected DegreeCachingStrategy strategy() {
        return new NodePropertiesDegreeCachingStrategy();
    }
}
