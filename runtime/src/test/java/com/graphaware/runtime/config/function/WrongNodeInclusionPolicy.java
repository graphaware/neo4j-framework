package com.graphaware.runtime.config.function;

import com.graphaware.common.policy.NodeInclusionPolicy;
import org.neo4j.graphdb.Node;

public class WrongNodeInclusionPolicy implements NodeInclusionPolicy {

    private WrongNodeInclusionPolicy() {
    }

    @Override
    public boolean include(Node object) {
        return false;
    }
}
