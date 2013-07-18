package com.graphaware.neo4j.framework.strategy;

import com.graphaware.neo4j.tx.event.strategy.NodePropertyInclusionStrategy;import org.neo4j.graphdb.Node;import java.lang.Override;import java.lang.String;

/**
 * Strategy that includes all (non-internal) node properties. Singleton.
 */
public final class IncludeAllNodeProperties extends IncludeAllBusinessProperties<Node> implements NodePropertyInclusionStrategy {

    private static final IncludeAllNodeProperties INSTANCE = new IncludeAllNodeProperties();

    public static IncludeAllNodeProperties getInstance() {
        return INSTANCE;
    }

    private IncludeAllNodeProperties() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean doInclude(String key, Node node) {
        return true;
    }
}
