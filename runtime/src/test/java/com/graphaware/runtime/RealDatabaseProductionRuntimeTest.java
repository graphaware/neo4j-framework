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
import com.graphaware.runtime.config.NullTxDrivenModuleConfiguration;
import com.graphaware.runtime.metadata.*;
import com.graphaware.runtime.module.DeliberateTransactionRollbackException;
import com.graphaware.runtime.module.TimerDrivenModule;
import com.graphaware.runtime.module.TxDrivenModule;
import com.graphaware.tx.event.improved.api.ImprovedTransactionData;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.*;
import org.neo4j.test.TestGraphDatabaseFactory;

import java.util.Iterator;

import static com.graphaware.runtime.config.RuntimeConfiguration.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.neo4j.tooling.GlobalGraphOperations.at;

/**
 * Unit test for {@link ProductionRuntime}.
 */
public class RealDatabaseProductionRuntimeTest extends DatabaseRuntimeTest {

    protected ModuleMetadataRepository timerRepo;

    @Before
    public void setUp() {
        database = new TestGraphDatabaseFactory().newImpermanentDatabase();
        txRepo = new ProductionSingleNodeMetadataRepository(database, DefaultRuntimeConfiguration.getInstance(), TX_MODULES_PROPERTY_PREFIX);
        timerRepo = new ProductionSingleNodeMetadataRepository(database, DefaultRuntimeConfiguration.getInstance(), TIMER_MODULES_PROPERTY_PREFIX);
    }

    @After
    public void tearDown() {
        database.shutdown();
    }

    private TimerDrivenModule mockTimerModule() {
        return mockTimerModule(MOCK);
    }

    private TimerDrivenModule mockTimerModule(String id) {
        TimerDrivenModule mockModule = mock(TimerDrivenModule.class);
        when(mockModule.getId()).thenReturn(id);
        when(mockModule.createInitialContext(database)).thenReturn(null);

        return mockModule;
    }

    @Test(expected = TransactionFailureException.class)
    public void shouldNotBeAllowedToDeleteRuntimeMetadataNode() {
        GraphAwareRuntime runtime = GraphAwareRuntimeFactory.createRuntime(database);
        runtime.start();

        try (Transaction tx = database.beginTx()) {
            getMetadataNode().delete();
            tx.success();
        }
    }

    @Test
    public void moduleThrowingExceptionShouldRollbackTransaction() {
        TxDrivenModule mockModule = mockTxModule(MOCK);
        doThrow(new DeliberateTransactionRollbackException("Deliberate testing exception")).when(mockModule).beforeCommit(any(ImprovedTransactionData.class));

        GraphAwareRuntime runtime = createRuntime();
        runtime.registerModule(mockModule);
        runtime.start();

        try (Transaction tx = getTransaction()) {
            Node node = createNode();
            node.setProperty("test", "test");
            tx.success();
        } catch (Exception e) {
            //ok
        }

        assertEquals(1, countNodes()); //just the node for storing metadata
    }

    @Test(expected = IllegalStateException.class)
    public void shouldNotBeAbleToRegisterDifferentModulesWithSameId() {
        GraphAwareRuntime runtime = createRuntime();
        runtime.registerModule(mockTxModule());
        runtime.registerModule(mockTimerModule());
    }

    @Test(expected = IllegalStateException.class)
    public void shouldNotBeAbleToRegisterSameTimerModuleTwice() {
        GraphAwareRuntime runtime = createRuntime();
        runtime.registerModule(mockTimerModule());
        runtime.registerModule(mockTimerModule());
    }

