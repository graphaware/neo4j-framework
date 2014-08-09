package com.graphaware.runtime.strategy;

import com.graphaware.common.description.predicate.Predicate;
import com.graphaware.common.description.property.DetachedPropertiesDescription;
import com.graphaware.common.description.property.WildcardPropertiesDescription;
import com.graphaware.common.strategy.BaseIncludeRelationships;
import com.graphaware.runtime.config.RuntimeConfiguration;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;

import java.util.Collections;

/**
 * An implementation of {@link com.graphaware.common.strategy.RelationshipInclusionStrategy} that is entirely configurable using
 * its fluent interface and never includes relationships internal to the framework and/or {@link com.graphaware.runtime.GraphAwareRuntime}.
 */
public class IncludeBusinessRelationships extends BaseIncludeRelationships<IncludeBusinessRelationships> {

    /**
     * Get a relationship inclusion strategy that includes all business relationships as the base-line for further configuration.
     * <p/>
     * Note that if you want to simply include all business relationship, it is more efficient to use {@link IncludeAllBusinessRelationships}.
     *
     * @return a strategy including all relationships.
     */
    public static IncludeBusinessRelationships all() {
        return new IncludeBusinessRelationships(Direction.BOTH, new RelationshipType[0], new WildcardPropertiesDescription(Collections.<String, Predicate>emptyMap()));
    }

    /**
     * Create a new strategy.
     *
     * @param direction             that matching relationships must have, {@link Direction#BOTH} for both.
     * @param relationshipTypes     one of which the matching relationships must have, empty for all.
     * @param propertiesDescription of the matching relationships.
     */
    protected IncludeBusinessRelationships(Direction direction, RelationshipType[] relationshipTypes, DetachedPropertiesDescription propertiesDescription) {
        super(direction, relationshipTypes, propertiesDescription);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected IncludeBusinessRelationships newInstance(Direction direction, RelationshipType... relationshipTypes) {
        return new IncludeBusinessRelationships(direction, relationshipTypes, getPropertiesDescription());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected IncludeBusinessRelationships newInstance(DetachedPropertiesDescription propertiesDescription) {
        return new IncludeBusinessRelationships(getDirection(), getRelationshipTypes(), propertiesDescription);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean include(Relationship relationship) {
        if (relationship.getType().name().startsWith(RuntimeConfiguration.GA_PREFIX)) {
            return false;
        }

        return super.include(relationship);
    }
}
