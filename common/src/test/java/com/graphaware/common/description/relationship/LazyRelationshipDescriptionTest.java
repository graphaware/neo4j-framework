/*
 * Copyright (c) 2013-2020 GraphAware
 *
 * This file is part of the GraphAware Framework.
 *
 * GraphAware Framework is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software Foundation, either
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
import com.graphaware.common.junit.InjectNeo4j;
import com.graphaware.common.junit.Neo4jExtension;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static org.neo4j.graphdb.Direction.OUTGOING;
import static org.neo4j.graphdb.RelationshipType.withName;

/**
 * Unit test for {@link com.graphaware.common.description.relationship.LazyRelationshipDescription}.
 */
@TestInstance(PER_CLASS)
@ExtendWith(Neo4jExtension.class)
public class LazyRelationshipDescriptionTest {

    @InjectNeo4j(lifecycle = InjectNeo4j.Lifecycle.CLASS)
    private GraphDatabaseService database;

    @BeforeEach
    private void populate() {
        try (Transaction tx = database.beginTx()) {
            Node root = tx.createNode();
            Node one = tx.createNode();
            root.createRelationshipTo(one, withName("TEST")).setProperty("k", new int[]{2, 3, 4});

            tx.commit();
        }
    }

    @Test
    public void shouldReturnCorrectTypeDirectionAndProps() {
        try (Transaction tx = database.beginTx()) {
            RelationshipDescription relationshipDescription = new LazyRelationshipDescription(
                    tx.getNodeById(0).getSingleRelationship(withName("TEST"), OUTGOING),
                    tx.getNodeById(0));

            assertEquals("TEST", relationshipDescription.getType());
            assertEquals(OUTGOING, relationshipDescription.getDirection());
            assertEquals(new LazyPropertiesDescription(tx.getNodeById(0).getSingleRelationship(withName("TEST"), OUTGOING)), relationshipDescription.getPropertiesDescription());
        }
    }
}
