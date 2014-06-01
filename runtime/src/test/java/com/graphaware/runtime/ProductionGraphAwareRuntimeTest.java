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
import com.graphaware.tx.event.improved.api.ImprovedTransactionData;
import com.graphaware.tx.executor.single.SimpleTransactionExecutor;
import com.graphaware.tx.executor.single.VoidReturningCallback;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.TransactionFailureException;
import org.neo4j.test.TestGraphDatabaseFactory;

import java.util.Iterator;

import static com.graphaware.common.util.IterableUtils.count;
import static com.graphaware.runtime.ProductionGraphAwareRuntime.*;
import static com.graphaware.runtime.config.RuntimeConfiguration.GA_PREFIX;
import static com.graphaware.runtime.config.RuntimeConfiguration.GA_ROOT;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.neo4j.tooling.GlobalGraphOperations.at;

/**
 * Unit test for {@link ProductionGraphAwareRuntime}.
 */
public class ProductionGraphAwareRuntimeTest extends GraphAwareRuntimeTest {

    private GraphDatabaseService database;

    @Before
    public void setUp() {
        database = new TestGraphDatabaseFactory().newImpermanentDatabase();
    }

    @After
    public void tearDown() {
        database.shutdown();
    }

    @Test
    public void shouldCreateRuntimeRootNodeAfterFirstStartup() {
        assertNull(getRuntimeRoot());

        GraphAwareRuntime runtime = new ProductionGraphAwareRuntime(database);

        assertNull(getRuntimeRoot());

        runtime.start();

        assertNotNull(getRuntimeRoot());
    }

    @Test
    public void moduleRegisteredForTheFirstTimeShouldBeInitialized() {
        final GraphAwareRuntimeModule mockModule = createMockModule();

        GraphAwareRuntime runtime = new ProductionGraphAwareRuntime(database);
        runtime.registerModule(mockModule);

        runtime.start();

        verify(mockModule).initialize(database);
        verify(mockModule, atLeastOnce()).getConfiguration();
        verify(mockModule, atLeastOnce()).getId();
        verifyNoMoreInteractions(mockModule);

        try (Transaction tx = database.beginTx()) {
            assertEquals(Serializer.toString(NullRuntimeModuleConfiguration.getInstance(), CONFIG), getRuntimeRoot().getProperty(GA_PREFIX + RUNTIME + "_" + MOCK).toString());
        }
    }

    @Test
    public void moduleAlreadyRegisteredShouldNotBeInitialized() {
        final GraphAwareRuntimeModule mockModule = createMockModule();

        new SimpleTransactionExecutor(database).executeInTransaction(new VoidReturningCallback() {
            @Override
            protected void doInTx(GraphDatabaseService database) {
                createRuntimeRoot().setProperty(GA_PREFIX + RUNTIME + "_" + MOCK, Serializer.toString(NullRuntimeModuleConfiguration.getInstance(), CONFIG));
            }
        });

        GraphAwareRuntime runtime = new ProductionGraphAwareRuntime(database);
        runtime.registerModule(mockModule);

        runtime.start();

        verify(mockModule, atLeastOnce()).getId();
        verify(mockModule, atLeastOnce()).getConfiguration();
        verifyNoMoreInteractions(mockModule);

        try (Transaction tx = database.beginTx()) {
            assertEquals(Serializer.toString(NullRuntimeModuleConfiguration.getInstance(), CONFIG), getRuntimeRoot().getProperty(GA_PREFIX + RUNTIME + "_" + MOCK).toString());
        }
    }

    @Test
    public void changedModuleShouldBeReInitialized() {
        final GraphAwareRuntimeModule mockModule = createMockModule();

        try (Transaction tx = database.beginTx()) {
            createRuntimeRoot().setProperty(GA_PREFIX + RUNTIME + "_" + MOCK, CONFIG + "123");
            tx.success();
        }

        GraphAwareRuntime runtime = new ProductionGraphAwareRuntime(database);
        runtime.registerModule(mockModule);

        runtime.start();

        verify(mockModule).reinitialize(database);
        verify(mockModule, atLeastOnce()).getConfiguration();
        verify(mockModule, atLeastOnce()).getId();
        verifyNoMoreInteractions(mockModule);

        try (Transaction tx = database.beginTx()) {
            assertEquals(Serializer.toString(NullRuntimeModuleConfiguration.getInstance(), CONFIG), getRuntimeRoot().getProperty(GA_PREFIX + RUNTIME + "_" + MOCK).toString());
        }
    }

