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

import com.graphaware.runtime.config.*;
import com.graphaware.runtime.metadata.*;
import com.graphaware.runtime.module.DeliberateTransactionRollbackException;
import com.graphaware.runtime.module.TimerDrivenModule;
import com.graphaware.runtime.module.TxDrivenModule;
import com.graphaware.runtime.schedule.AdaptiveTimingStrategy;
import com.graphaware.runtime.schedule.FixedDelayTimingStrategy;
import com.graphaware.runtime.schedule.TimingStrategy;
import com.graphaware.tx.event.improved.api.ImprovedTransactionData;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.*;
import org.neo4j.test.TestGraphDatabaseFactory;

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.graphaware.runtime.config.RuntimeConfiguration.*;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.neo4j.tooling.GlobalGraphOperations.at;

/**
 * Unit test for {@link ProductionRuntime}.
 */
public class RealDatabaseProductionRuntimeTest extends DatabaseRuntimeTest {

    public static final int DELAY = 200;
    public static final int INITIAL_DELAY = 1000;

    public static final TimingStrategy TIMING_STRATEGY = FixedDelayTimingStrategy
            .getInstance()
            .withDelay(DELAY)
            .withInitialDelay(INITIAL_DELAY);

    protected ModuleMetadataRepository timerRepo;

    @Before
    public void setUp() {
        database = new TestGraphDatabaseFactory().newImpermanentDatabase();
        txRepo = new ProductionSingleNodeMetadataRepository(database, FluentRuntimeConfiguration.defaultConfiguration(), TX_MODULES_PROPERTY_PREFIX);
        timerRepo = new ProductionSingleNodeMetadataRepository(database, FluentRuntimeConfiguration.defaultConfiguration(), TIMER_MODULES_PROPERTY_PREFIX);
    }

    @After
    public void tearDown() {
        database.shutdown();
    }

