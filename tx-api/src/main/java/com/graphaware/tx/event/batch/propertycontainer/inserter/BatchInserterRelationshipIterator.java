/*
 * Copyright (c) 2013 GraphAware
 *
 * This file is part of GraphAware.
 *
 * GraphAware is free software: you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received a copy of
 * the GNU General Public License along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 */

package com.graphaware.tx.event.batch.propertycontainer.inserter;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.helpers.collection.PrefetchingIterator;
import org.neo4j.unsafe.batchinsert.BatchInserter;
import org.neo4j.unsafe.batchinsert.BatchRelationship;

import java.util.Iterator;

/**
 * {@link org.neo4j.graphdb.Relationship} {@link java.util.Iterator} for all kinds of relationship filtering by type and direction.
 */
public class BatchInserterRelationshipIterator extends PrefetchingIterator<Relationship> implements Iterable<Relationship> {

    private final long nodeId;
    private final BatchInserter batchInserter;
    private final Iterator<BatchRelationship> batchRelationships;
    private final Direction direction;
    private final RelationshipType[] relationshipTypes;

    public BatchInserterRelationshipIterator(long nodeId, BatchInserter batchInserter, Direction direction, RelationshipType... relationshipTypes) {
        this.nodeId = nodeId;
        this.batchInserter = batchInserter;
        this.batchRelationships = batchInserter.getRelationships(nodeId).iterator();
        this.direction = direction;
        this.relationshipTypes = relationshipTypes; //empty or null = any
    }

    @Override
    protected Relationship fetchNextOrNull() {
        while (batchRelationships.hasNext()) {
            BatchRelationship next = batchRelationships.next();
            if (!typeMatches(next)) {
                continue;
            }

            if (!directionMatches(next)) {
                continue;
            }

            return new BatchInserterRelationship(next, batchInserter);
        }

        return null;
    }

    private boolean typeMatches(BatchRelationship batchRelationship) {
        if (relationshipTypes == null || relationshipTypes.length == 0) {
            return true;
        }

        for (RelationshipType relationshipType : relationshipTypes) {
            if (relationshipType.name().equals(batchRelationship.getType().name())) {
                return true;
            }
        }

        return false;
    }

    private boolean directionMatches(BatchRelationship batchRelationship) {
        if (Direction.BOTH.equals(direction)) {
            return true;
        }

        if (Direction.INCOMING.equals(direction) && nodeId == batchRelationship.getEndNode()) {
            return true;
        }

        if (Direction.OUTGOING.equals(direction) && nodeId == batchRelationship.getStartNode()) {
            return true;
        }

        return false;
    }

    @Override
    public Iterator<Relationship> iterator() {
        return this;
    }
}