    @Test
    public void unusedTimerModulesShouldBeRemoved() {
        final TimerDrivenModule mockModule = mockTimerModule();
        final TimerDrivenModule unusedModule = mockTimerModule("UNUSED");

        try (Transaction tx = getTransaction()) {
            timerRepo.persistModuleMetadata(mockModule, new DefaultTimerDrivenModuleMetadata(null));
            timerRepo.persistModuleMetadata(unusedModule, new DefaultTimerDrivenModuleMetadata(null));
            tx.success();
        }

        GraphAwareRuntime runtime = createRuntime();
        runtime.registerModule(mockModule);

        runtime.start();

        verify(mockModule, atLeastOnce()).getId();
        verifyNoMoreInteractions(mockModule);

        try (Transaction tx = getTransaction()) {
            assertEquals(1, timerRepo.getAllModuleIds().size());
        }
    }

    @Test
    public void corruptMetadataShouldNotKillTimerModule() {
        final TimerDrivenModule mockModule = mockTimerModule();

        try (Transaction tx = getTransaction()) {
            Node root = createMetadataNode();
            root.setProperty(GA_PREFIX + TX_MODULES_PROPERTY_PREFIX + "_" + MOCK, "CORRUPT");
            tx.success();
        }

        GraphAwareRuntime runtime = createRuntime();
        runtime.registerModule(mockModule);

        runtime.start();

        verify(mockModule, atLeastOnce()).getId();
        verify(mockModule).createInitialContext(database);
        verifyNoMoreInteractions(mockModule);

        try (Transaction tx = getTransaction()) {
            TimerDrivenModuleMetadata moduleMetadata = timerRepo.getModuleMetadata(mockModule);
            assertEquals(new DefaultTimerDrivenModuleMetadata(null), moduleMetadata);
        }
    }

    @Test
    public void allRegisteredInterestedTimerModulesShouldBeDelegatedTo() throws InterruptedException {
        TimerDrivenModule mockModule1 = mockTimerModule(MOCK + "1");
        TimerDrivenModule mockModule2 = mockTimerModule(MOCK + "2");
        TimerDrivenModule mockModule3 = mockTimerModule(MOCK + "3");

        GraphAwareRuntime runtime = createRuntime();
        runtime.registerModule(mockModule1);
        runtime.registerModule(mockModule2);
        runtime.registerModule(mockModule3);

        runtime.start();

        verify(mockModule1, atLeastOnce()).getId();
        verify(mockModule2, atLeastOnce()).getId();
        verify(mockModule3, atLeastOnce()).getId();
        verify(mockModule1).createInitialContext(database);
        verify(mockModule2).createInitialContext(database);
        verify(mockModule3).createInitialContext(database);
        verifyNoMoreInteractions(mockModule1, mockModule2, mockModule3);

        Thread.sleep(INITIAL_TIMER_DELAY + 5 * TIMER_DELAY - 100);

        verify(mockModule1, atLeastOnce()).getId();
        verify(mockModule2, atLeastOnce()).getId();
        verify(mockModule3, atLeastOnce()).getId();
        verify(mockModule1, times(2)).doSomeWork(null, database);
        verify(mockModule2, times(2)).doSomeWork(null, database);
        verify(mockModule3).doSomeWork(null, database);

        verifyNoMoreInteractions(mockModule1, mockModule2, mockModule3);
    }

