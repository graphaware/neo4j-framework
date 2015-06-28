/*
 * Copyright (c) 2015 GraphAware
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

package com.graphaware.tx.event.improved;

import org.junit.Ignore;
import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.event.PropertyEntry;
import org.neo4j.graphdb.event.TransactionData;
import org.neo4j.graphdb.event.TransactionEventHandler;
import org.neo4j.test.TestGraphDatabaseFactory;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class BugTest {

    @Test
    @Ignore
    public void demonstrateBug() {
        final GraphDatabaseService db = new TestGraphDatabaseFactory().newImpermanentDatabase();

        db.execute("CREATE (m:Person {name:'Michal'})-[:FRIEND_OF]->(l:Person {name:'Luanne'}), " +
                "(m)-[:LIVES_IN {since:2010}]->(c:City {name:'London'})");


        final Set<String> changedRelationships = new HashSet<>();

        db.registerTransactionEventHandler(new TransactionEventHandler.Adapter<Void>() {
            @Override
            public Void beforeCommit(TransactionData data) throws Exception {
                for (PropertyEntry<Relationship> entry : data.assignedRelationshipProperties()) {
                    changedRelationships.add(entry.entity().getType().name());
                }

                return null;
            }
        });

        db.execute("MATCH (m:Person {name:'Michal'})-[r:LIVES_IN]->() SET r.since = 2009");

        assertEquals(1, changedRelationships.size());
        assertTrue(changedRelationships.contains("LIVES_IN")); //fails, contains FRIEND_OF!!!
    }

    @Test
    public void demonstrateBugWorkaround() {
        final GraphDatabaseService db = new TestGraphDatabaseFactory().newImpermanentDatabase();

        db.execute("CREATE (m:Person {name:'Michal'})-[:FRIEND_OF]->(l:Person {name:'Luanne'}), " +
                "(m)-[:LIVES_IN {since:2010}]->(c:City {name:'London'})");


        final Set<String> changedRelationships = new HashSet<>();

        db.registerTransactionEventHandler(new TransactionEventHandler.Adapter<Void>() {
            @Override
            public Void beforeCommit(TransactionData data) throws Exception {
                for (PropertyEntry<Relationship> entry : data.assignedRelationshipProperties()) {
                    Relationship reloaded = db.getRelationshipById(entry.entity().getId());
                    changedRelationships.add(reloaded.getType().name());
                }

                return null;
            }
        });

        db.execute("MATCH (m:Person {name:'Michal'})-[r:LIVES_IN]->() SET r.since = 2009");

        assertEquals(1, changedRelationships.size());
        assertTrue(changedRelationships.contains("LIVES_IN"));
    }

    @Test
    @Ignore
    public void demonstrateBug2() {
        final GraphDatabaseService db = new TestGraphDatabaseFactory().newImpermanentDatabase();

        db.execute("CREATE (m:Person {name:'Michal'})-[:FRIEND_OF]->(l:Person {name:'Luanne'}), " +
                "(m)-[:LIVES_IN {since:2010}]->(c:City {name:'London'})");


        final Set<String> deletedRelationships = new HashSet<>();

        db.registerTransactionEventHandler(new TransactionEventHandler.Adapter<Void>() {
            @Override
            public Void beforeCommit(TransactionData data) throws Exception {
                for (Relationship r : data.deletedRelationships()) {
                    deletedRelationships.add(r.getType().name());
                }

                return null;
            }
        });

        db.execute("MATCH (m:Person {name:'Michal'})-[r:LIVES_IN]->() DELETE r");

        assertEquals(1, deletedRelationships.size());
        assertTrue(deletedRelationships.contains("LIVES_IN")); //fails, contains FRIEND_OF!!!
    }

    @Test
    @Ignore
    public void demonstrateBug2Workaround() {
        final GraphDatabaseService db = new TestGraphDatabaseFactory().newImpermanentDatabase();

        db.execute("CREATE (m:Person {name:'Michal'})-[:FRIEND_OF]->(l:Person {name:'Luanne'}), " +
                "(m)-[:LIVES_IN {since:2010}]->(c:City {name:'London'})");


        final Set<String> deletedRelationships = new HashSet<>();

        db.registerTransactionEventHandler(new TransactionEventHandler.Adapter<Void>() {
            @Override
            public Void beforeCommit(TransactionData data) throws Exception {
                for (Relationship r : data.deletedRelationships()) {
                    Relationship reloaded = db.getRelationshipById(r.getId()); //fails, can't reload any more. Lost info about the relationship's type
                    deletedRelationships.add(reloaded.getType().name());
                }

                return null;
            }
        });

        db.execute("MATCH (m:Person {name:'Michal'})-[r:LIVES_IN]->() DELETE r");

        assertEquals(1, deletedRelationships.size());
        assertTrue(deletedRelationships.contains("LIVES_IN"));
    }
}