    @Test
    public void forcedModuleShouldBeReInitialized() {
        final GraphAwareRuntimeModule mockModule = createMockModule();

        try (Transaction tx = database.beginTx()) {
            createRuntimeRoot().setProperty(GA_PREFIX + RUNTIME + "_" + MOCK, FORCE_INITIALIZATION + "123");
            tx.success();
        }

        GraphAwareRuntime runtime = new ProductionGraphAwareRuntime(database);
        runtime.registerModule(mockModule);

        runtime.start();

        verify(mockModule).reinitialize(database);
        verify(mockModule, atLeastOnce()).getConfiguration();
        verify(mockModule, atLeastOnce()).getId();
        verifyNoMoreInteractions(mockModule);

        try (Transaction tx = database.beginTx()) {
            assertEquals(Serializer.toString(NullRuntimeModuleConfiguration.getInstance(), CONFIG), getRuntimeRoot().getProperty(GA_PREFIX + RUNTIME + "_" + MOCK).toString());
        }
    }

    @Test
    public void moduleAlreadyRegisteredShouldBeInitializedWhenForced() {
        final GraphAwareRuntimeModule mockModule = createMockModule();

        try (Transaction tx = database.beginTx()) {
            createRuntimeRoot().setProperty(GA_PREFIX + RUNTIME + "_" + MOCK, Serializer.toString(NullRuntimeModuleConfiguration.getInstance(), CONFIG));
            tx.success();
        }

        GraphAwareRuntime runtime = new ProductionGraphAwareRuntime(database);
        runtime.registerModule(mockModule, true);

        runtime.start();

        verify(mockModule).reinitialize(database);
        verify(mockModule, atLeastOnce()).getId();
        verify(mockModule, atLeastOnce()).getConfiguration();
        verifyNoMoreInteractions(mockModule);

        try (Transaction tx = database.beginTx()) {
            assertEquals(Serializer.toString(NullRuntimeModuleConfiguration.getInstance(), CONFIG), getRuntimeRoot().getProperty(GA_PREFIX + RUNTIME + "_" + MOCK).toString());
        }
    }

    @Test
    public void changedModuleShouldNotBeReInitializedWhenInitializationSkipped() {
        final GraphAwareRuntimeModule mockModule = createMockModule();

        try (Transaction tx = database.beginTx()) {
            createRuntimeRoot().setProperty(GA_PREFIX + RUNTIME + "_" + MOCK, CONFIG + "123");
            tx.success();
        }

        GraphAwareRuntime runtime = new ProductionGraphAwareRuntime(database);
        runtime.registerModule(mockModule);

        runtime.start(true);

        verify(mockModule, atLeastOnce()).getId();
        verifyNoMoreInteractions(mockModule);

        try (Transaction tx = database.beginTx()) {
            assertEquals(CONFIG + "123", getRuntimeRoot().getProperty(GA_PREFIX + RUNTIME + "_" + MOCK).toString());
        }
    }

    @Test(expected = IllegalStateException.class)
    public void shouldNotBeAbleToRegisterTheSameModuleTwice() {
        final GraphAwareRuntimeModule mockModule = createMockModule();

        GraphAwareRuntime runtime = new ProductionGraphAwareRuntime(database);
        runtime.registerModule(mockModule);
        runtime.registerModule(mockModule);
    }

    @Test(expected = IllegalStateException.class)
    public void shouldNotBeAbleToRegisterModuleWithTheSameIdTwice() {
        final GraphAwareRuntimeModule mockModule1 = createMockModule();
        final GraphAwareRuntimeModule mockModule2 = createMockModule();
        when(mockModule1.getId()).thenReturn("ID");
        when(mockModule2.getId()).thenReturn("ID");

        GraphAwareRuntime runtime = new ProductionGraphAwareRuntime(database);
        runtime.registerModule(mockModule1);
        runtime.registerModule(mockModule2);
    }

