package com.graphaware.runtime.config.function;

import com.graphaware.common.strategy.NodeInclusionStrategy;
import org.neo4j.graphdb.Node;

public class TestNodeInclusionStrategy implements NodeInclusionStrategy {

    @Override
    public boolean include(Node object) {
        return false;
    }
}
