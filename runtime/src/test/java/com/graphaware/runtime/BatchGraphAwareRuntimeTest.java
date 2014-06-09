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

import com.graphaware.common.serialize.Serializer;
import com.graphaware.common.strategy.InclusionStrategies;
import com.graphaware.runtime.config.DefaultRuntimeConfiguration;
import com.graphaware.runtime.config.MinimalRuntimeModuleConfiguration;
import com.graphaware.runtime.config.NullRuntimeModuleConfiguration;
import com.graphaware.runtime.strategy.BatchSupportingGraphAwareRuntimeModule;
import com.graphaware.tx.event.batch.api.TransactionSimulatingBatchInserter;
import com.graphaware.tx.event.batch.api.TransactionSimulatingBatchInserterImpl;
import com.graphaware.tx.event.improved.api.ImprovedTransactionData;
import com.graphaware.tx.executor.single.SimpleTransactionExecutor;
import com.graphaware.tx.executor.single.VoidReturningCallback;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mockito;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.helpers.collection.Iterables;
import org.neo4j.tooling.GlobalGraphOperations;
import org.neo4j.unsafe.batchinsert.BatchInserters;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;

import static com.graphaware.common.util.IterableUtils.count;
import static com.graphaware.runtime.ProductionGraphAwareRuntime.*;
import static com.graphaware.runtime.config.RuntimeConfiguration.GA_PREFIX;
import static com.graphaware.runtime.config.RuntimeConfiguration.GA_ROOT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

/**
 * Unit test for {@link ProductionGraphAwareRuntime}.
 */
public class BatchGraphAwareRuntimeTest extends GraphAwareRuntimeTest {

    private final TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Before
    public void setUp() throws IOException {
        temporaryFolder.create();
    }

    @After
    public void tearDown() {
        temporaryFolder.delete();
    }

    @Test
    public void shouldCreateRuntimeRootNodeAfterFirstStartup() {
        TransactionSimulatingBatchInserterImpl batchInserter = new TransactionSimulatingBatchInserterImpl(BatchInserters.inserter(temporaryFolder.getRoot().getAbsolutePath()));
        BatchGraphAwareRuntime runtime = new BatchGraphAwareRuntime(batchInserter);
        runtime.start();
        batchInserter.shutdown();

        final GraphDatabaseService database = new GraphDatabaseFactory().newEmbeddedDatabase(temporaryFolder.getRoot().getAbsolutePath());

        new SimpleTransactionExecutor(database).executeInTransaction(new VoidReturningCallback() {
            @Override
            protected void doInTx(GraphDatabaseService database) {
                assertEquals(1, Iterables.count(GlobalGraphOperations.at(database).getAllNodesWithLabel(GA_ROOT)));
            }
        });

        database.shutdown();
    }

    @Test
    public void moduleRegisteredForTheFirstTimeShouldBeInitialized() {
        final BatchSupportingGraphAwareRuntimeModule mockModule = createBatchSupportingMockModule();

        TransactionSimulatingBatchInserter batchInserter = new TransactionSimulatingBatchInserterImpl(BatchInserters.inserter(temporaryFolder.getRoot().getAbsolutePath()));
        BatchGraphAwareRuntime runtime = new BatchGraphAwareRuntime(batchInserter);
        runtime.registerModule(mockModule);

        runtime.start();

        verify(mockModule).initialize(batchInserter);
        verify(mockModule, atLeastOnce()).getConfiguration();
        verify(mockModule, atLeastOnce()).getId();
        verifyNoMoreInteractions(mockModule);

        assertEquals(Serializer.toString(NullRuntimeModuleConfiguration.getInstance(), CONFIG), batchInserter.getNodeProperties(0).get(GA_PREFIX + RUNTIME + "_" + MOCK).toString());
    }

