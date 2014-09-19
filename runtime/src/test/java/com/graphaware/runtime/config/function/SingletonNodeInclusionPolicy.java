package com.graphaware.runtime.config.function;

import com.graphaware.common.policy.NodeInclusionPolicy;
import org.neo4j.graphdb.Node;

public class SingletonNodeInclusionPolicy implements NodeInclusionPolicy {

    private static final SingletonNodeInclusionPolicy INSTANCE = new SingletonNodeInclusionPolicy();

    public static SingletonNodeInclusionPolicy getInstance() {
        return INSTANCE;
    }

    private SingletonNodeInclusionPolicy() {
    }

    @Override
    public boolean include(Node object) {
        return false;
    }
}
