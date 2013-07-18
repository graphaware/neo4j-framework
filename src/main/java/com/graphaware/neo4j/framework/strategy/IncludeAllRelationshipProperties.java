package com.graphaware.neo4j.framework.strategy;

import com.graphaware.neo4j.tx.event.strategy.RelationshipPropertyInclusionStrategy;import org.neo4j.graphdb.Relationship;import java.lang.Override;import java.lang.String;

/**
 * Strategy that includes all (non-internal) relationship properties. Singleton.
 */
public final class IncludeAllRelationshipProperties extends IncludeAllBusinessProperties<Relationship> implements RelationshipPropertyInclusionStrategy {

    private static final IncludeAllRelationshipProperties INSTANCE = new IncludeAllRelationshipProperties();

    public static IncludeAllRelationshipProperties getInstance() {
        return INSTANCE;
    }

    private IncludeAllRelationshipProperties() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean doInclude(String key, Relationship relationship) {
        return true;
    }
}