    @Test
    public void unusedModulesShouldBeRemoved() {
        final GraphAwareRuntimeModule mockModule = createMockModule();

        try (Transaction tx = database.beginTx()) {
            Node root = createRuntimeRoot();
            root.setProperty(GA_PREFIX + RUNTIME + "_" + MOCK, Serializer.toString(NullRuntimeModuleConfiguration.getInstance(), CONFIG));
            root.setProperty(GA_PREFIX + RUNTIME + "_UNUSED", CONFIG + "123");
            tx.success();
        }

        GraphAwareRuntime runtime = new ProductionGraphAwareRuntime(database);
        runtime.registerModule(mockModule);

        runtime.start();

        verify(mockModule, atLeastOnce()).getId();
        verify(mockModule, atLeastOnce()).getConfiguration();
        verifyNoMoreInteractions(mockModule);

        try (Transaction tx = database.beginTx()) {
            assertEquals(1, count(getRuntimeRoot().getPropertyKeys()));
        }
    }

    @Test(expected = IllegalStateException.class)
    public void usedCorruptModulesShouldThrowException() {
        final GraphAwareRuntimeModule mockModule = createMockModule();

        try (Transaction tx = database.beginTx()) {
            Node root = createRuntimeRoot();
            root.setProperty(GA_PREFIX + RUNTIME + "_" + MOCK, "CORRUPT");
            tx.success();
        }

        GraphAwareRuntime runtime = new ProductionGraphAwareRuntime(database);
        runtime.registerModule(mockModule);

        runtime.start();
    }

    @Test
    public void unusedCorruptModulesShouldBeRemoved() {
        final GraphAwareRuntimeModule mockModule = createMockModule();

        try (Transaction tx = database.beginTx()) {
            Node root = createRuntimeRoot();
            root.setProperty(GA_PREFIX + RUNTIME + "_" + MOCK, Serializer.toString(NullRuntimeModuleConfiguration.getInstance(), CONFIG));
            root.setProperty(GA_PREFIX + RUNTIME + "_UNUSED", "CORRUPT");
            tx.success();
        }

        GraphAwareRuntime runtime = new ProductionGraphAwareRuntime(database);
        runtime.registerModule(mockModule);

        runtime.start();

        verify(mockModule, atLeastOnce()).getId();
        verify(mockModule, atLeastOnce()).getConfiguration();
        verifyNoMoreInteractions(mockModule);

        try (Transaction tx = database.beginTx()) {
            assertEquals(1, count(getRuntimeRoot().getPropertyKeys()));
        }
    }

    @Test
    public void allRegisteredInterestedModulesShouldBeDelegatedTo() {
        GraphAwareRuntimeModule mockModule1 = mock(GraphAwareRuntimeModule.class);
        when(mockModule1.getId()).thenReturn(MOCK + "1");
        when(mockModule1.getConfiguration()).thenReturn(NullRuntimeModuleConfiguration.getInstance());

        GraphAwareRuntimeModule mockModule2 = mock(GraphAwareRuntimeModule.class);
        when(mockModule2.getId()).thenReturn(MOCK + "2");
        when(mockModule2.getConfiguration()).thenReturn(NullRuntimeModuleConfiguration.getInstance());

        GraphAwareRuntimeModule mockModule3 = mock(GraphAwareRuntimeModule.class);
        when(mockModule3.getId()).thenReturn(MOCK + "3");
        when(mockModule3.getConfiguration()).thenReturn(new MinimalRuntimeModuleConfiguration(InclusionStrategies.none()));

        GraphAwareRuntime runtime = new ProductionGraphAwareRuntime(database);
        runtime.registerModule(mockModule1);
        runtime.registerModule(mockModule2);
        runtime.registerModule(mockModule3);

        runtime.start();

        verify(mockModule1).initialize(database);
        verify(mockModule2).initialize(database);
        verify(mockModule3).initialize(database);
        verify(mockModule1, atLeastOnce()).getConfiguration();
        verify(mockModule2, atLeastOnce()).getConfiguration();
        verify(mockModule3, atLeastOnce()).getConfiguration();
        verify(mockModule1, atLeastOnce()).getId();
        verify(mockModule2, atLeastOnce()).getId();
        verify(mockModule3, atLeastOnce()).getId();
        verifyNoMoreInteractions(mockModule1, mockModule2, mockModule3);

        try (Transaction tx = database.beginTx()) {
            database.createNode();
            tx.success();
        }

        verify(mockModule1).beforeCommit(any(ImprovedTransactionData.class));
        verify(mockModule2).beforeCommit(any(ImprovedTransactionData.class));
        verify(mockModule1, atLeastOnce()).getConfiguration();
        verify(mockModule2, atLeastOnce()).getConfiguration();
        verify(mockModule3, atLeastOnce()).getConfiguration();

        //no interaction with module3, it is not interested!
        verifyNoMoreInteractions(mockModule1, mockModule2, mockModule3);
    }

