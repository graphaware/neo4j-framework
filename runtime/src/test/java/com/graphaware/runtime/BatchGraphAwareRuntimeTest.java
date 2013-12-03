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

import com.graphaware.runtime.config.DefaultFrameworkConfiguration;
import com.graphaware.common.strategy.InclusionStrategiesImpl;
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

import static com.graphaware.runtime.GraphAwareRuntime.*;
import static com.graphaware.runtime.config.FrameworkConfiguration.GA_PREFIX;
import static com.graphaware.common.test.IterableUtils.count;
import static com.graphaware.runtime.config.FrameworkConfiguration.GA_ROOT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

/**
 * Unit test for {@link GraphAwareRuntime}.
 */
public class BatchGraphAwareRuntimeTest extends BaseGraphAwareRuntimeTest {

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
        final GraphAwareRuntimeModule mockModule = createMockModule();

        TransactionSimulatingBatchInserter batchInserter = new TransactionSimulatingBatchInserterImpl(BatchInserters.inserter(temporaryFolder.getRoot().getAbsolutePath()));
        BatchGraphAwareRuntime framework = new BatchGraphAwareRuntime(batchInserter);
        framework.registerModule(mockModule);

        framework.start();

        verify(mockModule).initialize(batchInserter);
        verify(mockModule).asString();
        verify(mockModule, atLeastOnce()).getId();
        verifyNoMoreInteractions(mockModule);

