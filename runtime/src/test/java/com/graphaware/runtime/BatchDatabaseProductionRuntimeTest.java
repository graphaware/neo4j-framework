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

package com.graphaware.runtime;

import com.graphaware.runtime.config.DefaultRuntimeConfiguration;
import com.graphaware.runtime.metadata.ProductionSingleNodeMetadataRepository;
import com.graphaware.runtime.module.DeliberateTransactionRollbackException;
import com.graphaware.runtime.module.TxDrivenModule;
import com.graphaware.tx.event.improved.api.ImprovedTransactionData;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.unsafe.batchinsert.BatchInserters;
import org.neo4j.unsafe.batchinsert.TransactionSimulatingBatchGraphDatabase;

import java.io.IOException;

import static com.graphaware.runtime.config.RuntimeConfiguration.GA_METADATA;
import static com.graphaware.runtime.config.RuntimeConfiguration.TX_MODULES_PROPERTY_PREFIX;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;

/**
 * Unit test for {@link ProductionRuntime} used with batch graph database.
 */
public class BatchDatabaseProductionRuntimeTest extends DatabaseRuntimeTest {

    private final TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Before
    public void setUp() throws IOException {
        temporaryFolder.create();
        database = new TransactionSimulatingBatchGraphDatabase(BatchInserters.batchDatabase(temporaryFolder.getRoot().getAbsolutePath()), 1);
        txRepo = new ProductionSingleNodeMetadataRepository(database, DefaultRuntimeConfiguration.getInstance(), TX_MODULES_PROPERTY_PREFIX);
    }

    @After
    public void tearDown() {
        try {
            database.shutdown();
        } catch (IllegalStateException e) {
            //already shutdown = ok
        }

        temporaryFolder.delete();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void shouldNotBeAllowedToDeleteMetadataNode() {
        GraphAwareRuntime runtime = GraphAwareRuntimeFactory.createRuntime(database);
        runtime.start();

        getMetadataNode().delete();
    }

    @Test(expected = RuntimeException.class)
    public void moduleThrowingExceptionShouldFailTheImport() {
        TxDrivenModule mockModule = mockTxModule(MOCK);
        doThrow(new DeliberateTransactionRollbackException("Deliberate testing exception")).when(mockModule).beforeCommit(any(ImprovedTransactionData.class));

        GraphAwareRuntime runtime = createRuntime();
        runtime.registerModule(mockModule);
        runtime.start();

        try (Transaction tx = getTransaction()) {
            Node node = createNode();
            node.setProperty("test", "test");
            tx.success();
        }

        fail();
    }

    protected Node getMetadataNode() {
        Node root = null;

        try (Transaction tx = database.beginTx()) {
            //deliberately using deprecated API, do not attempt to fix, or at least run the test afterwards
            //noinspection deprecation
            for (Node node : database.getAllNodes()) {
                if (node.hasLabel(GA_METADATA)) {
                    root = node;
                    break;
                }
            }

            tx.success();
        }

        return root;
    }

    protected Node createMetadataNode() {
        Node root;

        try (Transaction tx = database.beginTx()) {
            if (getMetadataNode() != null) {
                throw new IllegalArgumentException("Runtime metadata node already exists!");
            }
            root = database.createNode(GA_METADATA);
            tx.success();
        }

        return root;
    }
}