    @Test
    public void moduleAlreadyRegisteredShouldNotBeInitialized() {
        final GraphAwareRuntimeModule mockModule = createMockModule();

        TransactionSimulatingBatchInserter batchInserter = new TransactionSimulatingBatchInserterImpl(BatchInserters.inserter(temporaryFolder.getRoot().getAbsolutePath()));
        long root = batchInserter.createNode(Collections.<String, Object>singletonMap(GA_PREFIX + RUNTIME + "_" + MOCK, Serializer.toString(NullRuntimeModuleConfiguration.getInstance(), CONFIG)), GA_ROOT);

        BatchGraphAwareRuntime runtime = new BatchGraphAwareRuntime(batchInserter);
        runtime.registerModule(mockModule);

        runtime.start();

        verify(mockModule, atLeastOnce()).getId();
        verify(mockModule, atLeastOnce()).getConfiguration();
        verifyNoMoreInteractions(mockModule);

        assertEquals(Serializer.toString(NullRuntimeModuleConfiguration.getInstance(), CONFIG), batchInserter.getNodeProperties(root).get(GA_PREFIX + RUNTIME + "_" + MOCK).toString());
    }

    @Test
    public void changedModuleShouldBeReInitialized() {
        final BatchSupportingGraphAwareRuntimeModule mockModule = createBatchSupportingMockModule();

        TransactionSimulatingBatchInserter batchInserter = new TransactionSimulatingBatchInserterImpl(BatchInserters.inserter(temporaryFolder.getRoot().getAbsolutePath()));
        long root = batchInserter.createNode(Collections.<String, Object>singletonMap(GA_PREFIX + RUNTIME + "_" + MOCK, CONFIG + "123"), GA_ROOT);

        BatchGraphAwareRuntime runtime = new BatchGraphAwareRuntime(batchInserter);
        runtime.registerModule(mockModule);

        runtime.start();

        verify(mockModule).reinitialize(batchInserter);
        verify(mockModule, atLeastOnce()).getConfiguration();
        verify(mockModule, atLeastOnce()).getId();
        verifyNoMoreInteractions(mockModule);

        assertEquals(Serializer.toString(NullRuntimeModuleConfiguration.getInstance(), CONFIG), batchInserter.getNodeProperties(root).get(GA_PREFIX + RUNTIME + "_" + MOCK).toString());
    }

    @Test
    public void forcedModuleShouldBeReInitialized() {
        final BatchSupportingGraphAwareRuntimeModule mockModule = createBatchSupportingMockModule();

        TransactionSimulatingBatchInserter batchInserter = new TransactionSimulatingBatchInserterImpl(BatchInserters.inserter(temporaryFolder.getRoot().getAbsolutePath()));
        long root = batchInserter.createNode(new HashMap<String, Object>(), GA_ROOT);
        batchInserter.setNodeProperty(root, GA_PREFIX + RUNTIME + "_" + MOCK, FORCE_INITIALIZATION + "123");

        BatchGraphAwareRuntime runtime = new BatchGraphAwareRuntime(batchInserter);
        runtime.registerModule(mockModule);

        runtime.start();

        verify(mockModule).reinitialize(batchInserter);
        verify(mockModule, atLeastOnce()).getConfiguration();
        verify(mockModule, atLeastOnce()).getId();
        verifyNoMoreInteractions(mockModule);

        assertEquals(Serializer.toString(NullRuntimeModuleConfiguration.getInstance(), CONFIG), batchInserter.getNodeProperties(root).get(GA_PREFIX + RUNTIME + "_" + MOCK).toString());
    }

    @Test
    public void moduleAlreadyRegisteredShouldBeInitializedWhenForced() {
        final BatchSupportingGraphAwareRuntimeModule mockModule = createBatchSupportingMockModule();

        TransactionSimulatingBatchInserter batchInserter = new TransactionSimulatingBatchInserterImpl(BatchInserters.inserter(temporaryFolder.getRoot().getAbsolutePath()));
        long root = batchInserter.createNode(Collections.<String, Object>singletonMap(GA_PREFIX + RUNTIME + "_" + MOCK, Serializer.toString(NullRuntimeModuleConfiguration.getInstance(), CONFIG)), GA_ROOT);

        BatchGraphAwareRuntime runtime = new BatchGraphAwareRuntime(batchInserter);
        runtime.registerModule(mockModule, true);

        runtime.start();

        verify(mockModule).reinitialize(batchInserter);
        verify(mockModule, atLeastOnce()).getConfiguration();
        verify(mockModule, atLeastOnce()).getId();
        verifyNoMoreInteractions(mockModule);

        assertEquals(Serializer.toString(NullRuntimeModuleConfiguration.getInstance(), CONFIG), batchInserter.getNodeProperties(root).get(GA_PREFIX + RUNTIME + "_" + MOCK).toString());
    }

