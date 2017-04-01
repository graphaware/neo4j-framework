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

package com.graphaware.runtime.walk;

import com.graphaware.common.policy.inclusion.BaseNodeInclusionPolicy;
import com.graphaware.common.policy.inclusion.none.IncludeNoNodes;
import com.graphaware.tx.executor.batch.IterableInputBatchTransactionExecutor;
import com.graphaware.tx.executor.batch.NoInputBatchTransactionExecutor;
import com.graphaware.tx.executor.input.AllNodes;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.backup.OnlineBackupSettings;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.shell.ShellSettings;
import org.neo4j.test.TestGraphDatabaseFactory;

import static com.graphaware.common.util.DatabaseUtils.registerShutdownHook;
import static org.junit.Assert.*;
import static org.neo4j.kernel.configuration.Settings.*;

/**
 * Unit test for {@link RandomNodeSelector}.
 */
public class RandomNodeSelectorTest {

    private GraphDatabaseService database;

    @Before
    public void setUp() {
        database = new TestGraphDatabaseFactory()
                .newImpermanentDatabaseBuilder()
                .setConfig(OnlineBackupSettings.online_backup_enabled, FALSE)
                .setConfig(ShellSettings.remote_shell_enabled, FALSE)
                .newGraphDatabase();

        registerShutdownHook(database);
    }

    @After
    public void tearDown() {
        database.shutdown();
    }

    @Test
    public void shouldReturnNullOnEmptyDatabase() {
        try (Transaction tx = database.beginTx()) {
            assertNull(new RandomNodeSelector().selectNode(database));
            tx.success();
        }
    }

    @Test
    public void shouldReturnNullOnDatabaseWithAllNodesDeleted() {
        new NoInputBatchTransactionExecutor(database, 1000, 1000, (db, input, batchNumber, stepNumber) -> db.createNode()).execute();

        new IterableInputBatchTransactionExecutor<>(database, 1000, new AllNodes(database, 1000), (db, node, batchNumber, stepNumber) -> node.delete()).execute();

        try (Transaction tx = database.beginTx()) {
            assertNull(new RandomNodeSelector().selectNode(database));
            tx.success();
        }
    }

    @Test
    public void shouldReturnNullWhenNoNodeMatchesTheSpec() {
        new NoInputBatchTransactionExecutor(database, 1000, 1000, (db, input, batchNumber, stepNumber) -> db.createNode()).execute();

        try (Transaction tx = database.beginTx()) {
            assertNull(new RandomNodeSelector(IncludeNoNodes.getInstance()).selectNode(database));
            tx.success();
        }
    }

    @Test
    public void shouldReturnCorrectNode() {
        new NoInputBatchTransactionExecutor(database, 1000, 1000, (db, input, batchNumber, stepNumber) -> db.createNode(Label.label("Label" + (stepNumber % 10)))).execute();

        try (Transaction tx = database.beginTx()) {
            assertNotNull(new RandomNodeSelector().selectNode(database));

            Node node = new RandomNodeSelector(new BaseNodeInclusionPolicy() {
                @Override
                public boolean include(Node object) {
                    return object.hasLabel(Label.label("Label4"));
                }
            }).selectNode(database);

            assertNotNull(node);
            assertTrue(node.hasLabel(Label.label("Label4")));

            tx.success();
        }
    }

    @Test
    public void shouldReturnTheOnlyNode() {
        try (Transaction tx = database.beginTx()) {
            database.createNode();
            tx.success();
        }

        try (Transaction tx = database.beginTx()) {
            Node node = new RandomNodeSelector().selectNode(database);
            assertEquals(0, node.getId());
            tx.success();
        }
    }

    @Test
    public void shouldReturnTheOnlyRemainingNodeAfterTheRestHasBeenDeleted() {
        new NoInputBatchTransactionExecutor(database, 1000, 1000, (db, input, batchNumber, stepNumber) -> db.createNode()).execute();

        new IterableInputBatchTransactionExecutor<>(database, 1000, new AllNodes(database, 1000), (db, node, batchNumber, stepNumber) -> {
            if (stepNumber != 1000) {
                node.delete();
            }
        }).execute();

        try (Transaction tx = database.beginTx()) {
            Node node = new RandomNodeSelector().selectNode(database);
            assertEquals(999, node.getId());
            tx.success();
        }
    }
}
