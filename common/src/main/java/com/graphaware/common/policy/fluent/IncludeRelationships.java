package com.graphaware.common.policy.fluent;

import com.graphaware.common.description.predicate.Predicate;
import com.graphaware.common.description.property.DetachedPropertiesDescription;
import com.graphaware.common.description.property.WildcardPropertiesDescription;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.RelationshipType;

import java.util.Collections;

/**
 * An implementation of {@link com.graphaware.common.policy.RelationshipInclusionPolicy} that is entirely configurable using its fluent interface.
 */
public class IncludeRelationships extends BaseIncludeRelationships<IncludeRelationships> {

    /**
     * Get a relationship inclusion policy that includes all relationships as the base-line for further configuration.
     * <p/>
     * Note that if you want to simply include all relationships, it is more efficient to use {@link com.graphaware.common.policy.all.IncludeAllRelationships}.
     *
     * @return a policy including all relationships.
     */
    public static IncludeRelationships all() {
        return new IncludeRelationships(Direction.BOTH, new RelationshipType[0], new WildcardPropertiesDescription(Collections.<String, Predicate>emptyMap()));
    }

    /**
     * Create a new policy.
     *
     * @param direction             that matching relationships must have, {@link Direction#BOTH} for both.
     * @param relationshipTypes     one of which the matching relationships must have, empty for all.
     * @param propertiesDescription of the matching relationships.
     */
    protected IncludeRelationships(Direction direction, RelationshipType[] relationshipTypes, DetachedPropertiesDescription propertiesDescription) {
        super(direction, relationshipTypes, propertiesDescription);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected IncludeRelationships newInstance(Direction direction, RelationshipType... relationshipTypes) {
        return new IncludeRelationships(direction, relationshipTypes, getPropertiesDescription());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected IncludeRelationships newInstance(DetachedPropertiesDescription propertiesDescription) {
        return new IncludeRelationships(getDirection(), getRelationshipTypes(), propertiesDescription);
    }
}