    @Test
    public void changedModuleShouldNotBeReInitializedWhenInitializationSkipped() {
        final GraphAwareRuntimeModule mockModule = createMockModule();

        TransactionSimulatingBatchInserter batchInserter = new TransactionSimulatingBatchInserterImpl(BatchInserters.inserter(temporaryFolder.getRoot().getAbsolutePath()));
        long root = batchInserter.createNode(Collections.<String, Object>singletonMap(GA_PREFIX + RUNTIME + "_" + MOCK, CONFIG + "123"), GA_ROOT);

        BatchGraphAwareRuntime runtime = new BatchGraphAwareRuntime(batchInserter);
        runtime.registerModule(mockModule);

        runtime.start(true);

        verify(mockModule, atLeastOnce()).getId();
        verifyNoMoreInteractions(mockModule);

        assertEquals(CONFIG + "123", batchInserter.getNodeProperties(root).get(GA_PREFIX + RUNTIME + "_" + MOCK).toString());
    }

    @Test(expected = IllegalStateException.class)
    public void shouldNotBeAbleToRegisterTheSameModuleTwice() {
        final GraphAwareRuntimeModule mockModule = createMockModule();

        TransactionSimulatingBatchInserter batchInserter = new TransactionSimulatingBatchInserterImpl(BatchInserters.inserter(temporaryFolder.getRoot().getAbsolutePath()));
        BatchGraphAwareRuntime runtime = new BatchGraphAwareRuntime(batchInserter);
        runtime.registerModule(mockModule);
        runtime.registerModule(mockModule);
    }

    @Test(expected = IllegalStateException.class)
    public void shouldNotBeAbleToRegisterModuleWithTheSameIdTwice() {
        final GraphAwareRuntimeModule mockModule1 = createMockModule();
        final GraphAwareRuntimeModule mockModule2 = createMockModule();
        when(mockModule1.getId()).thenReturn("ID");
        when(mockModule2.getId()).thenReturn("ID");

        TransactionSimulatingBatchInserter batchInserter = new TransactionSimulatingBatchInserterImpl(BatchInserters.inserter(temporaryFolder.getRoot().getAbsolutePath()));
        BatchGraphAwareRuntime runtime = new BatchGraphAwareRuntime(batchInserter);
        runtime.registerModule(mockModule1);
        runtime.registerModule(mockModule2);
    }

    @Test
    public void unusedModulesShouldBeRemoved() {
        final GraphAwareRuntimeModule mockModule = createMockModule();

        TransactionSimulatingBatchInserter batchInserter = new TransactionSimulatingBatchInserterImpl(BatchInserters.inserter(temporaryFolder.getRoot().getAbsolutePath()));
        long root = batchInserter.createNode(Collections.<String, Object>emptyMap(), GA_ROOT);
        batchInserter.setNodeProperty(root, GA_PREFIX + RUNTIME + "_" + MOCK, Serializer.toString(NullRuntimeModuleConfiguration.getInstance(), CONFIG));
        batchInserter.setNodeProperty(root, GA_PREFIX + RUNTIME + "_UNUSED", CONFIG + "123");

        BatchGraphAwareRuntime runtime = new BatchGraphAwareRuntime(batchInserter);
        runtime.registerModule(mockModule);

        runtime.start();

        verify(mockModule, atLeastOnce()).getId();
        verify(mockModule, atLeastOnce()).getConfiguration();
        verifyNoMoreInteractions(mockModule);

        assertEquals(1, count(batchInserter.getNodeProperties(root).keySet()));
    }

    @Test(expected = IllegalStateException.class)
    public void usedCorruptModulesShouldThrowException() {
        final GraphAwareRuntimeModule mockModule = createMockModule();

        TransactionSimulatingBatchInserter batchInserter = new TransactionSimulatingBatchInserterImpl(BatchInserters.inserter(temporaryFolder.getRoot().getAbsolutePath()));
        batchInserter.createNode(Collections.<String, Object>singletonMap(GA_PREFIX + RUNTIME + "_" + MOCK, "CORRUPT"), GA_ROOT);

        BatchGraphAwareRuntime runtime = new BatchGraphAwareRuntime(batchInserter);
        runtime.registerModule(mockModule);

        runtime.start();
    }

