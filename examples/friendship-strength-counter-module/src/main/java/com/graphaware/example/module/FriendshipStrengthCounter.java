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

package com.graphaware.example.module;

import com.graphaware.tx.event.improved.api.Change;
import com.graphaware.tx.event.improved.api.ImprovedTransactionData;
import com.graphaware.tx.event.improved.api.LazyTransactionData;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.event.TransactionData;
import org.neo4j.graphdb.event.TransactionEventHandler;

import static com.graphaware.common.util.IterableUtils.getSingle;
import static com.graphaware.example.module.Labels.*;
import static com.graphaware.example.module.PropertyKeys.*;
import static com.graphaware.example.module.Relationships.*;
import static org.neo4j.tooling.GlobalGraphOperations.at;

/**
 * Example of a Neo4j {@link org.neo4j.graphdb.event.TransactionEventHandler} that uses GraphAware {@link ImprovedTransactionData}
 * to do its job, which is counting the total strength of all friendships in the database and writing that to a special
 * node created for that purpose.
 */
public class FriendshipStrengthCounter {

    private final GraphDatabaseService database;

    public FriendshipStrengthCounter(GraphDatabaseService database) {
        this.database = database;
    }

    public void handleCreatedFriendships(Iterable<Relationship> createdRelationships) {
        long delta = 0L;

        for (Relationship newFriendship : createdRelationships) {
            if (newFriendship.isType(FRIEND_OF)) {
                delta += (long) newFriendship.getProperty(STRENGTH, 0L);
            }
        }

        applyDelta(delta);
    }

    public void handleChangedFriendships(Iterable<Change<Relationship>> changedRelationships) {
        long delta = 0L;

        for (Change<Relationship> changedFriendship : changedRelationships) {
            if (changedFriendship.getPrevious().isType(FRIEND_OF)) {
                delta -= (long) changedFriendship.getPrevious().getProperty(STRENGTH, 0L);
                delta += (long) changedFriendship.getCurrent().getProperty(STRENGTH, 0L);
            }
        }

        applyDelta(delta);
    }

    public void handleDeletedFriendships(Iterable<Relationship> deletedRelationships) {
        long delta = 0L;

        for (Relationship deletedFriendship : deletedRelationships) {
            if (deletedFriendship.isType(FRIEND_OF)) {
                delta -= (long) deletedFriendship.getProperty(STRENGTH, 0L);
            }
        }

        applyDelta(delta);
    }

    private void applyDelta(long delta) {
        if (delta != 0L) {
            Node root = getCounterNode(database);
            root.setProperty(TOTAL_FRIENDSHIP_STRENGTH, (long) root.getProperty(TOTAL_FRIENDSHIP_STRENGTH, 0L) + delta);
        }
    }

    /**
     * Get the counter node, where the friendship strength is stored. Create it if it does not exist.
     *
     * @param database to find the node in.
     * @return counter node.
     */
    private static Node getCounterNode(GraphDatabaseService database) {
        Node result = getSingle(at(database).getAllNodesWithLabel(FriendshipCounter));

        if (result != null) {
            return result;
        }

        return database.createNode(FriendshipCounter);
    }

    /**
     * Get the counter value of the total friendship strength counter.
     *
     * @return total friendship strength.
     */
    public long getTotalFriendshipStrength() {
        long result = 0L;

        try (Transaction tx = database.beginTx()) {
            result = (long) getCounterNode(database).getProperty(TOTAL_FRIENDSHIP_STRENGTH, 0L);
            tx.success();
        }

        return result;
    }
}