    @Test
    public void earliestNextCallTimeShouldBeRespected() throws InterruptedException {
        TimerDrivenModule mockModule1 = mockTimerModule(MOCK + "1");
        TimerDrivenModule mockModule3 = mockTimerModule(MOCK + "3");
        NodeBasedContext context1 = new NodeBasedContext(0, System.currentTimeMillis() + INITIAL_TIMER_DELAY + 3 * TIMER_DELAY);
        NodeBasedContext context2 = new NodeBasedContext(1, TimerDrivenModuleContext.ASAP);

        TimerDrivenModule mockModule2 = mock(TimerDrivenModule.class);
        when(mockModule2.getId()).thenReturn(MOCK + "2");
        when(mockModule2.createInitialContext(database)).thenReturn(context1);
        when(mockModule2.doSomeWork(context1, database)).thenReturn(context2);

        GraphAwareRuntime runtime = createRuntime();
        runtime.registerModule(mockModule1);
        runtime.registerModule(mockModule2);
        runtime.registerModule(mockModule3);

        runtime.start();

        verify(mockModule1, atLeastOnce()).getId();
        verify(mockModule2, atLeastOnce()).getId();
        verify(mockModule3, atLeastOnce()).getId();
        verify(mockModule1).createInitialContext(database);
        verify(mockModule2).createInitialContext(database);
        verify(mockModule3).createInitialContext(database);
        verifyNoMoreInteractions(mockModule1, mockModule2, mockModule3);

        Thread.sleep(INITIAL_TIMER_DELAY + 8 * TIMER_DELAY - 100);

        verify(mockModule1, atLeastOnce()).getId();
        verify(mockModule2, atLeastOnce()).getId();
        verify(mockModule3, atLeastOnce()).getId();
        verify(mockModule1, times(3)).doSomeWork(null, database);
        verify(mockModule2).doSomeWork(context1, database);
        verify(mockModule2).doSomeWork(context2, database);
        verify(mockModule3, times(3)).doSomeWork(null, database);

        verifyNoMoreInteractions(mockModule1, mockModule2, mockModule3);
    }

    @Test
    public void nothingShouldHappenWhenNoModuleWantsToRun() throws InterruptedException {
        TimerDrivenModule mockModule1 = mockTimerModule(MOCK + "1");
        TimerDrivenModule mockModule2 = mockTimerModule(MOCK + "2");
        NodeBasedContext context = new NodeBasedContext(0, System.currentTimeMillis() + 1000000);

        when(mockModule1.createInitialContext(database)).thenReturn(context);
        when(mockModule2.createInitialContext(database)).thenReturn(context);

        GraphAwareRuntime runtime = createRuntime();
        runtime.registerModule(mockModule1);
        runtime.registerModule(mockModule2);

        runtime.start();

        verify(mockModule1, atLeastOnce()).getId();
        verify(mockModule2, atLeastOnce()).getId();
        verify(mockModule1).createInitialContext(database);
        verify(mockModule2).createInitialContext(database);
        verifyNoMoreInteractions(mockModule1, mockModule2);

        Thread.sleep(INITIAL_TIMER_DELAY + 10 * TIMER_DELAY);

        verify(mockModule1, atLeastOnce()).getId();
        verify(mockModule2, atLeastOnce()).getId();

        verifyNoMoreInteractions(mockModule1, mockModule2);
    }

    @Test
    public void allRegisteredInterestedTimerModulesShouldBeDelegatedToWhenRuntimeNotExplicitlyStarted() throws InterruptedException {
        TimerDrivenModule mockModule1 = mockTimerModule(MOCK + "1");
        TimerDrivenModule mockModule2 = mockTimerModule(MOCK + "2");
        TimerDrivenModule mockModule3 = mockTimerModule(MOCK + "3");

        GraphAwareRuntime runtime = createRuntime();
        runtime.registerModule(mockModule1);
        runtime.registerModule(mockModule2);
        runtime.registerModule(mockModule3);

        //no explicit runtime start!
        try (Transaction tx = getTransaction()) {
            createNode();
            tx.success();
        }

        verify(mockModule1, atLeastOnce()).getId();
        verify(mockModule2, atLeastOnce()).getId();
        verify(mockModule3, atLeastOnce()).getId();
        verify(mockModule1).createInitialContext(database);
        verify(mockModule2).createInitialContext(database);
        verify(mockModule3).createInitialContext(database);
        verifyNoMoreInteractions(mockModule1, mockModule2, mockModule3);

        Thread.sleep(INITIAL_TIMER_DELAY + 5 * TIMER_DELAY - 100);

        verify(mockModule1, atLeastOnce()).getId();
        verify(mockModule2, atLeastOnce()).getId();
        verify(mockModule3, atLeastOnce()).getId();
        verify(mockModule1, times(2)).doSomeWork(null, database);
        verify(mockModule2, times(2)).doSomeWork(null, database);
        verify(mockModule3).doSomeWork(null, database);

        verifyNoMoreInteractions(mockModule1, mockModule2, mockModule3);
    }