    @Test
    public void unusedCorruptModulesShouldBeRemoved() {
        final GraphAwareRuntimeModule mockModule = createMockModule();

        TransactionSimulatingBatchInserter batchInserter = new TransactionSimulatingBatchInserterImpl(BatchInserters.inserter(temporaryFolder.getRoot().getAbsolutePath()));
        long root = batchInserter.createNode(Collections.<String, Object>emptyMap(), GA_ROOT);
        batchInserter.setNodeProperty(root, GA_PREFIX + RUNTIME + "_" + MOCK, Serializer.toString(NullRuntimeModuleConfiguration.getInstance(), CONFIG));
        batchInserter.setNodeProperty(root, GA_PREFIX + RUNTIME + "_UNUSED", "CORRUPT");

        BatchGraphAwareRuntime runtime = new BatchGraphAwareRuntime(batchInserter);
        runtime.registerModule(mockModule);

        runtime.start();

        verify(mockModule, atLeastOnce()).getId();
        verify(mockModule, atLeastOnce()).getConfiguration();
        verifyNoMoreInteractions(mockModule);

        assertEquals(1, count(batchInserter.getNodeProperties(root).keySet()));
    }

    @Test
    public void allRegisteredInterestedModulesShouldBeDelegatedTo() {
        final BatchSupportingGraphAwareRuntimeModule mockModule1 = createBatchSupportingMockModule();
        when(mockModule1.getId()).thenReturn("MOCK1");
        when(mockModule1.getConfiguration()).thenReturn(NullRuntimeModuleConfiguration.getInstance());

        final BatchSupportingGraphAwareRuntimeModule mockModule2 = createBatchSupportingMockModule();
        when(mockModule2.getId()).thenReturn("MOCK2");
        when(mockModule2.getConfiguration()).thenReturn(NullRuntimeModuleConfiguration.getInstance());

        final BatchSupportingGraphAwareRuntimeModule mockModule3 = createBatchSupportingMockModule();
        when(mockModule3.getId()).thenReturn("MOCK3");
        when(mockModule3.getConfiguration()).thenReturn(new MinimalRuntimeModuleConfiguration(InclusionStrategies.none()));

        TransactionSimulatingBatchInserter batchInserter = new TransactionSimulatingBatchInserterImpl(BatchInserters.inserter(temporaryFolder.getRoot().getAbsolutePath()));
        BatchGraphAwareRuntime runtime = new BatchGraphAwareRuntime(batchInserter);
        runtime.registerModule(mockModule1);
        runtime.registerModule(mockModule2);
        runtime.registerModule(mockModule3);

        runtime.start();

        verify(mockModule1).initialize(batchInserter);
        verify(mockModule2).initialize(batchInserter);
        verify(mockModule3).initialize(batchInserter);
        verify(mockModule1, atLeastOnce()).getConfiguration();
        verify(mockModule2, atLeastOnce()).getConfiguration();
        verify(mockModule3, atLeastOnce()).getConfiguration();
        verify(mockModule1, atLeastOnce()).getId();
        verify(mockModule2, atLeastOnce()).getId();
        verify(mockModule3, atLeastOnce()).getId();
        verifyNoMoreInteractions(mockModule1, mockModule2, mockModule3);

        batchInserter.createNode(null);
        batchInserter.shutdown();

        verify(mockModule1).beforeCommit(any(ImprovedTransactionData.class));
        verify(mockModule2).beforeCommit(any(ImprovedTransactionData.class));
        verify(mockModule1, atLeastOnce()).getConfiguration();
        verify(mockModule2, atLeastOnce()).getConfiguration();
        verify(mockModule3, atLeastOnce()).getConfiguration();
        verify(mockModule1).shutdown();
        verify(mockModule2).shutdown();
        verify(mockModule3).shutdown();

        //no interaction with module3, it is not interested!
        verifyNoMoreInteractions(mockModule1, mockModule2, mockModule3);
    }