        assertEquals(CONFIG + TEST_CONFIG, batchInserter.getNodeProperties(0).get(GA_PREFIX + RUNTIME + "_" + MOCK).toString());
    }

    @Test
    public void moduleAlreadyRegisteredShouldNotBeInitialized() {
        final GraphAwareRuntimeModule mockModule = createMockModule();

        TransactionSimulatingBatchInserter batchInserter = new TransactionSimulatingBatchInserterImpl(BatchInserters.inserter(temporaryFolder.getRoot().getAbsolutePath()));
        long root = batchInserter.createNode(Collections.<String, Object>singletonMap(GA_PREFIX + RUNTIME + "_" + MOCK, CONFIG + TEST_CONFIG), GA_ROOT);

        BatchGraphAwareRuntime framework = new BatchGraphAwareRuntime(batchInserter);
        framework.registerModule(mockModule);

        framework.start();

        verify(mockModule, atLeastOnce()).getId();
        verify(mockModule).asString();
        verifyNoMoreInteractions(mockModule);

        assertEquals(CONFIG + TEST_CONFIG, batchInserter.getNodeProperties(root).get(GA_PREFIX + RUNTIME + "_" + MOCK).toString());
    }

    @Test
    @Ignore("Issue 1595")
    public void changedModuleShouldBeReInitialized() {
        final GraphAwareRuntimeModule mockModule = createMockModule();

        TransactionSimulatingBatchInserter batchInserter = new TransactionSimulatingBatchInserterImpl(BatchInserters.inserter(temporaryFolder.getRoot().getAbsolutePath()));
        long root = batchInserter.createNode(Collections.<String, Object>singletonMap(GA_PREFIX + RUNTIME + "_" + MOCK, CONFIG + "123"), GA_ROOT);

        BatchGraphAwareRuntime framework = new BatchGraphAwareRuntime(batchInserter);
        framework.registerModule(mockModule);

        framework.start();

        verify(mockModule).reinitialize(batchInserter);
        verify(mockModule, times(2)).asString();
        verify(mockModule, atLeastOnce()).getId();
        verifyNoMoreInteractions(mockModule);

        assertEquals(CONFIG + TEST_CONFIG, batchInserter.getNodeProperties(root).get(GA_PREFIX + RUNTIME + "_" + MOCK).toString());
    }

    @Test
    @Ignore("Issue 1595")
    public void forcedModuleShouldBeReInitialized() {
        final GraphAwareRuntimeModule mockModule = createMockModule();

        TransactionSimulatingBatchInserter batchInserter = new TransactionSimulatingBatchInserterImpl(BatchInserters.inserter(temporaryFolder.getRoot().getAbsolutePath()));
        long root = batchInserter.createNode(new HashMap<String, Object>(), GA_ROOT);
        batchInserter.setNodeProperty(root, GA_PREFIX + RUNTIME + "_" + MOCK, FORCE_INITIALIZATION + "123");

        BatchGraphAwareRuntime framework = new BatchGraphAwareRuntime(batchInserter);
        framework.registerModule(mockModule);

        framework.start();

        verify(mockModule).reinitialize(batchInserter);
        verify(mockModule).asString();
        verify(mockModule, atLeastOnce()).getId();
        verifyNoMoreInteractions(mockModule);

        assertEquals(CONFIG + TEST_CONFIG, batchInserter.getNodeProperties(root).get(GA_PREFIX + RUNTIME + "_" + MOCK).toString());
    }

    @Test
    @Ignore("Issue 1595")
    public void moduleAlreadyRegisteredShouldBeInitializedWhenForced() {
        final GraphAwareRuntimeModule mockModule = createMockModule();

        TransactionSimulatingBatchInserter batchInserter = new TransactionSimulatingBatchInserterImpl(BatchInserters.inserter(temporaryFolder.getRoot().getAbsolutePath()));
        long root = batchInserter.createNode(Collections.<String, Object>singletonMap(GA_PREFIX + RUNTIME + "_" + MOCK, CONFIG + TEST_CONFIG), GA_ROOT);

        BatchGraphAwareRuntime framework = new BatchGraphAwareRuntime(batchInserter);
        framework.registerModule(mockModule, true);

        framework.start();

        verify(mockModule).reinitialize(batchInserter);
        verify(mockModule).asString();
        verify(mockModule, atLeastOnce()).getId();
        verifyNoMoreInteractions(mockModule);

        assertEquals(CONFIG + TEST_CONFIG, batchInserter.getNodeProperties(root).get(GA_PREFIX + RUNTIME + "_" + MOCK).toString());
    }

    @Test
    public void changedModuleShouldNotBeReInitializedWhenInitializationSkipped() {
        final GraphAwareRuntimeModule mockModule = createMockModule();

        TransactionSimulatingBatchInserter batchInserter = new TransactionSimulatingBatchInserterImpl(BatchInserters.inserter(temporaryFolder.getRoot().getAbsolutePath()));
        long root = batchInserter.createNode(Collections.<String, Object>singletonMap(GA_PREFIX + RUNTIME + "_" + MOCK, CONFIG + "123"), GA_ROOT);

        BatchGraphAwareRuntime framework = new BatchGraphAwareRuntime(batchInserter);
        framework.registerModule(mockModule);

        framework.start(true);

        verify(mockModule, atLeastOnce()).getId();
        verifyNoMoreInteractions(mockModule);

        assertEquals(CONFIG + "123", batchInserter.getNodeProperties(root).get(GA_PREFIX + RUNTIME + "_" + MOCK).toString());
    }

    @Test(expected = IllegalStateException.class)
    public void shouldNotBeAbleToRegisterTheSameModuleTwice() {
        final GraphAwareRuntimeModule mockModule = createMockModule();

        TransactionSimulatingBatchInserter batchInserter = new TransactionSimulatingBatchInserterImpl(BatchInserters.inserter(temporaryFolder.getRoot().getAbsolutePath()));
        BatchGraphAwareRuntime framework = new BatchGraphAwareRuntime(batchInserter);
        framework.registerModule(mockModule);
        framework.registerModule(mockModule);
    }

    @Test(expected = IllegalStateException.class)
    public void shouldNotBeAbleToRegisterModuleWithTheSameIdTwice() {
        final GraphAwareRuntimeModule mockModule1 = createMockModule();
        final GraphAwareRuntimeModule mockModule2 = createMockModule();
        when(mockModule1.getId()).thenReturn("ID");
        when(mockModule2.getId()).thenReturn("ID");

        TransactionSimulatingBatchInserter batchInserter = new TransactionSimulatingBatchInserterImpl(BatchInserters.inserter(temporaryFolder.getRoot().getAbsolutePath()));
        BatchGraphAwareRuntime framework = new BatchGraphAwareRuntime(batchInserter);
        framework.registerModule(mockModule1);
        framework.registerModule(mockModule2);
    }

    @Test
    public void unusedModulesShouldBeRemoved() {
        final GraphAwareRuntimeModule mockModule = createMockModule();

        TransactionSimulatingBatchInserter batchInserter = new TransactionSimulatingBatchInserterImpl(BatchInserters.inserter(temporaryFolder.getRoot().getAbsolutePath()));
        long root = batchInserter.createNode(Collections.<String, Object>emptyMap(), GA_ROOT);
        batchInserter.setNodeProperty(root, GA_PREFIX + RUNTIME + "_" + MOCK, CONFIG + TEST_CONFIG);
        batchInserter.setNodeProperty(root, GA_PREFIX + RUNTIME + "_UNUSED", CONFIG + "123");

        BatchGraphAwareRuntime framework = new BatchGraphAwareRuntime(batchInserter);
        framework.registerModule(mockModule);

        framework.start();

        verify(mockModule, atLeastOnce()).getId();
        verify(mockModule).asString();
        verifyNoMoreInteractions(mockModule);

        assertEquals(1, count(batchInserter.getNodeProperties(root).keySet()));
    }

    @Test(expected = IllegalStateException.class)
    public void usedCorruptModulesShouldThrowException() {
        final GraphAwareRuntimeModule mockModule = createMockModule();

        TransactionSimulatingBatchInserter batchInserter = new TransactionSimulatingBatchInserterImpl(BatchInserters.inserter(temporaryFolder.getRoot().getAbsolutePath()));
        batchInserter.createNode(Collections.<String, Object>singletonMap(GA_PREFIX + RUNTIME + "_" + MOCK, "CORRUPT"), GA_ROOT);

        BatchGraphAwareRuntime framework = new BatchGraphAwareRuntime(batchInserter);
        framework.registerModule(mockModule);

        framework.start();
    }

    @Test
    public void unusedCorruptModulesShouldBeRemoved() {
        final GraphAwareRuntimeModule mockModule = createMockModule();

        TransactionSimulatingBatchInserter batchInserter = new TransactionSimulatingBatchInserterImpl(BatchInserters.inserter(temporaryFolder.getRoot().getAbsolutePath()));
        long root = batchInserter.createNode(Collections.<String, Object>emptyMap(), GA_ROOT);
        batchInserter.setNodeProperty(root, GA_PREFIX + RUNTIME + "_" + MOCK, CONFIG + TEST_CONFIG);
        batchInserter.setNodeProperty(root, GA_PREFIX + RUNTIME + "_UNUSED", "CORRUPT");

        BatchGraphAwareRuntime framework = new BatchGraphAwareRuntime(batchInserter);
        framework.registerModule(mockModule);

        framework.start();

        verify(mockModule, atLeastOnce()).getId();
        verify(mockModule).asString();
        verifyNoMoreInteractions(mockModule);

        assertEquals(1, count(batchInserter.getNodeProperties(root).keySet()));
    }

    @Test
    public void allRegisteredInterestedModulesShouldBeDelegatedTo() {
        GraphAwareRuntimeModule mockModule1 = mock(GraphAwareRuntimeModule.class);
        when(mockModule1.getId()).thenReturn("MOCK1");
        when(mockModule1.asString()).thenReturn(TEST_CONFIG);
        when(mockModule1.getInclusionStrategies()).thenReturn(InclusionStrategiesImpl.all());

        GraphAwareRuntimeModule mockModule2 = mock(GraphAwareRuntimeModule.class);
        when(mockModule2.getId()).thenReturn("MOCK2");
        when(mockModule2.asString()).thenReturn(TEST_CONFIG);
        when(mockModule2.getInclusionStrategies()).thenReturn(InclusionStrategiesImpl.all());

        GraphAwareRuntimeModule mockModule3 = mock(GraphAwareRuntimeModule.class);
        when(mockModule3.getId()).thenReturn("MOCK3");
        when(mockModule3.asString()).thenReturn(TEST_CONFIG);
        when(mockModule3.getInclusionStrategies()).thenReturn(InclusionStrategiesImpl.none());

        TransactionSimulatingBatchInserter batchInserter = new TransactionSimulatingBatchInserterImpl(BatchInserters.inserter(temporaryFolder.getRoot().getAbsolutePath()));
        BatchGraphAwareRuntime framework = new BatchGraphAwareRuntime(batchInserter);
        framework.registerModule(mockModule1);
        framework.registerModule(mockModule2);
        framework.registerModule(mockModule3);

        framework.start();

        verify(mockModule1).initialize(batchInserter);
        verify(mockModule2).initialize(batchInserter);
        verify(mockModule3).initialize(batchInserter);
        verify(mockModule1).asString();
        verify(mockModule2).asString();
        verify(mockModule3).asString();
        verify(mockModule1, atLeastOnce()).getId();
        verify(mockModule2, atLeastOnce()).getId();
        verify(mockModule3, atLeastOnce()).getId();
        verifyNoMoreInteractions(mockModule1, mockModule2, mockModule3);

        batchInserter.createNode(null);
        batchInserter.shutdown();

        verify(mockModule1).getInclusionStrategies();
        verify(mockModule2).getInclusionStrategies();
        verify(mockModule3).getInclusionStrategies();
        verify(mockModule1).beforeCommit(any(ImprovedTransactionData.class));
        verify(mockModule2).beforeCommit(any(ImprovedTransactionData.class));
        verify(mockModule1).shutdown();
        verify(mockModule2).shutdown();
        verify(mockModule3).shutdown();
        //no interaction with module3, it is not interested!
        verifyNoMoreInteractions(mockModule1, mockModule2, mockModule3);
    }

    @Test
    public void noRegisteredInterestedModulesShouldBeDelegatedToBeforeFrameworkStarts() {
        GraphAwareRuntimeModule mockModule1 = mock(GraphAwareRuntimeModule.class);
        when(mockModule1.getId()).thenReturn("MOCK1");
        when(mockModule1.asString()).thenReturn(TEST_CONFIG);
        when(mockModule1.getInclusionStrategies()).thenReturn(InclusionStrategiesImpl.all());

        GraphAwareRuntimeModule mockModule2 = mock(GraphAwareRuntimeModule.class);
        when(mockModule2.getId()).thenReturn("MOCK2");
        when(mockModule2.asString()).thenReturn(TEST_CONFIG);
        when(mockModule2.getInclusionStrategies()).thenReturn(InclusionStrategiesImpl.all());

        GraphAwareRuntimeModule mockModule3 = mock(GraphAwareRuntimeModule.class);
        when(mockModule3.getId()).thenReturn("MOCK3");
        when(mockModule3.asString()).thenReturn(TEST_CONFIG);
        when(mockModule3.getInclusionStrategies()).thenReturn(InclusionStrategiesImpl.none());

        TransactionSimulatingBatchInserter batchInserter = new TransactionSimulatingBatchInserterImpl(BatchInserters.inserter(temporaryFolder.getRoot().getAbsolutePath()));
        BatchGraphAwareRuntime framework = new BatchGraphAwareRuntime(batchInserter);
        framework.registerModule(mockModule1);
        framework.registerModule(mockModule2);
        framework.registerModule(mockModule3);

        verify(mockModule1, atLeastOnce()).getId();
        verify(mockModule2, atLeastOnce()).getId();
        verify(mockModule3, atLeastOnce()).getId();
        verifyNoMoreInteractions(mockModule1, mockModule2, mockModule3);

        batchInserter.shutdown();

        verifyNoMoreInteractions(mockModule1, mockModule2, mockModule3);
    }

    @Test
    @Ignore("Issue 1595")
    public void moduleThrowingInitExceptionShouldBeMarkedForReinitialization() {
        final GraphAwareRuntimeModule mockModule = createMockModule();
        when(mockModule.getInclusionStrategies()).thenReturn(InclusionStrategiesImpl.all());
        Mockito.doThrow(new NeedsInitializationException()).when(mockModule).beforeCommit(any(ImprovedTransactionData.class));

        TransactionSimulatingBatchInserter batchInserter = new TransactionSimulatingBatchInserterImpl(BatchInserters.inserter(temporaryFolder.getRoot().getAbsolutePath()));
        BatchGraphAwareRuntime framework = new BatchGraphAwareRuntime(batchInserter);
        framework.registerModule(mockModule);

        framework.start();

        assertTrue(batchInserter.getNodeProperties(0).get(GA_PREFIX + RUNTIME + "_" + MOCK).toString().startsWith(CONFIG));

        batchInserter.createNode(null);
        batchInserter.shutdown();

        batchInserter = new TransactionSimulatingBatchInserterImpl(BatchInserters.inserter(temporaryFolder.getRoot().getAbsolutePath()));
        assertTrue(batchInserter.getNodeProperties(0).get(GA_PREFIX + RUNTIME + "_" + MOCK).toString().startsWith(FORCE_INITIALIZATION));
    }

    @Test
    @Ignore("Issue 1595")
    public void moduleThrowingInitExceptionShouldBeMarkedForReinitializationOnlyTheFirstTime() throws InterruptedException {
        final GraphAwareRuntimeModule mockModule = createMockModule();
        when(mockModule.getInclusionStrategies()).thenReturn(InclusionStrategiesImpl.all());
        doThrow(new NeedsInitializationException()).when(mockModule).beforeCommit(any(ImprovedTransactionData.class));

        TransactionSimulatingBatchInserter batchInserter = new TransactionSimulatingBatchInserterImpl(BatchInserters.inserter(temporaryFolder.getRoot().getAbsolutePath()), 0);
        BatchGraphAwareRuntime framework = new BatchGraphAwareRuntime(batchInserter);
        framework.registerModule(mockModule);

        framework.start();

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
        BatchGraphAwareRuntime framework = new BatchGraphAwareRuntime(batchInserter);
        framework.start(true);
        framework.registerModule(mockModule);
    }

    @Test(expected = IllegalStateException.class)
    public void frameworkCanOnlyBeStartedOnce() {
        TransactionSimulatingBatchInserter batchInserter = new TransactionSimulatingBatchInserterImpl(BatchInserters.inserter(temporaryFolder.getRoot().getAbsolutePath()));
        BatchGraphAwareRuntime framework = new BatchGraphAwareRuntime(batchInserter);
        framework.start();
        framework.start();
    }

    @Test
    public void frameworkConfiguredModulesShouldBeConfigured() {
        FrameworkConfiguredRuntimeModule mockModule = mock(FrameworkConfiguredRuntimeModule.class);
        when(mockModule.getId()).thenReturn(MOCK);
        when(mockModule.asString()).thenReturn(TEST_CONFIG);

        TransactionSimulatingBatchInserter batchInserter = new TransactionSimulatingBatchInserterImpl(BatchInserters.inserter(temporaryFolder.getRoot().getAbsolutePath()));
        BatchGraphAwareRuntime framework = new BatchGraphAwareRuntime(batchInserter);
        framework.registerModule(mockModule);

        verify(mockModule).configurationChanged(DefaultFrameworkConfiguration.getInstance());
        verify(mockModule, atLeastOnce()).getId();
        verifyNoMoreInteractions(mockModule);
    }

    @Test
    public void frameworkConfiguredModulesShouldBeConfigured2() {
        FrameworkConfiguredRuntimeModule mockModule = mock(FrameworkConfiguredRuntimeModule.class);
        when(mockModule.getId()).thenReturn(MOCK);
        when(mockModule.asString()).thenReturn(TEST_CONFIG);

        TransactionSimulatingBatchInserter batchInserter = new TransactionSimulatingBatchInserterImpl(BatchInserters.inserter(temporaryFolder.getRoot().getAbsolutePath()));
        BatchGraphAwareRuntime framework = new BatchGraphAwareRuntime(batchInserter);
        framework.registerModule(mockModule, true);

        verify(mockModule).configurationChanged(DefaultFrameworkConfiguration.getInstance());
        verify(mockModule, atLeastOnce()).getId();
        verifyNoMoreInteractions(mockModule);
    }

    @Test
    public void realFrameworkConfiguredModulesShouldBeConfigured() {
        RealFrameworkConfiguredRuntimeModule module = new RealFrameworkConfiguredRuntimeModule();

        TransactionSimulatingBatchInserter batchInserter = new TransactionSimulatingBatchInserterImpl(BatchInserters.inserter(temporaryFolder.getRoot().getAbsolutePath()));
        BatchGraphAwareRuntime framework = new BatchGraphAwareRuntime(batchInserter);
        framework.registerModule(module);

        assertEquals(DefaultFrameworkConfiguration.getInstance(), module.getConfig());
    }

    @Test(expected = IllegalStateException.class)
    public void unConfiguredModuleShouldThrowException() {
        RealFrameworkConfiguredRuntimeModule module = new RealFrameworkConfiguredRuntimeModule();
        module.getConfig();
    }

    @Test
    public void shutdownShouldBeCalledBeforeShutdown() {
        FrameworkConfiguredRuntimeModule mockModule = mock(FrameworkConfiguredRuntimeModule.class);
        when(mockModule.getId()).thenReturn(MOCK);
        when(mockModule.asString()).thenReturn(TEST_CONFIG);

        TransactionSimulatingBatchInserter batchInserter = new TransactionSimulatingBatchInserterImpl(BatchInserters.inserter(temporaryFolder.getRoot().getAbsolutePath()));
        BatchGraphAwareRuntime framework = new BatchGraphAwareRuntime(batchInserter);
        framework.registerModule(mockModule);
        framework.start();

        batchInserter.shutdown();

        verify(mockModule).shutdown();
    }

    @Test
    public void whenOneModuleThrowsAnExceptionThenOtherModulesShouldStillBeDelegatedTo() {
        GraphAwareRuntimeModule mockModule1 = mock(GraphAwareRuntimeModule.class);
        when(mockModule1.getId()).thenReturn(MOCK + "1");
        when(mockModule1.asString()).thenReturn(TEST_CONFIG);
        when(mockModule1.getInclusionStrategies()).thenReturn(InclusionStrategiesImpl.all());
        doThrow(new RuntimeException()).when(mockModule1).beforeCommit(any(ImprovedTransactionData.class));

        GraphAwareRuntimeModule mockModule2 = mock(GraphAwareRuntimeModule.class);
        when(mockModule2.getId()).thenReturn(MOCK + "2");
        when(mockModule2.asString()).thenReturn(TEST_CONFIG);
        when(mockModule2.getInclusionStrategies()).thenReturn(InclusionStrategiesImpl.all());

        TransactionSimulatingBatchInserter batchInserter = new TransactionSimulatingBatchInserterImpl(BatchInserters.inserter(temporaryFolder.getRoot().getAbsolutePath()));
        BatchGraphAwareRuntime framework = new BatchGraphAwareRuntime(batchInserter);
        framework.registerModule(mockModule1);
        framework.registerModule(mockModule2);

        framework.start();

        verify(mockModule1).initialize(batchInserter);
        verify(mockModule2).initialize(batchInserter);
        verify(mockModule1).asString();
        verify(mockModule2).asString();
        verify(mockModule1, atLeastOnce()).getId();
        verify(mockModule2, atLeastOnce()).getId();
        verifyNoMoreInteractions(mockModule1, mockModule2);

        batchInserter.createNode(new HashMap<String, Object>());
        batchInserter.shutdown();

        verify(mockModule1).getInclusionStrategies();
        verify(mockModule2).getInclusionStrategies();
        verify(mockModule1).beforeCommit(any(ImprovedTransactionData.class));
        verify(mockModule2).beforeCommit(any(ImprovedTransactionData.class));
    }
}
