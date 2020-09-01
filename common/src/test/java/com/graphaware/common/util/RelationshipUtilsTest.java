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

package com.graphaware.common.util;

import com.graphaware.common.UnitTest;
import org.junit.jupiter.api.Test;
import org.neo4j.graphalgo.BasicEvaluationContext;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.NotFoundException;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;

import static com.graphaware.common.util.RelationshipUtils.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.neo4j.graphdb.Direction.*;
import static org.neo4j.graphdb.RelationshipType.withName;

/**
 * Unit test for {@link RelationshipUtils}.
 */
public class RelationshipUtilsTest extends UnitTest {

    @Override
    protected void populate(Transaction database) {
        Node node1 = database.createNode();
        Node node2 = database.createNode();
        node1.createRelationshipTo(node2, withName("TEST"));
    }

    @Test
    public void nonExistingRelationshipShouldBeCorrectlyIdentified() {
        try (Transaction tx = database.beginTx()) {
            Node node1 = tx.getNodeById(0);
            Node node2 = tx.getNodeById(1);

            assertTrue(relationshipNotExists(new BasicEvaluationContext(tx, database), node2, node1, withName("TEST"), OUTGOING));
            assertTrue(relationshipNotExists(new BasicEvaluationContext(tx, database), node2, node1, withName("IDONTEXIST"), INCOMING));
            assertFalse(relationshipNotExists(new BasicEvaluationContext(tx, database), node2, node1, withName("TEST"), INCOMING));
            assertFalse(relationshipNotExists(new BasicEvaluationContext(tx, database), node2, node1, withName("TEST"), BOTH));

            tx.commit();
        }
    }

    @Test
    public void nonExistingRelationshipShouldBeCorrectlyIdentified2() {
        try (Transaction tx = database.beginTx()) {
            Node node1 = tx.getNodeById(0);
            Node node2 = tx.getNodeById(1);

            assertThrows(NotFoundException.class, () -> {
                getSingleRelationship(new BasicEvaluationContext(tx, database), node2, node1, withName("IDONTEXIST"), INCOMING);
            });

            tx.commit();
        }
    }

    @Test
    public void existingRelationshipShouldNotBeRecreated() {
        try (Transaction tx = database.beginTx()) {
            Node node1 = tx.getNodeById(0);
            Node node2 = tx.getNodeById(1);

            Relationship r = createRelationshipIfNotExists(new BasicEvaluationContext(tx, database), node1, node2, withName("TEST"), OUTGOING);
            assertEquals(0, r.getId());
            assertEquals(1, IterableUtils.count(tx.getAllRelationships()));

            tx.commit();
        }
    }

    @Test
    public void nonExistingRelationshipShouldBeCreated() {
        try (Transaction tx = database.beginTx()) {
            Node node1 = tx.getNodeById(0);
            Node node2 = tx.getNodeById(1);

            Relationship r = createRelationshipIfNotExists(new BasicEvaluationContext(tx, database), node2, node1, withName("TEST"), OUTGOING);
            assertEquals(2, IterableUtils.count(tx.getAllRelationships()));

            assertTrue(relationshipExists(new BasicEvaluationContext(tx, database), node2, node1, withName("TEST"), OUTGOING));

            tx.commit();
        }
    }

    @Test
    public void nonExistingRelationshipShouldBeCreated2() {
        try (Transaction tx = database.beginTx()) {
            Node node1 = tx.getNodeById(0);
            Node node2 = tx.getNodeById(1);

            Relationship r = createRelationshipIfNotExists(new BasicEvaluationContext(tx, database), node1, node2, withName("TEST"), INCOMING);
            assertEquals(2, IterableUtils.count(tx.getAllRelationships()));

            assertTrue(relationshipExists(new BasicEvaluationContext(tx, database), node1, node2, withName("TEST"), INCOMING));

            tx.commit();
        }
    }

    @Test
    public void existingRelationshipShouldBeDeleted() {
        try (Transaction tx = database.beginTx()) {
            Node node1 = tx.getNodeById(0);
            Node node2 = tx.getNodeById(1);

            deleteRelationshipIfExists(new BasicEvaluationContext(tx, database), node1, node2, withName("TEST"), OUTGOING);
            assertEquals(0, IterableUtils.count(tx.getAllRelationships()));

            tx.commit();
        }
    }

    @Test
    public void nonExistingRelationshipDeletionShouldDoNothing() {
        try (Transaction tx = database.beginTx()) {
            Node node1 = tx.getNodeById(0);
            Node node2 = tx.getNodeById(1);

            deleteRelationshipIfExists(new BasicEvaluationContext(tx, database), node2, node1, withName("TEST"), OUTGOING);
            assertEquals(1, IterableUtils.count(tx.getAllRelationships()));

            tx.commit();
        }
    }
}
