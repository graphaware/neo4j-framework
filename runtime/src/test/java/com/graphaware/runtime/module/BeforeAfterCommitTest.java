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

package com.graphaware.runtime.module;

import com.graphaware.runtime.GraphAwareRuntime;
import com.graphaware.runtime.GraphAwareRuntimeFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.event.TransactionData;
import org.neo4j.graphdb.event.TransactionEventHandler;
import org.neo4j.harness.ServerControls;
import org.neo4j.harness.TestServerBuilders;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BeforeAfterCommitTest {

    private ServerControls controls;
    private GraphDatabaseService database;

    @BeforeEach
    public void setUp() {
        controls = TestServerBuilders.newInProcessBuilder().newServer();
        database = controls.graph();
    }

    @AfterEach
    public void tearDown() {
        controls.close();
    }

    @Test
    public void afterCommitShouldBeCalled() throws InterruptedException {
        BeforeAfterCommitModule module = new BeforeAfterCommitModule("test", null);

        GraphAwareRuntime runtime = GraphAwareRuntimeFactory.createRuntime(database);
        runtime.registerModule(module);
        runtime.start();
        runtime.waitUntilStarted();

        try (Transaction tx = database.beginTx()) {
            database.createNode();
            tx.success();
        }

        assertTrue(module.isAfterCommitCalled());
        assertFalse(module.isAfterRollbackCalled());
    }

    @Test
    public void afterRollbackShouldBeCalled() throws InterruptedException {
        BeforeAfterCommitModule module = new BeforeAfterCommitModule("test", null);

        GraphAwareRuntime runtime = GraphAwareRuntimeFactory.createRuntime(database);
        runtime.registerModule(module);
        runtime.start();
        runtime.waitUntilStarted();

        database.registerTransactionEventHandler(new TransactionEventHandler.Adapter<Void>() {
            @Override
            public Void beforeCommit(TransactionData data) throws Exception {
                throw new RuntimeException("bang");
            }
        });

        try (Transaction tx = database.beginTx()) {
            database.createNode();
            tx.success();
        } catch (Exception e) {
            //ok
        }

        assertFalse(module.isAfterCommitCalled());
        assertTrue(module.isAfterRollbackCalled());
    }
}