    @Test
    public void moduleThrowingInitExceptionShouldBeMarkedForReinitialization() {
        final GraphAwareRuntimeModule mockModule = createMockModule();
        when(mockModule.getConfiguration()).thenReturn(NullRuntimeModuleConfiguration.getInstance());
        Mockito.doThrow(new NeedsInitializationException()).when(mockModule).beforeCommit(any(ImprovedTransactionData.class));

        GraphAwareRuntime runtime = new ProductionGraphAwareRuntime(database);
        runtime.registerModule(mockModule);

        runtime.start();

        try (Transaction tx = database.beginTx()) {
            assertTrue(getRuntimeRoot().getProperty(GA_PREFIX + RUNTIME + "_" + MOCK).toString().startsWith(CONFIG));
            tx.success();
        }

        try (Transaction tx = database.beginTx()) {
            database.createNode();
            tx.success();
        }

        try (Transaction tx = database.beginTx()) {
            assertTrue(getRuntimeRoot().getProperty(GA_PREFIX + RUNTIME + "_" + MOCK).toString().startsWith(FORCE_INITIALIZATION));
            tx.success();
        }
    }

    @Test
    public void moduleThrowingInitExceptionShouldBeMarkedForReinitializationOnlyTheFirstTime() throws InterruptedException {
        final GraphAwareRuntimeModule mockModule = createMockModule();
        when(mockModule.getConfiguration()).thenReturn(NullRuntimeModuleConfiguration.getInstance());
        doThrow(new NeedsInitializationException()).when(mockModule).beforeCommit(any(ImprovedTransactionData.class));

        GraphAwareRuntime runtime = new ProductionGraphAwareRuntime(database);
        runtime.registerModule(mockModule);

        runtime.start();

        try (Transaction tx = database.beginTx()) {
            database.createNode();
            tx.success();
        }

        long firstFailureTimestamp;
        try (Transaction tx = database.beginTx()) {
            firstFailureTimestamp = Long.valueOf(getRuntimeRoot().getProperty(GA_PREFIX + RUNTIME + "_" + MOCK).toString().replaceFirst(FORCE_INITIALIZATION, ""));
        }

        Thread.sleep(1);

        try (Transaction tx = database.beginTx()) {
            database.createNode();
            tx.success();
        }

        long secondFailureTimestamp;
        try (Transaction tx = database.beginTx()) {
            secondFailureTimestamp = Long.valueOf(getRuntimeRoot().getProperty(GA_PREFIX + RUNTIME + "_" + MOCK).toString().replaceFirst(FORCE_INITIALIZATION, ""));
        }

        assertEquals(firstFailureTimestamp, secondFailureTimestamp);
    }

    @Test(expected = IllegalStateException.class)
    public void modulesCannotBeRegisteredAfterStart() {
        final GraphAwareRuntimeModule mockModule = createMockModule();

        GraphAwareRuntime runtime = new ProductionGraphAwareRuntime(database);
        runtime.start(true);
        runtime.registerModule(mockModule);
    }

    @Test
    public void multipleCallsToStartFrameworkHaveNoEffect() {
        GraphAwareRuntime runtime = new ProductionGraphAwareRuntime(database);
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

        GraphAwareRuntime runtime = new ProductionGraphAwareRuntime(database);
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

        GraphAwareRuntime runtime = new ProductionGraphAwareRuntime(database);
        runtime.registerModule(mockModule, true);

        verify(mockModule).configurationChanged(DefaultRuntimeConfiguration.getInstance());
        verify(mockModule, atLeastOnce()).getId();
        verifyNoMoreInteractions(mockModule);
    }

