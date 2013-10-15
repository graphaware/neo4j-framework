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

package com.graphaware.description.relationship;

import com.graphaware.description.property.LazyPropertiesDescription;
import com.graphaware.tx.executor.single.SimpleTransactionExecutor;
import com.graphaware.tx.executor.single.VoidReturningCallback;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.test.TestGraphDatabaseFactory;

import static org.junit.Assert.assertEquals;
import static org.neo4j.graphdb.Direction.OUTGOING;
import static org.neo4j.graphdb.DynamicRelationshipType.withName;

/**
 * Unit test for {@link LazyRelationshipDescription}.
 */
public class LazyRelationshipDescriptionTest {

    private GraphDatabaseService database;

    @Before
    public void setUp() {
        database = new TestGraphDatabaseFactory().newImpermanentDatabase();

        new SimpleTransactionExecutor(database).executeInTransaction(new VoidReturningCallback() {
            @Override
            protected void doInTx(GraphDatabaseService database) {
                Node root = database.getNodeById(0);
                Node one = database.createNode();
                root.createRelationshipTo(one, withName("TEST")).setProperty("k", new int[]{2, 3, 4});
            }
        });
    }

    @After
    public void tearDown() {
        database.shutdown();
    }

    @Test
    public void shouldReturnCorrectTypeDirectionAndProps() {
        RelationshipDescription relationshipDescription = new LazyRelationshipDescription(
                database.getNodeById(0).getSingleRelationship(withName("TEST"), OUTGOING),
                database.getNodeById(0));

        assertEquals("TEST", relationshipDescription.getType().name());
        assertEquals(OUTGOING, relationshipDescription.getDirection());
        assertEquals(new LazyPropertiesDescription(database.getNodeById(0).getSingleRelationship(withName("TEST"), OUTGOING)), relationshipDescription.getPropertiesDescription());
    }
}
