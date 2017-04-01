/*
 * Copyright (c) 2013-2017 GraphAware
 *
 * This file is part of the GraphAware Framework.
 *
 * GraphAware Framework is free software: you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received a copy of
 * the GNU General Public License along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 */

package com.graphaware.example;

import com.graphaware.common.util.Change;
import com.graphaware.common.util.IterableUtils;
import com.graphaware.tx.event.improved.api.ImprovedTransactionData;
import com.graphaware.tx.event.improved.api.LazyTransactionData;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.event.TransactionData;
import org.neo4j.graphdb.event.TransactionEventHandler;

/**
 * Example of a Neo4j {@link org.neo4j.graphdb.event.TransactionEventHandler} that uses GraphAware {@link ImprovedTransactionData}
 * to do its job, which is counting the total strength of all friendships in the database and writing that to a special
 * node created for that purpose.
 */
public class FriendshipStrengthCounter extends TransactionEventHandler.Adapter<Void> {

    public static final RelationshipType FRIEND_OF = RelationshipType.withName("FRIEND_OF");
    public static final String STRENGTH = "strength";
    public static final String TOTAL_FRIENDSHIP_STRENGTH = "totalFriendshipStrength";
    public static final Label COUNTER_NODE_LABEL = Label.label("FriendshipCounter");

    private final GraphDatabaseService database;

    public FriendshipStrengthCounter(GraphDatabaseService database) {
        this.database = database;
        try (Transaction tx = database.beginTx()) {
            getCounterNode(database); //do this in constructor to prevent multiple threads creating multiple nodes
            tx.success();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Void beforeCommit(TransactionData data) throws Exception {
        ImprovedTransactionData improvedTransactionData = new LazyTransactionData(data);

        long delta = 0;

        //handle new friendships
        for (Relationship newFriendship : improvedTransactionData.getAllCreatedRelationships()) {
            if (newFriendship.isType(FRIEND_OF)) {
                delta += (long) newFriendship.getProperty(STRENGTH, 0L);
            }
        }

        //handle changed friendships
        for (Change<Relationship> changedFriendship : improvedTransactionData.getAllChangedRelationships()) {
            if (changedFriendship.getPrevious().isType(FRIEND_OF)) {
                delta -= (long) changedFriendship.getPrevious().getProperty(STRENGTH, 0L);
                delta += (long) changedFriendship.getCurrent().getProperty(STRENGTH, 0L);
            }
        }

        //handle deleted friendships
        for (Relationship deletedFriendship : improvedTransactionData.getAllDeletedRelationships()) {
            if (deletedFriendship.isType(FRIEND_OF)) {
                delta -= (long) deletedFriendship.getProperty(STRENGTH, 0L);
            }
        }

        if (delta != 0) {
            Node counter = getCounterNode(database);

            try (Transaction tx = database.beginTx()) {
                tx.acquireWriteLock(counter);
                counter.setProperty(TOTAL_FRIENDSHIP_STRENGTH, (long) counter.getProperty(TOTAL_FRIENDSHIP_STRENGTH, 0L) + delta);
                tx.success();
            }
        }

        return null;
    }

    /**
     * Get the counter node, where the friendship strength is stored. Create it if it does not exist.
     *
     * @param database to find the node in.
     * @return counter node.
     */
    private static Node getCounterNode(GraphDatabaseService database) {
        Node result = IterableUtils.getSingleOrNull(database.findNodes(COUNTER_NODE_LABEL));

        if (result != null) {
            return result;
        }

        return database.createNode(COUNTER_NODE_LABEL);
    }

    /**
     * Get the counter value of the total friendship strength counter.
     *
     * @param database to find the counter in.
     * @return total friendship strength.
     */
    public static long getTotalFriendshipStrength(GraphDatabaseService database) {
        long result = 0;

        try (Transaction tx = database.beginTx()) {
            result = (long) getCounterNode(database).getProperty(TOTAL_FRIENDSHIP_STRENGTH, 0L);
            tx.success();
        }

        return result;
    }
}
