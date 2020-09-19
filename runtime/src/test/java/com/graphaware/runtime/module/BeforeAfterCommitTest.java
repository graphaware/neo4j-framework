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

import com.graphaware.common.junit.InjectNeo4j;
import com.graphaware.common.junit.Neo4jExtension;
import com.graphaware.runtime.GraphAwareRuntime;
import com.graphaware.runtime.GraphAwareRuntimeFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.event.TransactionData;
import org.neo4j.graphdb.event.TransactionEventListenerAdapter;
import org.neo4j.harness.Neo4j;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(Neo4jExtension.class)
public class BeforeAfterCommitTest {

    @InjectNeo4j
    private Neo4j neo4j;

    @InjectNeo4j
    private GraphDatabaseService database;

    @Test
    public void afterCommitShouldBeCalled() {
        BeforeAfterCommitModule module = new BeforeAfterCommitModule("test", null);

        GraphAwareRuntime runtime = GraphAwareRuntimeFactory.createRuntime(neo4j.databaseManagementService(), database);
        runtime.registerModule(module);
        runtime.start();
        runtime.waitUntilStarted();

        try (Transaction tx = database.beginTx()) {
            tx.createNode();
            tx.commit();
        }

        assertTrue(module.isAfterCommitCalled());
        assertFalse(module.isAfterRollbackCalled());

        runtime.removeSelf();
    }

    @Test
    public void afterRollbackShouldBeCalled() {
        BeforeAfterCommitModule module = new BeforeAfterCommitModule("test", null);

        GraphAwareRuntime runtime = GraphAwareRuntimeFactory.createRuntime(neo4j.databaseManagementService(), database);
        runtime.registerModule(module);
        runtime.start();
        runtime.waitUntilStarted();

        TransactionEventListenerAdapter<Void> bang = new TransactionEventListenerAdapter<>() {
            @Override
            public Void beforeCommit(TransactionData data, Transaction transaction, GraphDatabaseService databaseService) {
                throw new RuntimeException("bang");
            }
        };

        neo4j.databaseManagementService().registerTransactionEventListener(database.databaseName(), bang);

        try (Transaction tx = database.beginTx()) {
            tx.createNode();
            tx.commit();
        } catch (Exception e) {
            //ok
        }

        assertFalse(module.isAfterCommitCalled());
        assertTrue(module.isAfterRollbackCalled());

        runtime.removeSelf();
        neo4j.databaseManagementService().unregisterTransactionEventListener(database.databaseName(), bang);
    }
}
