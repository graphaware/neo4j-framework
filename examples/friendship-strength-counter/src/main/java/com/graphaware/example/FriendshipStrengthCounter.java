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

package com.graphaware.example;

import com.graphaware.tx.event.improved.api.Change;
import com.graphaware.tx.event.improved.api.ImprovedTransactionData;
import com.graphaware.tx.event.improved.api.LazyTransactionData;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.event.TransactionData;
import org.neo4j.graphdb.event.TransactionEventHandler;

import static com.graphaware.common.util.IterableUtils.getSingle;
import static org.neo4j.tooling.GlobalGraphOperations.at;

/**
 * Example of a Neo4j {@link org.neo4j.graphdb.event.TransactionEventHandler} that uses GraphAware {@link ImprovedTransactionData}
 * to do its job, which is counting the total strength of all friendships in the database and writing that to a special
 * node created for that purpose.
 */
public class FriendshipStrengthCounter extends TransactionEventHandler.Adapter<Void> {

    public static final RelationshipType FRIEND_OF = DynamicRelationshipType.withName("FRIEND_OF");
    public static final String STRENGTH = "strength";
    public static final String TOTAL_FRIENDSHIP_STRENGTH = "totalFriendshipStrength";
    public static final Label COUNTER_NODE_LABEL = DynamicLabel.label("FriendshipCounter");

    private final GraphDatabaseService database;

    public FriendshipStrengthCounter(GraphDatabaseService database) {
        this.database = database;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Void beforeCommit(TransactionData data) throws Exception {
        ImprovedTransactionData improvedTransactionData = new LazyTransactionData(data);

        int delta = 0;

        //handle new friendships
        for (Relationship newFriendship : improvedTransactionData.getAllCreatedRelationships()) {
            if (newFriendship.isType(FRIEND_OF)) {
                delta += (int) newFriendship.getProperty(STRENGTH, 0);
            }
        }

        //handle changed friendships
        for (Change<Relationship> changedFriendship : improvedTransactionData.getAllChangedRelationships()) {
            if (changedFriendship.getPrevious().isType(FRIEND_OF)) {
                delta -= (int) changedFriendship.getPrevious().getProperty(STRENGTH, 0);
                delta += (int) changedFriendship.getCurrent().getProperty(STRENGTH, 0);
            }
        }

        //handle deleted friendships
        for (Relationship deletedFriendship : improvedTransactionData.getAllDeletedRelationships()) {
            if (deletedFriendship.isType(FRIEND_OF)) {
                delta -= (int) deletedFriendship.getProperty(STRENGTH, 0);
            }
        }

        if (delta != 0) {
            Node root = getCounterNode(database);
            root.setProperty(TOTAL_FRIENDSHIP_STRENGTH, (int) root.getProperty(TOTAL_FRIENDSHIP_STRENGTH, 0) + delta);
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
        Node result = getSingle(at(database).getAllNodesWithLabel(COUNTER_NODE_LABEL));

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
    public static int getTotalFriendshipStrength(GraphDatabaseService database) {
        int result = 0;

        try (Transaction tx = database.beginTx()) {
            result = (int) getCounterNode(database).getProperty(TOTAL_FRIENDSHIP_STRENGTH, 0);
            tx.success();
        }

        return result;
    }
}