    @Test
    public void moduleThrowingInitExceptionShouldBeMarkedForReinitialization() {
        final GraphAwareRuntimeModule mockModule = createMockModule();
        when(mockModule.getConfiguration()).thenReturn(NullRuntimeModuleConfiguration.getInstance());
        Mockito.doThrow(new NeedsInitializationException()).when(mockModule).beforeCommit(any(ImprovedTransactionData.class));

        TransactionSimulatingBatchInserter batchInserter = new TransactionSimulatingBatchInserterImpl(BatchInserters.inserter(temporaryFolder.getRoot().getAbsolutePath()));
        BatchGraphAwareRuntime runtime = new BatchGraphAwareRuntime(batchInserter);
        runtime.registerModule(mockModule);

        runtime.start();

        assertTrue(batchInserter.getNodeProperties(0).get(GA_PREFIX + RUNTIME + "_" + MOCK).toString().startsWith(CONFIG));

        batchInserter.createNode(null);
        batchInserter.shutdown();

        batchInserter = new TransactionSimulatingBatchInserterImpl(BatchInserters.inserter(temporaryFolder.getRoot().getAbsolutePath()));
        assertTrue(batchInserter.getNodeProperties(0).get(GA_PREFIX + RUNTIME + "_" + MOCK).toString().startsWith(FORCE_INITIALIZATION));
    }

    @Test
    public void moduleThrowingInitExceptionShouldBeMarkedForReinitializationOnlyTheFirstTime() throws InterruptedException {
        final GraphAwareRuntimeModule mockModule = createMockModule();
        when(mockModule.getConfiguration()).thenReturn(NullRuntimeModuleConfiguration.getInstance());
        doThrow(new NeedsInitializationException()).when(mockModule).beforeCommit(any(ImprovedTransactionData.class));

        TransactionSimulatingBatchInserter batchInserter = new TransactionSimulatingBatchInserterImpl(BatchInserters.inserter(temporaryFolder.getRoot().getAbsolutePath()), 0);
        BatchGraphAwareRuntime runtime = new BatchGraphAwareRuntime(batchInserter);
        runtime.registerModule(mockModule);

        runtime.start();

        batchInserter.createNode(null);

        long firstFailureTimestamp = Long.valueOf(batchInserter.getNodeProperties(0).get(GA_PREFIX + RUNTIME + "_" + MOCK).toString().replaceFirst(FORCE_INITIALIZATION, ""));

        Thread.sleep(1);

        batchInserter.createNode(null);

        long secondFailureTimestamp = Long.valueOf(batchInserter.getNodeProperties(0).get(GA_PREFIX + RUNTIME + "_" + MOCK).toString().replaceFirst(FORCE_INITIALIZATION, ""));

        batchInserter.shutdown();

        assertEquals(firstFailureTimestamp, secondFailureTimestamp);
    }

    @Test(expected = IllegalStateException.class)
    public void modulesCannotBeRegisteredAfterStart() {
        final GraphAwareRuntimeModule mockModule = createMockModule();

        TransactionSimulatingBatchInserter batchInserter = new TransactionSimulatingBatchInserterImpl(BatchInserters.inserter(temporaryFolder.getRoot().getAbsolutePath()));
        BatchGraphAwareRuntime runtime = new BatchGraphAwareRuntime(batchInserter);
        runtime.start(true);
        runtime.registerModule(mockModule);
    }

    @Test
    public void multipleCallsToStartFrameworkHaveNoEffect() {
        TransactionSimulatingBatchInserter batchInserter = new TransactionSimulatingBatchInserterImpl(BatchInserters.inserter(temporaryFolder.getRoot().getAbsolutePath()));
        BatchGraphAwareRuntime runtime = new BatchGraphAwareRuntime(batchInserter);
        runtime.start();
        runtime.start();
        runtime.start();
        runtime.start();
    }

    @Test
    public void runtimeConfiguredModulesShouldBeConfigured() {
        RuntimeConfiguredRuntimeModule mockModule = mock(RuntimeConfiguredRuntimeModule.class);
        when(mockModule.getId()).thenReturn(MOCK);
        when(mockModule.getConfiguration()).thenReturn(NullRuntimeModuleConfiguration.getInstance());

        TransactionSimulatingBatchInserter batchInserter = new TransactionSimulatingBatchInserterImpl(BatchInserters.inserter(temporaryFolder.getRoot().getAbsolutePath()));
        BatchGraphAwareRuntime runtime = new BatchGraphAwareRuntime(batchInserter);
        runtime.registerModule(mockModule);

        verify(mockModule).configurationChanged(DefaultRuntimeConfiguration.getInstance());
        verify(mockModule, atLeastOnce()).getId();
        verifyNoMoreInteractions(mockModule);
    }

