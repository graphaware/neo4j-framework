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
import com.graphaware.tx.event.improved.api.ImprovedTransactionData;
import com.graphaware.tx.executor.single.SimpleTransactionExecutor;
import com.graphaware.tx.executor.single.VoidReturningCallback;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mockito;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.unsafe.batchinsert.BatchInserters;
import org.neo4j.unsafe.batchinsert.TransactionSimulatingBatchGraphDatabase;

import java.io.IOException;

import static com.graphaware.runtime.GraphAwareRuntime.*;
import static com.graphaware.runtime.config.FrameworkConfiguration.GA_PREFIX;
import static com.graphaware.common.test.IterableUtils.count;
import static com.graphaware.runtime.config.FrameworkConfiguration.GA_ROOT;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Unit test for {@link GraphAwareRuntime} used with batch graph database.
 */
public class GraphAwareRuntimeBatchDatabaseTest extends BaseGraphAwareRuntimeTest {

    private GraphDatabaseService database;
    private final TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Before
    public void setUp() throws IOException {
        temporaryFolder.create();
        database = new TransactionSimulatingBatchGraphDatabase(BatchInserters.batchDatabase(temporaryFolder.getRoot().getAbsolutePath()));
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

    @Test
    public void shouldCreateRuntimeRootNodeAfterFirstStartup() {
        assertNull(getRuntimeRoot());

        GraphAwareRuntime runtime = new GraphAwareRuntime(database);

        assertNull(getRuntimeRoot());

        runtime.start();

        assertNotNull(getRuntimeRoot());
    }

    @Test
    public void moduleRegisteredForTheFirstTimeShouldBeInitialized() {
        final GraphAwareRuntimeModule mockModule = createMockModule();

        GraphAwareRuntime framework = new GraphAwareRuntime(database);
        framework.registerModule(mockModule);

        framework.start();

        verify(mockModule).initialize(database);
        verify(mockModule).asString();
        verify(mockModule, atLeastOnce()).getId();
        verifyNoMoreInteractions(mockModule);

        assertEquals(CONFIG + TEST_CONFIG, getRuntimeRoot().getProperty(GA_PREFIX + RUNTIME + "_" + MOCK).toString());
    }

    @Test
    public void moduleAlreadyRegisteredShouldNotBeInitialized() {
        final GraphAwareRuntimeModule mockModule = createMockModule();

        createRuntimeRoot().setProperty(GA_PREFIX + RUNTIME + "_" + MOCK, CONFIG + TEST_CONFIG);
        GraphAwareRuntime framework = new GraphAwareRuntime(database);
        framework.registerModule(mockModule);

        framework.start();

        verify(mockModule, atLeastOnce()).getId();
        verify(mockModule).asString();
        verifyNoMoreInteractions(mockModule);

        assertEquals(CONFIG + TEST_CONFIG, getRuntimeRoot().getProperty(GA_PREFIX + RUNTIME + "_" + MOCK).toString());
    }

    @Test
    public void changedModuleShouldBeReInitialized() {
        final GraphAwareRuntimeModule mockModule = createMockModule();
        createRuntimeRoot().setProperty(GA_PREFIX + RUNTIME + "_" + MOCK, CONFIG + "123");

        GraphAwareRuntime framework = new GraphAwareRuntime(database);
        framework.registerModule(mockModule);

        framework.start();

        verify(mockModule).reinitialize(database);
        verify(mockModule, times(2)).asString();
        verify(mockModule, atLeastOnce()).getId();
        verifyNoMoreInteractions(mockModule);

        assertEquals(CONFIG + TEST_CONFIG, getRuntimeRoot().getProperty(GA_PREFIX + RUNTIME + "_" + MOCK).toString());
    }

    @Test
    public void forcedModuleShouldBeReInitialized() {
        final GraphAwareRuntimeModule mockModule = createMockModule();

        createRuntimeRoot().setProperty(GA_PREFIX + RUNTIME + "_" + MOCK, FORCE_INITIALIZATION + "123");

        GraphAwareRuntime framework = new GraphAwareRuntime(database);
        framework.registerModule(mockModule);

        framework.start();

        verify(mockModule).reinitialize(database);
        verify(mockModule).asString();
        verify(mockModule, atLeastOnce()).getId();
        verifyNoMoreInteractions(mockModule);

        assertEquals(CONFIG + TEST_CONFIG, getRuntimeRoot().getProperty(GA_PREFIX + RUNTIME + "_" + MOCK).toString());
    }

    @Test
    public void moduleAlreadyRegisteredShouldBeInitializedWhenForced() {
        final GraphAwareRuntimeModule mockModule = createMockModule();

        createRuntimeRoot().setProperty(GA_PREFIX + RUNTIME + "_" + MOCK, CONFIG + TEST_CONFIG);

        GraphAwareRuntime framework = new GraphAwareRuntime(database);
        framework.registerModule(mockModule, true);

        framework.start();

        verify(mockModule).reinitialize(database);
        verify(mockModule).asString();
        verify(mockModule, atLeastOnce()).getId();
        verifyNoMoreInteractions(mockModule);

        assertEquals(CONFIG + TEST_CONFIG, getRuntimeRoot().getProperty(GA_PREFIX + RUNTIME + "_" + MOCK).toString());
    }

    @Test
    public void changedModuleShouldNotBeReInitializedWhenInitializationSkipped() {
        final GraphAwareRuntimeModule mockModule = createMockModule();

        createRuntimeRoot().setProperty(GA_PREFIX + RUNTIME + "_" + MOCK, CONFIG + "123");

        GraphAwareRuntime framework = new GraphAwareRuntime(database);
        framework.registerModule(mockModule);

        framework.start(true);

        verify(mockModule, atLeastOnce()).getId();
        verifyNoMoreInteractions(mockModule);

        assertEquals(CONFIG + "123", getRuntimeRoot().getProperty(GA_PREFIX + RUNTIME + "_" + MOCK).toString());
    }

    @Test(expected = IllegalStateException.class)
    public void shouldNotBeAbleToRegisterTheSameModuleTwice() {
        final GraphAwareRuntimeModule mockModule = createMockModule();

        GraphAwareRuntime framework = new GraphAwareRuntime(database);
        framework.registerModule(mockModule);
        framework.registerModule(mockModule);
    }

    @Test(expected = IllegalStateException.class)
    public void shouldNotBeAbleToRegisterModuleWithTheSameIdTwice() {
        final GraphAwareRuntimeModule mockModule1 = createMockModule();
        final GraphAwareRuntimeModule mockModule2 = createMockModule();
        when(mockModule1.getId()).thenReturn("ID");
        when(mockModule2.getId()).thenReturn("ID");

        GraphAwareRuntime framework = new GraphAwareRuntime(database);
        framework.registerModule(mockModule1);
        framework.registerModule(mockModule2);
    }

    @Test
    public void unusedModulesShouldBeRemoved() {
        final GraphAwareRuntimeModule mockModule = createMockModule();

        Node root = createRuntimeRoot();
        root.setProperty(GA_PREFIX + RUNTIME + "_" + MOCK, CONFIG + TEST_CONFIG);
        root.setProperty(GA_PREFIX + RUNTIME + "_UNUSED", CONFIG + "123");

        GraphAwareRuntime framework = new GraphAwareRuntime(database);
        framework.registerModule(mockModule);

        framework.start();

        verify(mockModule, atLeastOnce()).getId();
        verify(mockModule).asString();
        verifyNoMoreInteractions(mockModule);

        assertEquals(1, count(getRuntimeRoot().getPropertyKeys()));
    }

    @Test(expected = IllegalStateException.class)
    public void usedCorruptModulesShouldThrowException() {
        final GraphAwareRuntimeModule mockModule = createMockModule();

        Node root = createRuntimeRoot();
        root.setProperty(GA_PREFIX + RUNTIME + "_" + MOCK, "CORRUPT");

        GraphAwareRuntime framework = new GraphAwareRuntime(database);
        framework.registerModule(mockModule);

        framework.start();
    }

    @Test
    public void unusedCorruptModulesShouldBeRemoved() {
        final GraphAwareRuntimeModule mockModule = createMockModule();

        Node root = createRuntimeRoot();
        root.setProperty(GA_PREFIX + RUNTIME + "_" + MOCK, CONFIG + TEST_CONFIG);
        root.setProperty(GA_PREFIX + RUNTIME + "_UNUSED", "CORRUPT");

        GraphAwareRuntime framework = new GraphAwareRuntime(database);
        framework.registerModule(mockModule);

        framework.start();

        verify(mockModule, atLeastOnce()).getId();
        verify(mockModule).asString();
        verifyNoMoreInteractions(mockModule);

        assertEquals(1, count(getRuntimeRoot().getPropertyKeys()));
    }

    @Test
    public void allRegisteredInterestedModulesShouldBeDelegatedTo() {
        database.shutdown();
        database = new TransactionSimulatingBatchGraphDatabase(BatchInserters.batchDatabase(temporaryFolder.getRoot().getAbsolutePath()), 0);

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

        GraphAwareRuntime framework = new GraphAwareRuntime(database);
        framework.registerModule(mockModule1);
        framework.registerModule(mockModule2);
        framework.registerModule(mockModule3);

        framework.start();

        verify(mockModule1).initialize(database);
        verify(mockModule2).initialize(database);
        verify(mockModule3).initialize(database);
        verify(mockModule1).asString();
        verify(mockModule2).asString();
        verify(mockModule3).asString();
        verify(mockModule1, atLeastOnce()).getId();
        verify(mockModule2, atLeastOnce()).getId();
        verify(mockModule3, atLeastOnce()).getId();
        verifyNoMoreInteractions(mockModule1, mockModule2, mockModule3);

        database.createNode();


        verify(mockModule1).getInclusionStrategies();
        verify(mockModule2).getInclusionStrategies();
        verify(mockModule3).getInclusionStrategies();
        verify(mockModule1).beforeCommit(any(ImprovedTransactionData.class));
        verify(mockModule2).beforeCommit(any(ImprovedTransactionData.class));
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

        GraphAwareRuntime framework = new GraphAwareRuntime(database);
        framework.registerModule(mockModule1);
        framework.registerModule(mockModule2);
        framework.registerModule(mockModule3);

        verify(mockModule1, atLeastOnce()).getId();
        verify(mockModule2, atLeastOnce()).getId();
        verify(mockModule3, atLeastOnce()).getId();
        verifyNoMoreInteractions(mockModule1, mockModule2, mockModule3);

        database.createNode();

        verifyNoMoreInteractions(mockModule1, mockModule2, mockModule3);
    }

    @Test
    public void moduleThrowingInitExceptionShouldBeMarkedForReinitialization() {
        final GraphAwareRuntimeModule mockModule = createMockModule();
        when(mockModule.getInclusionStrategies()).thenReturn(InclusionStrategiesImpl.all());
        Mockito.doThrow(new NeedsInitializationException()).when(mockModule).beforeCommit(any(ImprovedTransactionData.class));

        database.shutdown();
        database = new TransactionSimulatingBatchGraphDatabase(BatchInserters.batchDatabase(temporaryFolder.getRoot().getAbsolutePath()), 0);

        GraphAwareRuntime framework = new GraphAwareRuntime(database);
        framework.registerModule(mockModule);

        framework.start();

        assertTrue(getRuntimeRoot().getProperty(GA_PREFIX + RUNTIME + "_" + MOCK).toString().startsWith(CONFIG));

        database.createNode();

        assertTrue(getRuntimeRoot().getProperty(GA_PREFIX + RUNTIME + "_" + MOCK).toString().startsWith(FORCE_INITIALIZATION));
    }

    @Test
    public void moduleThrowingInitExceptionShouldBeMarkedForReinitializationOnlyTheFirstTime() throws InterruptedException {
        final GraphAwareRuntimeModule mockModule = createMockModule();
        when(mockModule.getInclusionStrategies()).thenReturn(InclusionStrategiesImpl.all());
        doThrow(new NeedsInitializationException()).when(mockModule).beforeCommit(any(ImprovedTransactionData.class));

        GraphAwareRuntime framework = new GraphAwareRuntime(database);
        framework.registerModule(mockModule);

        framework.start();

        new SimpleTransactionExecutor(database).executeInTransaction(new VoidReturningCallback() {
            @Override
            protected void doInTx(GraphDatabaseService database) {
                database.createNode();
            }
        });

        database.shutdown();
        database = new TransactionSimulatingBatchGraphDatabase(BatchInserters.batchDatabase(temporaryFolder.getRoot().getAbsolutePath()), 0);

        long firstFailureTimestamp = Long.valueOf(getRuntimeRoot().getProperty(GA_PREFIX + RUNTIME + "_" + MOCK).toString().replaceFirst(FORCE_INITIALIZATION, ""));

        Thread.sleep(1);

        new SimpleTransactionExecutor(database).executeInTransaction(new VoidReturningCallback() {
            @Override
            protected void doInTx(GraphDatabaseService database) {
                database.createNode();
            }
        });

        database.shutdown();
        database = new TransactionSimulatingBatchGraphDatabase(BatchInserters.batchDatabase(temporaryFolder.getRoot().getAbsolutePath()), 0);

        long secondFailureTimestamp = Long.valueOf(getRuntimeRoot().getProperty(GA_PREFIX + RUNTIME + "_" + MOCK).toString().replaceFirst(FORCE_INITIALIZATION, ""));

        assertEquals(firstFailureTimestamp, secondFailureTimestamp);
    }

    @Test(expected = IllegalStateException.class)
    public void modulesCannotBeRegisteredAfterStart() {
        final GraphAwareRuntimeModule mockModule = createMockModule();

        GraphAwareRuntime framework = new GraphAwareRuntime(database);
        framework.start(true);
        framework.registerModule(mockModule);
    }

    @Test(expected = IllegalStateException.class)
    public void frameworkCanOnlyBeStartedOnce() {
        GraphAwareRuntime framework = new GraphAwareRuntime(database);
        framework.start();
        framework.start();
    }

    @Test
    public void frameworkConfiguredModulesShouldBeConfigured() {
        FrameworkConfiguredRuntimeModule mockModule = mock(FrameworkConfiguredRuntimeModule.class);
        when(mockModule.getId()).thenReturn("MOCK");

        GraphAwareRuntime framework = new GraphAwareRuntime(database);
        framework.registerModule(mockModule);

        verify(mockModule).configurationChanged(DefaultFrameworkConfiguration.getInstance());
        verify(mockModule, atLeastOnce()).getId();
        verifyNoMoreInteractions(mockModule);
    }

    @Test
    public void frameworkConfiguredModulesShouldBeConfigured2() {
        FrameworkConfiguredRuntimeModule mockModule = mock(FrameworkConfiguredRuntimeModule.class);
        when(mockModule.getId()).thenReturn("MOCK");

        GraphAwareRuntime framework = new GraphAwareRuntime(database);
        framework.registerModule(mockModule, true);

        verify(mockModule).configurationChanged(DefaultFrameworkConfiguration.getInstance());
        verify(mockModule, atLeastOnce()).getId();
        verifyNoMoreInteractions(mockModule);
    }

    @Test
    public void realFrameworkConfiguredModulesShouldBeConfigured() {
        RealFrameworkConfiguredRuntimeModule module = new RealFrameworkConfiguredRuntimeModule();

        GraphAwareRuntime framework = new GraphAwareRuntime(database);
        framework.registerModule(module, true);

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
        when(mockModule.getId()).thenReturn("MOCK");

        GraphAwareRuntime framework = new GraphAwareRuntime(database);
        framework.registerModule(mockModule);
        framework.start();

        database.shutdown();

        verify(mockModule).shutdown();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void shouldNotBeAllowedToDeleteRootNode() {
        GraphAwareRuntime framework = new GraphAwareRuntime(database);
        framework.start();

        getRuntimeRoot().delete();
    }

    private Node getRuntimeRoot() {
        Node root = null;

        try (Transaction tx = database.beginTx()) {
            for (Node node : database.getAllNodes()) {
                if (node.hasLabel(GA_ROOT)) {
                    root = node;
                    break;
                }
            }

            tx.success();
        }

        return root;
    }

    private Node createRuntimeRoot() {
        Node root;

        try (Transaction tx = database.beginTx()) {
            if (getRuntimeRoot() != null) {
                throw new IllegalArgumentException("Runtime root already exists!");
            }
            root = database.createNode(GA_ROOT);
            tx.success();
        }

        return root;
    }
}
