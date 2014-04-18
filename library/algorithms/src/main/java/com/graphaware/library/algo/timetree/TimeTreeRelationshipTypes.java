package com.graphaware.library.algo.timetree;

import org.neo4j.graphdb.RelationshipType;

/**
 * {@link RelationshipType}s for {@link TimeTree}.
 */
public enum TimeTreeRelationshipTypes implements RelationshipType {
    FIRST, LAST, NEXT, CHILD,
}
