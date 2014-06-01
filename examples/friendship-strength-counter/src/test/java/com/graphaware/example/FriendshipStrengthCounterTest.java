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

import com.graphaware.common.strategy.IncludeAllNodes;
import com.graphaware.common.strategy.IncludeNoRelationships;
import com.graphaware.common.strategy.InclusionStrategies;
import com.graphaware.common.strategy.NodeInclusionStrategy;
import com.graphaware.tx.event.improved.api.FilteredTransactionData;
import com.graphaware.tx.event.improved.api.ImprovedTransactionData;
import com.graphaware.tx.event.improved.api.LazyTransactionData;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.event.TransactionData;
import org.neo4j.graphdb.event.TransactionEventHandler;
import org.neo4j.test.TestGraphDatabaseFactory;

import static com.graphaware.example.FriendshipStrengthCounter.*;
import static org.junit.Assert.assertEquals;


/**
 * Test for {@link FriendshipStrengthCounter}.
 */
public class FriendshipStrengthCounterTest {

    private GraphDatabaseService database;

    @Before
    public void setUp() {
        database = new TestGraphDatabaseFactory().newImpermanentDatabase();
        database.registerTransactionEventHandler(new FriendshipStrengthCounter(database));
    }

    @Test
    public void totalFriendshipStrengthShouldBeZeroInEmptyDatabase() {
        assertEquals(0, getTotalFriendshipStrength(database));
    }

    @Test
    public void totalFriendshipStrengthShouldBeCounted() {
        try (Transaction tx = database.beginTx()) {
            Node person1 = database.createNode();
            Node person2 = database.createNode();
            Node person3 = database.createNode();

            person1.setProperty("name", "Person One");
            person2.setProperty("name", "Person Two");
            person3.setProperty("name", "Person Three");

            person1.createRelationshipTo(person2, FRIEND_OF).setProperty(STRENGTH, 2);
            person1.createRelationshipTo(person3, FRIEND_OF).setProperty(STRENGTH, 1);
            person2.createRelationshipTo(person1, FRIEND_OF).setProperty(STRENGTH, 3);
            person2.createRelationshipTo(person3, FRIEND_OF).setProperty(STRENGTH, 1);
            person3.createRelationshipTo(person1, FRIEND_OF).setProperty(STRENGTH, 2);

            tx.success();
        }

        assertEquals(9, getTotalFriendshipStrength(database));

        //delete and change some friendships
        try (Transaction tx = database.beginTx()) {
            for (Relationship relationship : database.getNodeById(0).getRelationships(FRIEND_OF, Direction.OUTGOING)) {
                if (relationship.getEndNode().getId() == 1) {
                    relationship.delete(); //remove 2 from total strength
                } else {
                    relationship.setProperty(STRENGTH, 2); //add 1 to total strength
                }
            }

            tx.success();
        }

        assertEquals(8, getTotalFriendshipStrength(database));
    }
}
