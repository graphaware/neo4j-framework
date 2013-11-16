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

package com.graphaware.tx.event.demo;

import com.graphaware.common.change.Change;
import com.graphaware.tx.event.improved.api.ImprovedTransactionData;
import com.graphaware.tx.event.improved.api.LazyTransactionData;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.event.TransactionData;
import org.neo4j.graphdb.event.TransactionEventHandler;

/**
 * Total friendship strength counter for documentation.
 */
public class TotalFriendshipStrengthCounter implements TransactionEventHandler<Void> {
    public static final RelationshipType FRIEND_OF = DynamicRelationshipType.withName("FRIEND_OF");
    public static final String STRENGTH = "strength";
    public static final String TOTAL_FRIENDSHIP_STRENGTH = "totalFriendshipStrength";

    private final GraphDatabaseService database;

    public TotalFriendshipStrengthCounter(GraphDatabaseService database) {
        this.database = database;
    }

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

        Node root = database.getNodeById(0);
        root.setProperty(TOTAL_FRIENDSHIP_STRENGTH, (int) root.getProperty(TOTAL_FRIENDSHIP_STRENGTH, 0) + delta);

        return null;
    }

    @Override
    public void afterCommit(TransactionData data, Void state) {
    }

    @Override
    public void afterRollback(TransactionData data, Void state) {
    }
}
