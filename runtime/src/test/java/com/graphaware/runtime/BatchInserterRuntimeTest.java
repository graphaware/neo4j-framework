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

import com.graphaware.common.util.FakeTransaction;
import com.graphaware.common.util.IterableUtils;
import com.graphaware.runtime.config.DefaultRuntimeConfiguration;
import com.graphaware.runtime.config.NullTxDrivenModuleConfiguration;
import com.graphaware.runtime.config.TxDrivenModuleConfiguration;
import com.graphaware.runtime.metadata.BatchSingleNodeMetadataRepository;
import com.graphaware.runtime.metadata.ModuleMetadataRepository;
import com.graphaware.runtime.module.DeliberateTransactionRollbackException;
import com.graphaware.runtime.module.TxDrivenModule;
import com.graphaware.runtime.module.BatchSupportingTxDrivenModule;
import com.graphaware.tx.event.batch.api.TransactionSimulatingBatchInserter;
import com.graphaware.tx.event.batch.api.TransactionSimulatingBatchInserterImpl;
import com.graphaware.tx.event.batch.propertycontainer.inserter.BatchInserterNode;
import com.graphaware.tx.event.improved.api.ImprovedTransactionData;
import com.graphaware.tx.executor.single.SimpleTransactionExecutor;
import com.graphaware.tx.executor.single.VoidReturningCallback;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.helpers.collection.Iterables;
import org.neo4j.tooling.GlobalGraphOperations;
import org.neo4j.unsafe.batchinsert.BatchInserters;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;

import static com.graphaware.runtime.config.RuntimeConfiguration.GA_METADATA;
import static com.graphaware.runtime.config.RuntimeConfiguration.TX_MODULES_PROPERTY_PREFIX;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.doThrow;

/**
 * Unit test for {@link BatchInserterRuntime}.
 */
public class BatchInserterRuntimeTest extends GraphAwareRuntimeTest<BatchSupportingTxDrivenModule> {

    private final TemporaryFolder temporaryFolder = new TemporaryFolder();
    private TransactionSimulatingBatchInserter batchInserter;
    private ModuleMetadataRepository txRepo;

    @Before
    public void setUp() throws IOException {
        temporaryFolder.create();
        batchInserter = new TransactionSimulatingBatchInserterImpl(BatchInserters.inserter(temporaryFolder.getRoot().getAbsolutePath()), 1);
        txRepo = new BatchSingleNodeMetadataRepository(batchInserter, DefaultRuntimeConfiguration.getInstance(), TX_MODULES_PROPERTY_PREFIX);
    }

    @After
    public void tearDown() {
        temporaryFolder.delete();
    }

    @Override
    protected GraphAwareRuntime createRuntime() {
        return GraphAwareRuntimeFactory.createRuntime(batchInserter);
    }

    @Override
    protected Node createMetadataNode() {
        for (long candidate : batchInserter.getAllNodes()) {
            if (batchInserter.nodeHasLabel(candidate, GA_METADATA)) {
                throw new IllegalArgumentException("Runtime root already exists!");
            }
        }

        return new BatchInserterNode(batchInserter.createNode(new HashMap<String, Object>(), GA_METADATA), batchInserter);
    }

    @Override
    protected BatchSupportingTxDrivenModule mockTxModule() {
        return mockTxModule(MOCK);
    }

    @Override
    protected BatchSupportingTxDrivenModule mockTxModule(String id) {
        return mockTxModule(id, NullTxDrivenModuleConfiguration.getInstance());
    }

    @Override
    protected BatchSupportingTxDrivenModule mockTxModule(String id, TxDrivenModuleConfiguration configuration) {
        BatchSupportingTxDrivenModule mockModule = mock(BatchSupportingTxDrivenModule.class);
        when(mockModule.getId()).thenReturn(id);
        when(mockModule.getConfiguration()).thenReturn(configuration);
        when(mockModule.beforeCommit(any(ImprovedTransactionData.class))).thenReturn("TEST_" + id);
        return mockModule;
    }

    @Override
    protected Transaction getTransaction() {
        return new FakeTransaction();
    }

    @Override
    protected void verifyInitialization(BatchSupportingTxDrivenModule module) {
        verify(module).initialize(batchInserter);
    }

    @Override
    protected void verifyReinitialization(BatchSupportingTxDrivenModule module) {
        verify(module).reinitialize(batchInserter);
    }

    @Override
    protected void verifyStart(BatchSupportingTxDrivenModule module) {
        verify(module).start(batchInserter);
    }

    @Override
    protected Node createNode(Label... labels) {
        return new BatchInserterNode(batchInserter.createNode(Collections.<String, Object>emptyMap(), labels), batchInserter);
    }

    @Override
    protected void shutdown() {
        batchInserter.shutdown();
    }

    @Override
    protected ModuleMetadataRepository getTxRepo() {
        return txRepo;
    }

    @Override
    protected long countNodes() {
        return IterableUtils.count(batchInserter.getAllNodes());
    }

    @Test
    public void shouldCreateRuntimeMetadataNodeAfterFirstStartup() {
        GraphAwareRuntime runtime = GraphAwareRuntimeFactory.createRuntime(batchInserter);
        runtime.start();
        batchInserter.shutdown();

        final GraphDatabaseService database = new GraphDatabaseFactory().newEmbeddedDatabase(temporaryFolder.getRoot().getAbsolutePath());

        new SimpleTransactionExecutor(database).executeInTransaction(new VoidReturningCallback() {
            @Override
            protected void doInTx(GraphDatabaseService database) {
                assertEquals(1, Iterables.count(GlobalGraphOperations.at(database).getAllNodesWithLabel(GA_METADATA)));
            }
        });

        database.shutdown();
    }

    @Test
    public void shouldIgnoreNonCompatibleModules() {
        TxDrivenModule mockModule = mock(TxDrivenModule.class);

        GraphAwareRuntime runtime = createRuntime();
        runtime.registerModule(mockModule);

        runtime.start();

        verify(mockModule, atLeastOnce()).getId();
        verifyNoMoreInteractions(mockModule);
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
}