    @Test
    public void lastContextShouldBePresentedInNextCallAndPersisted() throws InterruptedException {
        TimerDrivenModuleContext<Node> firstContext = new NodeBasedContext(1);
        TimerDrivenModuleContext<Node> secondContext = new NodeBasedContext(2);

        TimerDrivenModule mockModule = mockTimerModule();
        when(mockModule.doSomeWork(null, database)).thenReturn(firstContext);
        when(mockModule.doSomeWork(firstContext, database)).thenReturn(secondContext);

        GraphAwareRuntime runtime = createRuntime();
        runtime.registerModule(mockModule);

        runtime.start();

        verify(mockModule, atLeastOnce()).getId();
        verify(mockModule).createInitialContext(database);
        verifyNoMoreInteractions(mockModule);

        Thread.sleep(INITIAL_TIMER_DELAY + 2 * TIMER_DELAY - 100);

        verify(mockModule, atLeastOnce()).getId();
        verify(mockModule).doSomeWork(null, database);
        verify(mockModule).doSomeWork(firstContext, database);

        verifyNoMoreInteractions(mockModule);

        try (Transaction tx = getTransaction()) {
            TimerDrivenModuleMetadata moduleMetadata = timerRepo.getModuleMetadata(mockModule);
            assertEquals(new DefaultTimerDrivenModuleMetadata(new NodeBasedContext(2)), moduleMetadata);
        }
    }

    @Test
    public void lastContextShouldBePresentedAfterRestart() throws InterruptedException {
        TimerDrivenModule mockModule = mockTimerModule();

        TimerDrivenModuleContext<Node> lastContext = new NodeBasedContext(1);
        try (Transaction tx = database.beginTx()) {
            timerRepo.persistModuleMetadata(mockModule, new DefaultTimerDrivenModuleMetadata(lastContext));
            tx.success();
        }

        GraphAwareRuntime runtime = createRuntime();
        runtime.registerModule(mockModule);

        runtime.start();

        Thread.sleep(INITIAL_TIMER_DELAY + TIMER_DELAY - 100);

        verify(mockModule, atLeastOnce()).getId();
        verify(mockModule).doSomeWork(lastContext, database);
        verifyNoMoreInteractions(mockModule);
    }

    @Test
    public void shutdownShouldBeCalledBeforeShutdownOnTimerDrivenModules() {
        TimerDrivenModule mockModule = mockTimerModule();

        GraphAwareRuntime runtime = createRuntime();
        runtime.registerModule(mockModule);
        runtime.start();

        shutdown();

        verify(mockModule).shutdown();
    }

    @Test
    public void whenOneTimerModuleThrowsAnExceptionThenOtherModulesShouldStillBeDelegatedTo() throws InterruptedException {
        TimerDrivenModule mockModule1 = mockTimerModule(MOCK + "1");
        when(mockModule1.doSomeWork(any(TimerDrivenModuleContext.class), any(GraphDatabaseService.class))).thenThrow(new RuntimeException("deliberate testing exception"));

        TimerDrivenModule mockModule2 = mockTimerModule(MOCK + "2");

        GraphAwareRuntime runtime = createRuntime();
        runtime.registerModule(mockModule1);
        runtime.registerModule(mockModule2);

        runtime.start();

        verify(mockModule1, atLeastOnce()).getId();
        verify(mockModule2, atLeastOnce()).getId();
        verify(mockModule1).createInitialContext(database);
        verify(mockModule2).createInitialContext(database);
        verifyNoMoreInteractions(mockModule1, mockModule2);

        Thread.sleep(INITIAL_TIMER_DELAY + 2 * TIMER_DELAY - 100);

        verify(mockModule1, atLeastOnce()).getId();
        verify(mockModule2, atLeastOnce()).getId();
        verify(mockModule1).doSomeWork(null, database);
        verify(mockModule2).doSomeWork(null, database);
        verifyNoMoreInteractions(mockModule1, mockModule2);
    }

