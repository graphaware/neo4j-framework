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

import com.graphaware.description.serialize.Serializer;
import com.graphaware.tx.executor.single.SimpleTransactionExecutor;
import com.graphaware.tx.executor.single.VoidReturningCallback;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.test.TestGraphDatabaseFactory;

import static org.junit.Assert.assertEquals;

/**
 *
 */
public class DetachedRelationshipDescriptionImplTest {

    private GraphDatabaseService database;

    @Before
    public void setUp() {
        database = new TestGraphDatabaseFactory().newImpermanentDatabase();

        new SimpleTransactionExecutor(database).executeInTransaction(new VoidReturningCallback() {
            @Override
            protected void doInTx(GraphDatabaseService database) {
                Node root = database.getNodeById(0);
                Node one = database.createNode();
                root.createRelationshipTo(one, DynamicRelationshipType.withName("TEST")).setProperty("k", new int[]{2,3,4});
            }
        });
    }

    @Test
    public void verifySerialization() {
        RelationshipDescription description = RelationshipDescriptionFactory.literal(database.getRelationshipById(0), database.getNodeById(0));
        String serialized = Serializer.serialize(description, "testPrefix");
        System.out.println(serialized);
        RelationshipDescription deserialized = Serializer.deserialize(serialized, DetachedRelationshipDescriptionImpl.class, "testPrefix");

        assertEquals(deserialized, description);
    }
}
