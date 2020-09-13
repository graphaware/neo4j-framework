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

package com.graphaware.tx.executor.single;

import com.graphaware.common.junit.InjectNeo4j;
import com.graphaware.common.junit.Neo4jExtension;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.neo4j.graphdb.*;
import org.neo4j.harness.Neo4j;
import org.neo4j.harness.Neo4jBuilders;

import static com.graphaware.common.util.IterableUtils.countNodes;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit test for {@link com.graphaware.tx.executor.single.SimpleTransactionExecutor}.
 */
@ExtendWith(Neo4jExtension.class)
public class SimpleTransactionExecutorTest {

    @InjectNeo4j
    protected GraphDatabaseService database;

    private TransactionExecutor executor;
    private long id;

    @BeforeEach
    public void setUp() {
        try (Transaction tx = database.beginTx()) {
            id = tx.createNode().getId();
            tx.commit();
        }

        executor = new SimpleTransactionExecutor(database);
    }

    @Test
    public void nodeShouldBeSuccessfullyCreatedInTransaction() {
        executor.executeInTransaction(new VoidReturningCallback() {
            @Override
            public void doInTx(Transaction tx) {
                tx.createNode();
            }
        });

        try (Transaction tx = database.beginTx()) {
            assertEquals(2, countNodes(tx));
        }
    }

    @Test
    public void nodeShouldBeSuccessfullyDeletedInTransaction() {
        executor.executeInTransaction(new VoidReturningCallback() {
            @Override
            protected void doInTx(Transaction tx) {
                tx.getNodeById(id).delete();
            }
        });

        try (Transaction tx = database.beginTx()) {
            assertEquals(0, countNodes(tx));
        }
    }

    @Test
    public void deletingNodeWithRelationshipsShouldThrowException() {
        createNodeAndRelationship();

        assertThrows(ConstraintViolationException.class, () -> {
            executor.executeInTransaction(tx -> {
                tx.getNodeById(id).delete();
                return null;
            });
        });
    }

    @Test
    public void deletingNodeWithRelationshipsShouldNotSucceed() {
        createNodeAndRelationship();

        try (Transaction tx = database.beginTx()) {
            assertEquals(2, countNodes(tx));
        }

        executor.executeInTransaction(db -> {
            db.getNodeById(id).delete();
            return null;
        }, KeepCalmAndCarryOn.getInstance());

        try (Transaction tx = database.beginTx()) {
            assertEquals(2, countNodes(tx));
        }
    }

    private void createNodeAndRelationship() {
        executor.executeInTransaction(db -> {
            Node node = db.createNode();
            node.createRelationshipTo(db.getNodeById(id), RelationshipType.withName("TEST_REL_TYPE"));
            return null;
        });
    }
}