    @Test
    public void sameModuleCanActAsTxAndTimerDriven() throws InterruptedException {
        TxAndTimerDrivenModule mockModule = mock(TxAndTimerDrivenModule.class);
        when(mockModule.getId()).thenReturn(MOCK);
        when(mockModule.createInitialContext(database)).thenReturn(null);
        when(mockModule.getConfiguration()).thenReturn(NullTxDrivenModuleConfiguration.getInstance());

        GraphAwareRuntime runtime = createRuntime();
        runtime.registerModule(mockModule);

        runtime.start();

        try (Transaction tx = database.beginTx()) {
            database.createNode();
            tx.success();
        }

        Thread.sleep(INITIAL_TIMER_DELAY + TIMER_DELAY - 100);

        verify(mockModule).initialize(database);
        verify(mockModule).start(database);
        verify(mockModule).createInitialContext(database);
        verify(mockModule).doSomeWork(null, database);
        verify(mockModule).beforeCommit(any(ImprovedTransactionData.class));
        verify(mockModule).afterCommit(null);
        verify(mockModule, atLeastOnce()).getId();
        verify(mockModule, atLeastOnce()).getConfiguration();
        verifyNoMoreInteractions(mockModule);
    }

    @Test
    public void modulesShouldBeAwareOfRollbackAfterConstraintViolation() {
        Node node1;

        try (Transaction tx = getTransaction()) {
            node1 = createNode();
            node1.createRelationshipTo(createNode(), DynamicRelationshipType.withName("TEST"));
            tx.success();
        }

        TxDrivenModule mockModule1 = mockTxModule(MOCK + "1");
        TxDrivenModule mockModule2 = mockTxModule(MOCK + "2");

        GraphAwareRuntime runtime = createRuntime();
        runtime.registerModule(mockModule1);
        runtime.registerModule(mockModule2);

        runtime.start();

        verifyInitialization(mockModule1);
        verifyInitialization(mockModule2);
        verifyStart(mockModule1);
        verifyStart(mockModule2);
        verify(mockModule1, atLeastOnce()).getConfiguration();
        verify(mockModule2, atLeastOnce()).getConfiguration();
        verify(mockModule1, atLeastOnce()).getId();
        verify(mockModule2, atLeastOnce()).getId();
        verifyNoMoreInteractions(mockModule1, mockModule2);

        try {
            try (Transaction tx = getTransaction()) {
                node1.delete();
                tx.success();
            }
            fail();
        } catch (RuntimeException e) {
            //expected
        }

        verify(mockModule1).beforeCommit(any(ImprovedTransactionData.class));
        verify(mockModule2).beforeCommit(any(ImprovedTransactionData.class));
        verify(mockModule1).afterRollback("TEST_" + MOCK + "1");
        verify(mockModule2).afterRollback("TEST_" + MOCK + "2");
        verify(mockModule1, atLeastOnce()).getId();
        verify(mockModule2, atLeastOnce()).getId();
        verify(mockModule1, atLeastOnce()).getConfiguration();
        verify(mockModule2, atLeastOnce()).getConfiguration();
        verifyNoMoreInteractions(mockModule1, mockModule2);
    }

    interface TxAndTimerDrivenModule extends TxDrivenModule, TimerDrivenModule {

    }

    protected Node getMetadataNode() {
        Node root = null;

        try (Transaction tx = database.beginTx()) {
            Iterator<Node> roots = at(database).getAllNodesWithLabel(GA_METADATA).iterator();
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

    protected Node createMetadataNode() {
        Node root;

        try (Transaction tx = database.beginTx()) {
            if (getMetadataNode() != null) {
                throw new IllegalArgumentException("Runtime root already exists!");
            }
            root = database.createNode(GA_METADATA);
            tx.success();
        }

        return root;
    }
}
