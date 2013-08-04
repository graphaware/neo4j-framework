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

package com.graphaware.propertycontainer.dto;

import com.graphaware.tx.executor.single.SimpleTransactionExecutor;
import com.graphaware.tx.executor.single.VoidReturningCallback;
import org.junit.After;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.test.TestGraphDatabaseFactory;

import static org.neo4j.graphdb.DynamicRelationshipType.withName;

public abstract class AbstractTest {

    protected GraphDatabaseService database;

    protected void setUp() {
        database = new TestGraphDatabaseFactory().newImpermanentDatabase();
        new SimpleTransactionExecutor(database).executeInTransaction(new VoidReturningCallback() {
            @Override
            public void doInTx(GraphDatabaseService database) {
                Node node1 = database.createNode();
                node1.setProperty("prop1", "value1");
                node1.setProperty("prop2", 2);

                Node node2 = database.createNode();
                Relationship relationship = node1.createRelationshipTo(node2, withName("test"));
                relationship.setProperty("prop3", 10000434132L);
                relationship.setProperty("prop4", new long[]{3L, 4L, 5L});

                node2.createRelationshipTo(node2, withName("cycle"));

                additionalSetup();
            }
        });
    }

    protected void additionalSetup() {
        //for subclasses to override.
    }

    @After
    public void tearDown() {
        if (database != null) {
            database.shutdown();
            database = null;
        }
    }
}
