package com.graphaware.common.strategy;

import com.graphaware.common.util.DirectionUtils;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;

/**
 * {@link NodeCentricRelationshipInclusionStrategy} including {@link Relationship}s with specific types and directions.
 */
public class IncludeRelationships implements NodeCentricRelationshipInclusionStrategy {

    public static final IncludeRelationships ALL = new IncludeRelationships(Direction.BOTH);
    public static final IncludeRelationships OUTGOING = new IncludeRelationships(Direction.OUTGOING);
    public static final IncludeRelationships INCOMING = new IncludeRelationships(Direction.INCOMING);

    private final Direction direction;
    private final RelationshipType[] relationshipTypes;

    /**
     * Construct a strategy that only includes relationships with a specific direction.
     *
     * @param direction of the relationships to include.
     */
    public IncludeRelationships(Direction direction) {
        this(direction, new RelationshipType[0]);
    }

    /**
     * Construct a strategy that only includes relationships that have one of the specific types.
     *
     * @param relationshipTypes to include.
     */
    public IncludeRelationships(RelationshipType... relationshipTypes) {
        this(Direction.BOTH, relationshipTypes);
    }

    /**
     * Construct a strategy which only includes relationships with a specific direction and one of the specific types.
     *
     * @param direction         of the relationships to include.
     * @param relationshipTypes to include.
     */
    public IncludeRelationships(Direction direction, RelationshipType... relationshipTypes) {
        this.direction = direction;
        this.relationshipTypes = relationshipTypes;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean include(Relationship relationship) {
        if (relationshipTypes == null || relationshipTypes.length == 0) {
            return true;
        }

        for (RelationshipType type : relationshipTypes) {
            if (relationship.isType(type)) {
                return true;
            }
        }

        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean include(Relationship relationship, Node pointOfView) {
        return include(relationship)
                && DirectionUtils.matches(this.direction, DirectionUtils.resolveDirection(relationship, pointOfView));
    }
}
