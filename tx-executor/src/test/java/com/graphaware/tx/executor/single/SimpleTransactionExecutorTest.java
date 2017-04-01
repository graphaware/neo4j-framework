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

package com.graphaware.tx.executor.single;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.backup.OnlineBackupSettings;
import org.neo4j.graphdb.*;
import org.neo4j.shell.ShellSettings;
import org.neo4j.test.TestGraphDatabaseFactory;

import static com.graphaware.common.util.DatabaseUtils.registerShutdownHook;
import static com.graphaware.common.util.IterableUtils.countNodes;
import static org.junit.Assert.assertEquals;
import static org.neo4j.kernel.configuration.Settings.FALSE;

/**
 * Unit test for {@link com.graphaware.tx.executor.single.SimpleTransactionExecutor}.
 */
public class SimpleTransactionExecutorTest {

    private GraphDatabaseService database;
    private TransactionExecutor executor;

    @Before
    public void setUp() {
        database = new TestGraphDatabaseFactory()
                .newImpermanentDatabaseBuilder()
                .setConfig(OnlineBackupSettings.online_backup_enabled, FALSE)
                .setConfig(ShellSettings.remote_shell_enabled, FALSE)
                .newGraphDatabase();

        registerShutdownHook(database);

        try (Transaction tx = database.beginTx()) {
            database.createNode();
            tx.success();
        }

        executor = new SimpleTransactionExecutor(database);
    }

    @After
    public void tearDown() {
        database.shutdown();
    }

    @Test
    public void nodeShouldBeSuccessfullyCreatedInTransaction() {
        executor.executeInTransaction(new VoidReturningCallback() {
            @Override
            public void doInTx(GraphDatabaseService database) {
                database.createNode();
            }
        });

        try (Transaction tx = database.beginTx()) {
            assertEquals(2, countNodes(database));
        }
    }

    @Test
    public void nodeShouldBeSuccessfullyDeletedInTransaction() {
        executor.executeInTransaction(new VoidReturningCallback() {
            @Override
            protected void doInTx(GraphDatabaseService database) {
                database.getNodeById(0).delete();
            }
        });

        try (Transaction tx = database.beginTx()) {
            assertEquals(0, countNodes(database));
        }
    }

    @Test(expected = ConstraintViolationException.class)
    public void deletingNodeWithRelationshipsShouldThrowException() {
        createNodeAndRelationship();

        executor.executeInTransaction(database -> {
            database.getNodeById(0).delete();
            return null;
        });
    }

    @Test
    public void deletingNodeWithRelationshipsShouldNotSucceed() {
        createNodeAndRelationship();

        try (Transaction tx = database.beginTx()) {
            assertEquals(2, countNodes(database));
        }

        executor.executeInTransaction(db -> {
            db.getNodeById(0).delete();
            return null;
        }, KeepCalmAndCarryOn.getInstance());

        try (Transaction tx = database.beginTx()) {
            assertEquals(2, countNodes(database));
        }
    }

    private void createNodeAndRelationship() {
        executor.executeInTransaction(db -> {
            Node node = db.createNode();
            node.createRelationshipTo(db.getNodeById(0), RelationshipType.withName("TEST_REL_TYPE"));
            return null;
        });
    }
}