    @Override
    protected RuntimeConfiguration createTestRuntimeConfiguration() {
        return FluentRuntimeConfiguration.defaultConfiguration().withTimingStrategy(TIMING_STRATEGY);
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

    @Test(expected = IllegalStateException.class)
    public void shouldNotBeAllowedToCreateTwoRuntimes() {
        GraphAwareRuntimeFactory.createRuntime(database);
        GraphAwareRuntimeFactory.createRuntime(database);
    }

    @Test
    public void nullShouldBeReturnedWhenNoRuntimeHasBeenRegisteredForDatabase() {
        assertNull(ProductionRuntime.getRuntime(database));
    }

    @Test(expected = IllegalStateException.class)
    public void exceptionShouldBeThrownWhenNoRuntimeHasBeenRegisteredForDatabase() {
        assertNull(ProductionRuntime.getStartedRuntime(database));
    }

    @Test
    public void registeredRuntimeShouldBeRetrieved() {
        GraphAwareRuntime runtime = GraphAwareRuntimeFactory.createRuntime(database);
        assertEquals(runtime, ProductionRuntime.getRuntime(database));
    }

    @Test
    public void registeredRuntimeShouldBeRetrieved2() {
        GraphAwareRuntime runtime = GraphAwareRuntimeFactory.createRuntime(database);
        assertEquals(runtime, ProductionRuntime.getStartedRuntime(database));
    }

    @Test(expected = IllegalStateException.class)
    public void shouldFailWaitingForRuntimeThatHasNotBeenStarted() {
        GraphAwareRuntime runtime = GraphAwareRuntimeFactory.createRuntime(database);
        runtime.waitUntilStarted();
    }

    @Test
    public void shouldWaitForRuntimeToStart() throws InterruptedException {
        final GraphAwareRuntime runtime = GraphAwareRuntimeFactory.createRuntime(database);

        final AtomicBoolean finished = new AtomicBoolean(false);
        new Thread(new Runnable() {
            @Override
            public void run() {
                runtime.start();
                finished.set(true);
            }
        }).start();

        runtime.waitUntilStarted();
        Thread.sleep(5);

        assertTrue(finished.get());
    }

    @Test(expected = IllegalStateException.class)
    public void shouldFailWhenStartIsNotCalledInOneSecond() {
        final GraphAwareRuntime runtime = GraphAwareRuntimeFactory.createRuntime(database);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1200);
                } catch (InterruptedException e) {
                    //do nothing
                }
                runtime.start();
            }
        }).start();

        runtime.waitUntilStarted();
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

        Thread.sleep(INITIAL_DELAY + 5 * DELAY - 50);

        verify(mockModule1, atLeastOnce()).getId();
        verify(mockModule2, atLeastOnce()).getId();
        verify(mockModule3, atLeastOnce()).getId();
        verify(mockModule1, times(2)).doSomeWork(null, database);
        verify(mockModule2, times(2)).doSomeWork(null, database);
        verify(mockModule3).doSomeWork(null, database);

        verifyNoMoreInteractions(mockModule1, mockModule2, mockModule3);
    }

    @Test
    public void allRegisteredInterestedTimerModulesShouldBeDelegatedToWithAdaptiveStrategy() throws InterruptedException {
        TimerDrivenModule mockModule1 = mockTimerModule(MOCK + "1");
        TimerDrivenModule mockModule2 = mockTimerModule(MOCK + "2");

        GraphAwareRuntime runtime = GraphAwareRuntimeFactory.createRuntime(database, FluentRuntimeConfiguration
                .defaultConfiguration()
                .withTimingStrategy(
                        AdaptiveTimingStrategy
                                .defaultConfiguration()
                                .withDefaultDelayMillis(50)));

        runtime.registerModule(mockModule1);
        runtime.registerModule(mockModule2);

        runtime.start();

        verify(mockModule1, atLeastOnce()).getId();
        verify(mockModule2, atLeastOnce()).getId();
        verify(mockModule1).createInitialContext(database);
        verify(mockModule2).createInitialContext(database);
        verifyNoMoreInteractions(mockModule1, mockModule2);

        Thread.sleep(200);

        verify(mockModule1, atLeastOnce()).getId();
        verify(mockModule2, atLeastOnce()).getId();
        verify(mockModule1, atLeastOnce()).doSomeWork(null, database);
        verify(mockModule2, atLeastOnce()).doSomeWork(null, database);
    }

    @Test
    public void earliestNextCallTimeShouldBeRespected() throws InterruptedException {
        TimerDrivenModule mockModule1 = mockTimerModule(MOCK + "1");
        TimerDrivenModule mockModule3 = mockTimerModule(MOCK + "3");
        NodeBasedContext context1 = new NodeBasedContext(0, System.currentTimeMillis() + INITIAL_DELAY + 3 * DELAY);
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

        Thread.sleep(INITIAL_DELAY + 8 * DELAY - 100);

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

        Thread.sleep(INITIAL_DELAY + 10 * DELAY);

        verify(mockModule1, atLeastOnce()).getId();
        verify(mockModule2, atLeastOnce()).getId();

        verifyNoMoreInteractions(mockModule1, mockModule2);
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

        Thread.sleep(INITIAL_DELAY + 2 * DELAY - 100);

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

        Thread.sleep(INITIAL_DELAY + DELAY - 100);

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

        Thread.sleep(INITIAL_DELAY + 2 * DELAY - 100);

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

        Thread.sleep(INITIAL_DELAY + DELAY - 100);

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

    @Test
    public void shouldObtainModulesOfCorrectTypes() {
        TxAndTimerDrivenModule mockModule1 = mock(TxAndTimerDrivenModule.class);
        when(mockModule1.getId()).thenReturn(MOCK + "1");
        TxDrivenModule mockModule2 = mockTxModule(MOCK + "2");
        TimerDrivenModule mockModule3 = mockTimerModule(MOCK + "3");

        GraphAwareRuntime runtime = createRuntime();
        runtime.registerModule(mockModule1);
        runtime.registerModule(mockModule2);
        runtime.registerModule(mockModule3);

        assertEquals(mockModule1, runtime.getModule(MOCK + "1", TxAndTimerDrivenModule.class));
        assertEquals(mockModule1, runtime.getModule(MOCK + "1", TxDrivenModule.class));
        assertEquals(mockModule1, runtime.getModule(MOCK + "1", TimerDrivenModule.class));
        assertEquals(mockModule2, runtime.getModule(MOCK + "2", TxDrivenModule.class));
        assertEquals(mockModule3, runtime.getModule(MOCK + "3", TimerDrivenModule.class));
    }

    @Test(expected = NotFoundException.class)
    public void shouldThrowExceptionWhenAskedForNonExistingModule() {
        GraphAwareRuntime runtime = createRuntime();
        runtime.getModule("non-existing", TxAndTimerDrivenModule.class);
    }

    @Test(expected = NotFoundException.class)
    public void shouldThrowExceptionWhenAskedForWrongModuleType() {
        TxAndTimerDrivenModule mockModule1 = mock(TxAndTimerDrivenModule.class);
        when(mockModule1.getId()).thenReturn(MOCK + "1");
        TxDrivenModule mockModule2 = mockTxModule(MOCK + "2");
        TimerDrivenModule mockModule3 = mockTimerModule(MOCK + "3");

        GraphAwareRuntime runtime = createRuntime();
        runtime.registerModule(mockModule1);
        runtime.registerModule(mockModule2);
        runtime.registerModule(mockModule3);

        runtime.getModule(MOCK + "3", TxDrivenModule.class);
    }

    @Test(expected = NotFoundException.class)
    public void shouldThrowExceptionWhenAskedForWrongModuleType2() {
        TxAndTimerDrivenModule mockModule1 = mock(TxAndTimerDrivenModule.class);
        when(mockModule1.getId()).thenReturn(MOCK + "1");
        TxDrivenModule mockModule2 = mockTxModule(MOCK + "2");
        TimerDrivenModule mockModule3 = mockTimerModule(MOCK + "3");

        GraphAwareRuntime runtime = createRuntime();
        runtime.registerModule(mockModule1);
        runtime.registerModule(mockModule2);
        runtime.registerModule(mockModule3);

        runtime.getModule(MOCK + "2", TimerDrivenModule.class);
    }

    interface TxAndTimerDrivenModule extends TxDrivenModule, TimerDrivenModule {

    }

    @Override
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

    @Override
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
