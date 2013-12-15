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

import com.graphaware.tx.executor.single.SimpleTransactionExecutor;
import com.graphaware.tx.executor.single.VoidReturningCallback;
import org.junit.Test;
import org.neo4j.graphdb.*;
import org.neo4j.test.TestGraphDatabaseFactory;

import static com.graphaware.tx.event.demo.TotalFriendshipStrengthCounter.*;
import static org.junit.Assert.assertEquals;

/**
 * Just for documentation.
 */
public class TotalFriendshipStrengthCountingDemo {

    @Test
    public void demonstrateTotalFriendshipStrengthCounting() {
        GraphDatabaseService database = new TestGraphDatabaseFactory().newImpermanentDatabase();
        database.registerTransactionEventHandler(new TotalFriendshipStrengthCounter(database));

        new SimpleTransactionExecutor(database).executeInTransaction(new VoidReturningCallback() {
            @Override
            protected void doInTx(GraphDatabaseService database) {
                database.createNode(); //ID = 0

                Node person1 = database.createNode();
                Node person2 = database.createNode();
                Node person3 = database.createNode();

                person1.setProperty("name", "Person One");
                person2.setProperty("name", "Person Two");
                person3.setProperty("name", "Person Three");

                Relationship friendship1 = person1.createRelationshipTo(person2, FRIEND_OF);
                friendship1.setProperty(STRENGTH, 2);

                Relationship friendship2 = person1.createRelationshipTo(person3, FRIEND_OF);
                friendship2.setProperty(STRENGTH, 1);

                Relationship friendship3 = person2.createRelationshipTo(person1, FRIEND_OF);
                friendship3.setProperty(STRENGTH, 3);

                Relationship friendship4 = person2.createRelationshipTo(person3, FRIEND_OF);
                friendship4.setProperty(STRENGTH, 1);

                Relationship friendship5 = person3.createRelationshipTo(person1, FRIEND_OF);
                friendship5.setProperty(STRENGTH, 2);
            }
        });

        try (Transaction tx = database.beginTx()) {
            assertEquals(9, database.getNodeById(0).getProperty(TOTAL_FRIENDSHIP_STRENGTH));
        }

        //delete and change some friendships
        new SimpleTransactionExecutor(database).executeInTransaction(new VoidReturningCallback() {
            @Override
            protected void doInTx(GraphDatabaseService database) {
                for (Relationship relationship : database.getNodeById(1).getRelationships(FRIEND_OF, Direction.OUTGOING)) {
                    if (relationship.getEndNode().getId() == 2) {
                        relationship.delete(); //remove 2 from total strength
                    } else {
                        relationship.setProperty(STRENGTH, 2); //add 1 to total strength
                    }
                }
            }
        });

        try (Transaction tx = database.beginTx()) {
            assertEquals(8, database.getNodeById(0).getProperty(TOTAL_FRIENDSHIP_STRENGTH));
        }
    }
}