    @Test
    public void runtimeConfiguredModulesShouldBeConfigured2() {
        RuntimeConfiguredRuntimeModule mockModule = mock(RuntimeConfiguredRuntimeModule.class);
        when(mockModule.getId()).thenReturn(MOCK);
        when(mockModule.getConfiguration()).thenReturn(NullRuntimeModuleConfiguration.getInstance());

        TransactionSimulatingBatchInserter batchInserter = new TransactionSimulatingBatchInserterImpl(BatchInserters.inserter(temporaryFolder.getRoot().getAbsolutePath()));
        BatchGraphAwareRuntime runtime = new BatchGraphAwareRuntime(batchInserter);
        runtime.registerModule(mockModule, true);

        verify(mockModule).configurationChanged(DefaultRuntimeConfiguration.getInstance());
        verify(mockModule, atLeastOnce()).getId();
        verifyNoMoreInteractions(mockModule);
    }

    @Test
    public void realRuntimeConfiguredModulesShouldBeConfigured() {
        RealRuntimeConfiguredRuntimeModule module = new RealRuntimeConfiguredRuntimeModule();

        TransactionSimulatingBatchInserter batchInserter = new TransactionSimulatingBatchInserterImpl(BatchInserters.inserter(temporaryFolder.getRoot().getAbsolutePath()));
        BatchGraphAwareRuntime runtime = new BatchGraphAwareRuntime(batchInserter);
        runtime.registerModule(module);

        assertEquals(DefaultRuntimeConfiguration.getInstance(), module.getConfig());
    }

    @Test(expected = IllegalStateException.class)
    public void unConfiguredModuleShouldThrowException() {
        RealRuntimeConfiguredRuntimeModule module = new RealRuntimeConfiguredRuntimeModule();
        module.getConfig();
    }

    @Test
    public void shutdownShouldBeCalledBeforeShutdown() {
        RuntimeConfiguredRuntimeModule mockModule = mock(RuntimeConfiguredRuntimeModule.class);
        when(mockModule.getId()).thenReturn(MOCK);
        when(mockModule.getConfiguration()).thenReturn(NullRuntimeModuleConfiguration.getInstance());

        TransactionSimulatingBatchInserter batchInserter = new TransactionSimulatingBatchInserterImpl(BatchInserters.inserter(temporaryFolder.getRoot().getAbsolutePath()));
        BatchGraphAwareRuntime runtime = new BatchGraphAwareRuntime(batchInserter);
        runtime.registerModule(mockModule);
        runtime.start();

        batchInserter.shutdown();

        verify(mockModule).shutdown();
    }

    @Test
    public void whenOneModuleThrowsAnExceptionThenOtherModulesShouldStillBeDelegatedTo() {
        final BatchSupportingGraphAwareRuntimeModule mockModule1 = createBatchSupportingMockModule();
        when(mockModule1.getId()).thenReturn(MOCK + "1");
        when(mockModule1.getConfiguration()).thenReturn(NullRuntimeModuleConfiguration.getInstance());
        doThrow(new RuntimeException()).when(mockModule1).beforeCommit(any(ImprovedTransactionData.class));

        final BatchSupportingGraphAwareRuntimeModule mockModule2 = createBatchSupportingMockModule();
        when(mockModule2.getId()).thenReturn(MOCK + "2");
        when(mockModule2.getConfiguration()).thenReturn(NullRuntimeModuleConfiguration.getInstance());

        TransactionSimulatingBatchInserter batchInserter = new TransactionSimulatingBatchInserterImpl(BatchInserters.inserter(temporaryFolder.getRoot().getAbsolutePath()));
        BatchGraphAwareRuntime runtime = new BatchGraphAwareRuntime(batchInserter);
        runtime.registerModule(mockModule1);
        runtime.registerModule(mockModule2);

        runtime.start();

        verify(mockModule1).initialize(batchInserter);
        verify(mockModule2).initialize(batchInserter);
        verify(mockModule1, atLeastOnce()).getConfiguration();
        verify(mockModule2, atLeastOnce()).getConfiguration();
        verify(mockModule1, atLeastOnce()).getId();
        verify(mockModule2, atLeastOnce()).getId();
        verifyNoMoreInteractions(mockModule1, mockModule2);

        batchInserter.createNode(new HashMap<String, Object>());
        batchInserter.shutdown();

        verify(mockModule1).beforeCommit(any(ImprovedTransactionData.class));
        verify(mockModule2).beforeCommit(any(ImprovedTransactionData.class));
    }
}
