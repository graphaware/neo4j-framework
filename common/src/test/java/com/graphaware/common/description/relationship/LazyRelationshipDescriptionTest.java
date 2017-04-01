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

package com.graphaware.common.description.relationship;

import com.graphaware.common.description.property.LazyPropertiesDescription;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;
import org.neo4j.test.TestGraphDatabaseFactory;

import static com.graphaware.common.util.DatabaseUtils.registerShutdownHook;
import static org.junit.Assert.assertEquals;
import static org.neo4j.graphdb.Direction.OUTGOING;
import static org.neo4j.graphdb.RelationshipType.*;

/**
 * Unit test for {@link com.graphaware.common.description.relationship.LazyRelationshipDescription}.
 */
public class LazyRelationshipDescriptionTest {

    private GraphDatabaseService database;

    @Before
    public void setUp() {
        database = new TestGraphDatabaseFactory().newImpermanentDatabase();
        registerShutdownHook(database);

        try (Transaction tx = database.beginTx()) {
            Node root = database.createNode();
            Node one = database.createNode();
            root.createRelationshipTo(one, withName("TEST")).setProperty("k", new int[]{2, 3, 4});
            tx.success();
        }
    }

    @After
    public void tearDown() {
        database.shutdown();
    }

    @Test
    public void shouldReturnCorrectTypeDirectionAndProps() {
        try (Transaction tx = database.beginTx()) {
            RelationshipDescription relationshipDescription = new LazyRelationshipDescription(
                    database.getNodeById(0).getSingleRelationship(withName("TEST"), OUTGOING),
                    database.getNodeById(0));

            assertEquals("TEST", relationshipDescription.getType());
            assertEquals(OUTGOING, relationshipDescription.getDirection());
            assertEquals(new LazyPropertiesDescription(database.getNodeById(0).getSingleRelationship(withName("TEST"), OUTGOING)), relationshipDescription.getPropertiesDescription());
        }
    }
}