    @Test
    public void realRuntimeConfiguredModulesShouldBeConfigured() {
        RealRuntimeConfiguredRuntimeModule module = new RealRuntimeConfiguredRuntimeModule();

        GraphAwareRuntime runtime = new ProductionGraphAwareRuntime(database);
        runtime.registerModule(module, true);

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

        GraphAwareRuntime runtime = new ProductionGraphAwareRuntime(database);
        runtime.registerModule(mockModule);
        runtime.start();

        database.shutdown();

        verify(mockModule).shutdown();
    }

    @Test(expected = TransactionFailureException.class)
    public void shouldNotBeAllowedToDeleteRuntimeRootNode() {
        GraphAwareRuntime runtime = new ProductionGraphAwareRuntime(database);
        runtime.start();

        try (Transaction tx = database.beginTx()) {
            getRuntimeRoot().delete();
            tx.success();
        }
    }

    @Test(expected = IllegalStateException.class)
    public void shouldFailWhenThereAreTwoRootNodes() {
        try (Transaction tx = database.beginTx()) {
            database.createNode(GA_ROOT);
            database.createNode(GA_ROOT);
            tx.success();
        }

        GraphAwareRuntime runtime = new ProductionGraphAwareRuntime(database);
        runtime.start();
    }

    @Test
    public void whenOneModuleThrowsAnExceptionThenOtherModulesShouldStillBeDelegatedTo() {
        GraphAwareRuntimeModule mockModule1 = mock(GraphAwareRuntimeModule.class);
        when(mockModule1.getId()).thenReturn(MOCK + "1");
        when(mockModule1.getConfiguration()).thenReturn(NullRuntimeModuleConfiguration.getInstance());
        doThrow(new RuntimeException()).when(mockModule1).beforeCommit(any(ImprovedTransactionData.class));

        GraphAwareRuntimeModule mockModule2 = mock(GraphAwareRuntimeModule.class);
        when(mockModule2.getId()).thenReturn(MOCK + "2");
        when(mockModule2.getConfiguration()).thenReturn(NullRuntimeModuleConfiguration.getInstance());

        GraphAwareRuntime runtime = new ProductionGraphAwareRuntime(database);
        runtime.registerModule(mockModule1);
        runtime.registerModule(mockModule2);

        runtime.start();

        verify(mockModule1).initialize(database);
        verify(mockModule2).initialize(database);
        verify(mockModule1, atLeastOnce()).getConfiguration();
        verify(mockModule2, atLeastOnce()).getConfiguration();
        verify(mockModule1, atLeastOnce()).getId();
        verify(mockModule2, atLeastOnce()).getId();
        verifyNoMoreInteractions(mockModule1, mockModule2);

        try (Transaction tx = database.beginTx()) {
            database.createNode();
            tx.success();
        }

        verify(mockModule1).beforeCommit(any(ImprovedTransactionData.class));
        verify(mockModule2).beforeCommit(any(ImprovedTransactionData.class));
        verify(mockModule1, atLeastOnce()).getId();
        verify(mockModule2, atLeastOnce()).getId();
        verify(mockModule1, atLeastOnce()).getConfiguration();
        verify(mockModule2, atLeastOnce()).getConfiguration();
        verifyNoMoreInteractions(mockModule1, mockModule2);
    }

    @Test
    public void runtimeShouldBeStartedAutomatically() {
        final GraphAwareRuntimeModule mockModule = createMockModule();

        GraphAwareRuntime runtime = new ProductionGraphAwareRuntime(database);
        runtime.registerModule(mockModule);

        try (Transaction tx = database.beginTx()) {
            database.createNode();
            tx.success();
        }

        verify(mockModule).initialize(database);
    }

    private Node getRuntimeRoot() {
        Node root = null;

        try (Transaction tx = database.beginTx()) {
            Iterator<Node> roots = at(database).getAllNodesWithLabel(GA_ROOT).iterator();
            if (roots.hasNext()) {
                root = roots.next();
            }

            if (roots.hasNext()) {
                throw new IllegalStateException("There is more than 1 runtime root node!");
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
