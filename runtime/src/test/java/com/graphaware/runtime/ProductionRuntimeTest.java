/*
 * Copyright (c) 2013-2017 GraphAware
 *
 * This file is part of the GraphAware Framework.
 *
 * GraphAware Framework is free software: you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received a copy of
 * the GNU General Public License along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 */

package com.graphaware.runtime;

import com.graphaware.common.kv.GraphKeyValueStore;
import com.graphaware.common.kv.KeyValueStore;
import com.graphaware.common.policy.inclusion.InclusionPolicies;
import com.graphaware.runtime.config.*;
import com.graphaware.runtime.metadata.*;
import com.graphaware.runtime.module.*;
import com.graphaware.runtime.schedule.AdaptiveTimingStrategy;
import com.graphaware.runtime.schedule.FixedDelayTimingStrategy;
import com.graphaware.runtime.schedule.TimingStrategy;
import com.graphaware.runtime.write.WritingConfig;
import com.graphaware.tx.event.improved.api.ImprovedTransactionData;
import com.graphaware.writer.neo4j.BaseNeo4jWriter;
import com.graphaware.writer.neo4j.Neo4jWriter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.neo4j.backup.OnlineBackupSettings;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.event.KernelEventHandler;
import org.neo4j.helpers.collection.Iterables;
import org.neo4j.shell.ShellSettings;
import org.neo4j.test.TestGraphDatabaseFactory;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.graphaware.common.util.DatabaseUtils.registerShutdownHook;
import static com.graphaware.runtime.GraphAwareRuntimeFactory.createRuntime;
import static com.graphaware.runtime.config.FluentRuntimeConfiguration.*;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.neo4j.kernel.configuration.Settings.FALSE;

/**
 * Unit test for {@link ProductionRuntime}.
 */
public class ProductionRuntimeTest {

    private static final int DELAY = 200;
    private static final int INITIAL_DELAY = 1000;

    private static final TimingStrategy TIMING_STRATEGY = FixedDelayTimingStrategy
            .getInstance()
            .withDelay(DELAY)
            .withInitialDelay(INITIAL_DELAY);

    private static final String MOCK = "MOCK";

    private ModuleMetadataRepository timerRepo;
    private GraphDatabaseService database;
    private ModuleMetadataRepository txRepo;
    private KeyValueStore keyValueStore;

    @Before
    public void setUp() {
        database = new TestGraphDatabaseFactory()
                .newImpermanentDatabaseBuilder()
                .setConfig(OnlineBackupSettings.online_backup_enabled, FALSE)
                .setConfig(ShellSettings.remote_shell_enabled, FALSE)
                .newGraphDatabase();

        registerShutdownHook(database);

        txRepo = new GraphPropertiesMetadataRepository(database, defaultConfiguration(database), TX_MODULES_PROPERTY_PREFIX);
        timerRepo = new GraphPropertiesMetadataRepository(database, defaultConfiguration(database), TIMER_MODULES_PROPERTY_PREFIX);
        keyValueStore = new GraphKeyValueStore(database);
    }

    @After
    public void tearDown() {
        database.shutdown();
    }

    @Test(expected = IllegalStateException.class)
    public void shouldNotBeAllowedToCreateTwoRuntimes() {
        createRuntime(database);
        createRuntime(database);
    }

    @Test
    public void nullShouldBeReturnedWhenNoRuntimeHasBeenRegisteredForDatabase() {
        assertNull(RuntimeRegistry.getRuntime(database));
    }

    @Test(expected = IllegalStateException.class)
    public void exceptionShouldBeThrownWhenNoRuntimeHasBeenRegisteredForDatabase() {
        assertNull(RuntimeRegistry.getStartedRuntime(database));
    }

    @Test(expected = IllegalStateException.class)
    public void exceptionShouldBeThrownWhenRuntimeHasNotBeenStarted() {
        createRuntime(database);
        RuntimeRegistry.getStartedRuntime(database);
    }

    @Test
    public void registeredRuntimeShouldBeRetrieved() {
        GraphAwareRuntime runtime = createRuntime(database);
        assertEquals(runtime, RuntimeRegistry.getRuntime(database));
    }

    @Test
    public void registeredRuntimeShouldBeRetrieved2() {
        GraphAwareRuntime runtime = createRuntime(database);
        runtime.start();
        assertEquals(runtime, RuntimeRegistry.getStartedRuntime(database));
    }

    @Test(expected = IllegalStateException.class)
    public void shouldFailWaitingForRuntimeThatHasNotBeenStarted() {
        GraphAwareRuntime runtime = createRuntime(database);
        runtime.waitUntilStarted();
    }

    @Test
    public void shouldWaitForRuntimeToStart() throws InterruptedException {
        final GraphAwareRuntime runtime = createRuntime(database);

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
        final GraphAwareRuntime runtime = createRuntime(database);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1500);
                } catch (InterruptedException e) {
                    //do nothing
                }
                runtime.start();
            }
        }).start();

        runtime.waitUntilStarted();
    }

    @Test
    public void moduleThrowingExceptionShouldRollbackTransaction() {
        TxDrivenModule mockModule = mockTxModule(MOCK);
        doThrow(new DeliberateTransactionRollbackException("Deliberate testing exception")).when(mockModule).beforeCommit(any(ImprovedTransactionData.class));

        GraphAwareRuntime runtime = createRuntime(database, defaultConfiguration(database).withTimingStrategy(TIMING_STRATEGY));
        runtime.registerModule(mockModule);
        runtime.start();

        try (Transaction tx = database.beginTx()) {
            Node node = database.createNode(new Label[]{});
            node.setProperty("test", "test");
            tx.success();
        } catch (Exception e) {
            //ok
        }

        try (Transaction tx = database.beginTx()) {
            assertEquals(0, Iterables.count(database.getAllNodes()));
            tx.success();
        }
    }

    @Test
    public void moduleThrowingRuntimeExceptionShouldRollbackTransaction() {
        TxDrivenModule mockModule = mockTxModule(MOCK);
        doThrow(new RuntimeException("Deliberate testing exception")).when(mockModule).beforeCommit(any(ImprovedTransactionData.class));

        GraphAwareRuntime runtime = createRuntime(database, defaultConfiguration(database).withTimingStrategy(TIMING_STRATEGY));
        runtime.registerModule(mockModule);
        runtime.start();

        try (Transaction tx = database.beginTx()) {
            Node node = database.createNode(new Label[]{});
            node.setProperty("test", "test");
            tx.success();
        } catch (Exception e) {
            //ok
        }

        try (Transaction tx = database.beginTx()) {
            assertEquals(0, Iterables.count(database.getAllNodes()));
            tx.success();
        }
    }

    @Test(expected = IllegalStateException.class)
    public void shouldNotBeAbleToRegisterDifferentModulesWithSameId() {
        GraphAwareRuntime runtime = createRuntime(database, defaultConfiguration(database).withTimingStrategy(TIMING_STRATEGY));
        runtime.registerModule(mockTxModule());
        runtime.registerModule(mockTimerModule());
    }

    @Test(expected = IllegalStateException.class)
    public void shouldNotBeAbleToRegisterSameTimerModuleTwice() {
        GraphAwareRuntime runtime = createRuntime(database, defaultConfiguration(database).withTimingStrategy(TIMING_STRATEGY));
        runtime.registerModule(mockTimerModule());
        runtime.registerModule(mockTimerModule());
    }

    @Test
    public void unusedTimerModulesShouldBeRemoved() {
        final TimerDrivenModule mockModule = mockTimerModule();
        final TimerDrivenModule unusedModule = mockTimerModule("UNUSED");

        try (Transaction tx = database.beginTx()) {
            timerRepo.persistModuleMetadata(mockModule, new DefaultTimerDrivenModuleMetadata(null));
            timerRepo.persistModuleMetadata(unusedModule, new DefaultTimerDrivenModuleMetadata(null));
            tx.success();
        }

        GraphAwareRuntime runtime = createRuntime(database, defaultConfiguration(database).withTimingStrategy(TIMING_STRATEGY));
        runtime.registerModule(mockModule);

        runtime.start();

        verify(mockModule, atLeastOnce()).getId();
        verifyNoMoreInteractions(mockModule);

        try (Transaction tx = database.beginTx()) {
            assertEquals(1, timerRepo.getAllModuleIds().size());
        }
    }

    @Test
    public void corruptMetadataShouldNotKillTimerModule() {
        final TimerDrivenModule mockModule = mockTimerModule();

        try (Transaction tx = database.beginTx()) {
            keyValueStore.set(GA_PREFIX + TX_MODULES_PROPERTY_PREFIX + "_" + MOCK, "CORRUPT");
            tx.success();
        }

        GraphAwareRuntime runtime = createRuntime(database, defaultConfiguration(database).withTimingStrategy(TIMING_STRATEGY));
        runtime.registerModule(mockModule);

        runtime.start();

        verify(mockModule, atLeastOnce()).getId();
        verify(mockModule).createInitialContext(database);
        verifyNoMoreInteractions(mockModule);

        try (Transaction tx = database.beginTx()) {
            TimerDrivenModuleMetadata moduleMetadata = timerRepo.getModuleMetadata(mockModule);
            assertEquals(new DefaultTimerDrivenModuleMetadata(null), moduleMetadata);
        }
    }

    @Test
    public void allRegisteredInterestedTimerModulesShouldBeDelegatedTo() throws InterruptedException {
        TimerDrivenModule mockModule1 = mockTimerModule(MOCK + "1");
        TimerDrivenModule mockModule2 = mockTimerModule(MOCK + "2");
        TimerDrivenModule mockModule3 = mockTimerModule(MOCK + "3");

        GraphAwareRuntime runtime = createRuntime(database, defaultConfiguration(database).withTimingStrategy(TIMING_STRATEGY));
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
        verify(mockModule1, atLeastOnce()).getConfiguration();
        verify(mockModule2, atLeastOnce()).getConfiguration();
        verify(mockModule3, atLeastOnce()).getConfiguration();
        verify(mockModule1, times(2)).doSomeWork(null, database);
        verify(mockModule2, times(2)).doSomeWork(null, database);
        verify(mockModule3).doSomeWork(null, database);

        verifyNoMoreInteractions(mockModule1, mockModule2, mockModule3);
    }

    @Test
    public void allRegisteredInterestedTimerModulesShouldBeDelegatedToWithAdaptiveStrategy() throws InterruptedException {
        TimerDrivenModule mockModule1 = mockTimerModule(MOCK + "1");
        TimerDrivenModule mockModule2 = mockTimerModule(MOCK + "2");

        GraphAwareRuntime runtime = createRuntime(database,
                defaultConfiguration(database)
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
        when(mockModule2.getConfiguration()).thenReturn(NullTimerDrivenModuleConfiguration.getInstance());
        when(mockModule2.createInitialContext(database)).thenReturn(context1);
        when(mockModule2.doSomeWork(context1, database)).thenReturn(context2);

        GraphAwareRuntime runtime = createRuntime(database, defaultConfiguration(database).withTimingStrategy(TIMING_STRATEGY));
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
        verify(mockModule1, atLeastOnce()).getConfiguration();
        verify(mockModule2, atLeastOnce()).getConfiguration();
        verify(mockModule3, atLeastOnce()).getConfiguration();
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

        GraphAwareRuntime runtime = createRuntime(database, defaultConfiguration(database).withTimingStrategy(TIMING_STRATEGY));
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
        verify(mockModule1, atLeastOnce()).getConfiguration();
        verify(mockModule2, atLeastOnce()).getConfiguration();

        verifyNoMoreInteractions(mockModule1, mockModule2);
    }

    @Test
    public void lastContextShouldBePresentedInNextCallAndPersisted() throws InterruptedException {
        TimerDrivenModuleContext<Node> firstContext = new NodeBasedContext(1);
        TimerDrivenModuleContext<Node> secondContext = new NodeBasedContext(2);

        TimerDrivenModule mockModule = mockTimerModule();
        when(mockModule.doSomeWork(null, database)).thenReturn(firstContext);
        when(mockModule.doSomeWork(firstContext, database)).thenReturn(secondContext);

        GraphAwareRuntime runtime = createRuntime(database, defaultConfiguration(database).withTimingStrategy(TIMING_STRATEGY));
        runtime.registerModule(mockModule);

        runtime.start();

        verify(mockModule, atLeastOnce()).getId();
        verify(mockModule).createInitialContext(database);
        verifyNoMoreInteractions(mockModule);

        Thread.sleep(INITIAL_DELAY + 2 * DELAY - 100);

        verify(mockModule, atLeastOnce()).getId();
        verify(mockModule, atLeastOnce()).getConfiguration();
        verify(mockModule).doSomeWork(null, database);
        verify(mockModule).doSomeWork(firstContext, database);

        verifyNoMoreInteractions(mockModule);

        try (Transaction tx = database.beginTx()) {
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

        GraphAwareRuntime runtime = createRuntime(database, defaultConfiguration(database).withTimingStrategy(TIMING_STRATEGY));
        runtime.registerModule(mockModule);

        runtime.start();

        Thread.sleep(INITIAL_DELAY + DELAY - 100);

        verify(mockModule, atLeastOnce()).getId();
        verify(mockModule, atLeastOnce()).getConfiguration();
        verify(mockModule).doSomeWork(lastContext, database);
        verifyNoMoreInteractions(mockModule);
    }

    @Test
    public void shutdownShouldBeCalledBeforeShutdownOnTimerDrivenModules() {
        TimerDrivenModule mockModule = mockTimerModule();

        GraphAwareRuntime runtime = createRuntime(database, defaultConfiguration(database).withTimingStrategy(TIMING_STRATEGY));
        runtime.registerModule(mockModule);
        runtime.start();

        database.shutdown();

        verify(mockModule).shutdown();
    }

    @Test
    public void whenOneTimerModuleThrowsAnExceptionThenOtherModulesShouldStillBeDelegatedTo() throws InterruptedException {
        TimerDrivenModule mockModule1 = mockTimerModule(MOCK + "1");
        when(mockModule1.doSomeWork(any(TimerDrivenModuleContext.class), any(GraphDatabaseService.class))).thenThrow(new RuntimeException("deliberate testing exception"));

        TimerDrivenModule mockModule2 = mockTimerModule(MOCK + "2");

        GraphAwareRuntime runtime = createRuntime(database, defaultConfiguration(database).withTimingStrategy(TIMING_STRATEGY));
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
        verify(mockModule1, atLeastOnce()).getConfiguration();
        verify(mockModule2, atLeastOnce()).getConfiguration();
        verify(mockModule1).doSomeWork(null, database);
        verify(mockModule2).doSomeWork(null, database);
        verifyNoMoreInteractions(mockModule1, mockModule2);
    }

    @Test
    public void sameModuleCanActAsTxAndTimerDriven() throws InterruptedException {
        TxAndTimerDrivenModule mockModule = mock(TxAndTimerDrivenModule.class);
        when(mockModule.getId()).thenReturn(MOCK);
        when(mockModule.createInitialContext(database)).thenReturn(null);
        when(mockModule.getConfiguration()).thenReturn(NullTxAndTimerDrivenModuleConfiguration.getInstance());

        GraphAwareRuntime runtime = createRuntime(database, defaultConfiguration(database).withTimingStrategy(TIMING_STRATEGY));
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

        try (Transaction tx = database.beginTx()) {
            node1 = database.createNode(new Label[]{});
            node1.createRelationshipTo(database.createNode(new Label[]{}), RelationshipType.withName("TEST"));
            tx.success();
        }

        TxDrivenModule mockModule1 = mockTxModule(MOCK + "1");
        TxDrivenModule mockModule2 = mockTxModule(MOCK + "2");

        GraphAwareRuntime runtime = createRuntime(database, defaultConfiguration(database).withTimingStrategy(TIMING_STRATEGY));
        runtime.registerModule(mockModule1);
        runtime.registerModule(mockModule2);

        runtime.start();

        verify(mockModule1).initialize(database);
        verify(mockModule2).initialize(database);
        verify(mockModule1).start(database);
        verify(mockModule2).start(database);
        verify(mockModule1, atLeastOnce()).getConfiguration();
        verify(mockModule2, atLeastOnce()).getConfiguration();
        verify(mockModule1, atLeastOnce()).getId();
        verify(mockModule2, atLeastOnce()).getId();
        verifyNoMoreInteractions(mockModule1, mockModule2);

        try {
            try (Transaction tx = database.beginTx()) {
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

        GraphAwareRuntime runtime = createRuntime(database, defaultConfiguration(database).withTimingStrategy(TIMING_STRATEGY));
        runtime.registerModule(mockModule1);
        runtime.registerModule(mockModule2);
        runtime.registerModule(mockModule3);

        assertEquals(mockModule1, runtime.getModule(MOCK + "1", TxAndTimerDrivenModule.class));
        assertEquals(mockModule1, runtime.getModule(MOCK + "1", TxDrivenModule.class));
        assertEquals(mockModule1, runtime.getModule(MOCK + "1", TimerDrivenModule.class));
        assertEquals(mockModule2, runtime.getModule(MOCK + "2", TxDrivenModule.class));
        assertEquals(mockModule3, runtime.getModule(MOCK + "3", TimerDrivenModule.class));
    }

    @Test
    public void shouldObtainModulesOfCorrectTypesWhenIdNotSpecified() {
        M1 mockM1 = mockTxModule("M1", M1.class);
        M2 mockM2a = mockTxModule("M2a", M2.class);
        M2 mockM2b = mockTxModule("M2b", M2.class);
        M3 mockM3 = mockTimerModule("M3", M3.class);
        M4 mockM4a = mockTimerModule("M4a", M4.class);
        M4 mockM4b = mockTimerModule("M4b", M4.class);
        M5 mockM5 = mockTimerModule("M5", M5.class);
        M6 mockM6a = mockTimerModule("M6a", M6.class);
        M6 mockM6b = mockTimerModule("M6b", M6.class);

        GraphAwareRuntime runtime = createRuntime(database, defaultConfiguration(database).withTimingStrategy(TIMING_STRATEGY));
        runtime.registerModule(mockM1);
        runtime.registerModule(mockM2a);
        runtime.registerModule(mockM2b);
        runtime.registerModule(mockM3);
        runtime.registerModule(mockM4a);
        runtime.registerModule(mockM4b);
        runtime.registerModule(mockM5);
        runtime.registerModule(mockM6a);
        runtime.registerModule(mockM6b);

        assertEquals(mockM1, runtime.getModule(M1.class));
        assertEquals(mockM3, runtime.getModule(M3.class));
        assertEquals(mockM5, runtime.getModule(M5.class));

        try {
            runtime.getModule(M2.class);
        } catch (IllegalStateException e) {
            //ok
        }
        try {
            runtime.getModule(M4.class);
        } catch (IllegalStateException e) {
            //ok
        }
        try {
            runtime.getModule(M6.class);
        } catch (IllegalStateException e) {
            //ok
        }
        try {
            runtime.getModule(M7.class);
        } catch (NotFoundException e) {
            //ok
        }
        try {
            runtime.getModule(M8.class);
        } catch (NotFoundException e) {
            //ok
        }
        try {
            runtime.getModule(M9.class);
        } catch (NotFoundException e) {
            //ok
        }
    }

    @Test(expected = NotFoundException.class)
    public void shouldThrowExceptionWhenAskedForNonExistingModule() {
        GraphAwareRuntime runtime = createRuntime(database, defaultConfiguration(database).withTimingStrategy(TIMING_STRATEGY));
        runtime.getModule("non-existing", TxAndTimerDrivenModule.class);
    }

    @Test(expected = NotFoundException.class)
    public void shouldThrowExceptionWhenAskedForWrongModuleType() {
        TxAndTimerDrivenModule mockModule1 = mock(TxAndTimerDrivenModule.class);
        when(mockModule1.getId()).thenReturn(MOCK + "1");
        TxDrivenModule mockModule2 = mockTxModule(MOCK + "2");
        TimerDrivenModule mockModule3 = mockTimerModule(MOCK + "3");

        GraphAwareRuntime runtime = createRuntime(database, defaultConfiguration(database).withTimingStrategy(TIMING_STRATEGY));
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

        GraphAwareRuntime runtime = createRuntime(database, defaultConfiguration(database).withTimingStrategy(TIMING_STRATEGY));
        runtime.registerModule(mockModule1);
        runtime.registerModule(mockModule2);
        runtime.registerModule(mockModule3);

        runtime.getModule(MOCK + "2", TimerDrivenModule.class);
    }

    @Test
    public void moduleRegisteredForTheFirstTimeShouldBeInitialized() {
        final TxDrivenModule mockModule = mockTxModule();

        GraphAwareRuntime runtime = createRuntime(database, defaultConfiguration(database).withTimingStrategy(TIMING_STRATEGY));
        runtime.registerModule(mockModule);

        runtime.start();

        verify(mockModule).initialize(database);
        verify(mockModule).start(database);
        verify(mockModule, atLeastOnce()).getConfiguration();
        verify(mockModule, atLeastOnce()).getId();
        verifyNoMoreInteractions(mockModule);

        try (Transaction tx = database.beginTx()) {
            TxDrivenModuleMetadata moduleMetadata = txRepo.getModuleMetadata(mockModule);
            assertEquals(NullTxDrivenModuleConfiguration.getInstance(), moduleMetadata.getConfig());
            assertFalse(moduleMetadata.needsInitialization());
            assertEquals(-1, moduleMetadata.problemTimestamp());
        }
    }

    @Test
    public void moduleRegisteredForTheFirstTimeShouldBeInitializedWhenAllowed() {
        FluentTxDrivenModuleConfiguration configuration = FluentTxDrivenModuleConfiguration.defaultConfiguration().withInitializeUntil(System.currentTimeMillis() + 2000);

        final TxDrivenModule mockModule = mockTxModule(configuration);

        GraphAwareRuntime runtime = createRuntime(database, defaultConfiguration(database).withTimingStrategy(TIMING_STRATEGY));
        runtime.registerModule(mockModule);

        runtime.start();

        verify(mockModule).initialize(database);
        verify(mockModule).start(database);
        verify(mockModule, atLeastOnce()).getConfiguration();
        verify(mockModule, atLeastOnce()).getId();
        verifyNoMoreInteractions(mockModule);

        try (Transaction tx = database.beginTx()) {
            TxDrivenModuleMetadata moduleMetadata = txRepo.getModuleMetadata(mockModule);
            assertEquals(configuration, moduleMetadata.getConfig());
            assertFalse(moduleMetadata.needsInitialization());
            assertEquals(-1, moduleMetadata.problemTimestamp());
        }
    }

    @Test
    public void moduleRegisteredForTheFirstTimeShouldNotBeInitializedWhenNotAllowed() {
        FluentTxDrivenModuleConfiguration configuration = FluentTxDrivenModuleConfiguration.defaultConfiguration().withInitializeUntil(System.currentTimeMillis() - 1);

        final TxDrivenModule mockModule = mockTxModule(configuration);

        GraphAwareRuntime runtime = createRuntime(database, defaultConfiguration(database).withTimingStrategy(TIMING_STRATEGY));
        runtime.registerModule(mockModule);

        runtime.start();

        verify(mockModule).start(database);
        verify(mockModule, atLeastOnce()).getConfiguration();
        verify(mockModule, atLeastOnce()).getId();
        verifyNoMoreInteractions(mockModule);

        try (Transaction tx = database.beginTx()) {
            TxDrivenModuleMetadata moduleMetadata = txRepo.getModuleMetadata(mockModule);
            assertEquals(configuration, moduleMetadata.getConfig());
            assertFalse(moduleMetadata.needsInitialization());
            assertEquals(-1, moduleMetadata.problemTimestamp());
        }
    }

    @Test
    public void moduleAlreadyRegisteredShouldNotBeInitialized() {
        final TxDrivenModule mockModule = mockTxModule();

        try (Transaction tx = database.beginTx()) {
            txRepo.persistModuleMetadata(mockModule, new DefaultTxDrivenModuleMetadata(NullTxDrivenModuleConfiguration.getInstance()));
            tx.success();
        }

        GraphAwareRuntime runtime = createRuntime(database, defaultConfiguration(database).withTimingStrategy(TIMING_STRATEGY));
        runtime.registerModule(mockModule);

        runtime.start();

        verify(mockModule).start(database);
        verify(mockModule, atLeastOnce()).getId();
        verify(mockModule, atLeastOnce()).getConfiguration();
        verifyNoMoreInteractions(mockModule);

        try (Transaction tx = database.beginTx()) {
            TxDrivenModuleMetadata moduleMetadata = txRepo.getModuleMetadata(mockModule);
            assertEquals(NullTxDrivenModuleConfiguration.getInstance(), moduleMetadata.getConfig());
            assertFalse(moduleMetadata.needsInitialization());
            assertEquals(-1, moduleMetadata.problemTimestamp());
        }
    }

    @Test
    public void changedModuleShouldBeReInitialized() {
        final TxDrivenModule mockModule = mockTxModule();

        DefaultTxDrivenModuleMetadata oldMetadata = new DefaultTxDrivenModuleMetadata(FluentTxDrivenModuleConfiguration.defaultConfiguration().with(InclusionPolicies.none()));

        try (Transaction tx = database.beginTx()) {
            txRepo.persistModuleMetadata(mockModule, oldMetadata);
            tx.success();
        }

        GraphAwareRuntime runtime = createRuntime(database, defaultConfiguration(database).withTimingStrategy(TIMING_STRATEGY));
        runtime.registerModule(mockModule);

        runtime.start();

        verify(mockModule).reinitialize(database, oldMetadata);
        verify(mockModule).start(database);
        verify(mockModule, atLeastOnce()).getConfiguration();
        verify(mockModule, atLeastOnce()).getId();
        verifyNoMoreInteractions(mockModule);

        try (Transaction tx = database.beginTx()) {
            TxDrivenModuleMetadata moduleMetadata = txRepo.getModuleMetadata(mockModule);
            assertEquals(NullTxDrivenModuleConfiguration.getInstance(), moduleMetadata.getConfig());
            assertFalse(moduleMetadata.needsInitialization());
            assertEquals(-1, moduleMetadata.problemTimestamp());
        }
    }

    @Test
    public void changedModuleShouldNotBeReInitializedWhenNotAllowed() {
        FluentTxDrivenModuleConfiguration configuration = FluentTxDrivenModuleConfiguration.defaultConfiguration().withInitializeUntil(System.currentTimeMillis() - 1);

        final TxDrivenModule mockModule = mockTxModule(configuration);

        DefaultTxDrivenModuleMetadata oldMetadata = new DefaultTxDrivenModuleMetadata(FluentTxDrivenModuleConfiguration.defaultConfiguration().with(InclusionPolicies.none()));

        try (Transaction tx = database.beginTx()) {
            txRepo.persistModuleMetadata(mockModule, oldMetadata);
            tx.success();
        }

        GraphAwareRuntime runtime = createRuntime(database, defaultConfiguration(database).withTimingStrategy(TIMING_STRATEGY));
        runtime.registerModule(mockModule);

        runtime.start();

        verify(mockModule).start(database);
        verify(mockModule, atLeastOnce()).getConfiguration();
        verify(mockModule, atLeastOnce()).getId();
        verifyNoMoreInteractions(mockModule);

        try (Transaction tx = database.beginTx()) {
            TxDrivenModuleMetadata moduleMetadata = txRepo.getModuleMetadata(mockModule);
            assertEquals(configuration, moduleMetadata.getConfig());
            assertFalse(moduleMetadata.needsInitialization());
            assertEquals(-1, moduleMetadata.problemTimestamp());
        }
    }

    @Test
    public void forcedModuleShouldBeReInitialized() {
        final TxDrivenModule mockModule = mockTxModule();

        DefaultTxDrivenModuleMetadata metadata = new DefaultTxDrivenModuleMetadata(NullTxDrivenModuleConfiguration.getInstance()).markedNeedingInitialization();

        try (Transaction tx = database.beginTx()) {
            txRepo.persistModuleMetadata(mockModule, metadata);
            tx.success();
        }

        GraphAwareRuntime runtime = createRuntime(database, defaultConfiguration(database).withTimingStrategy(TIMING_STRATEGY));
        runtime.registerModule(mockModule);

        runtime.start();

        verify(mockModule).reinitialize(database, metadata);
        verify(mockModule).start(database);
        verify(mockModule, atLeastOnce()).getConfiguration();
        verify(mockModule, atLeastOnce()).getId();
        verifyNoMoreInteractions(mockModule);

        try (Transaction tx = database.beginTx()) {
            TxDrivenModuleMetadata moduleMetadata = txRepo.getModuleMetadata(mockModule);
            assertEquals(NullTxDrivenModuleConfiguration.getInstance(), moduleMetadata.getConfig());
            assertFalse(moduleMetadata.needsInitialization());
            assertEquals(-1, moduleMetadata.problemTimestamp());
        }
    }

    @Test
    public void forcedModuleShouldNotBeReInitializedWhenNotAllowed() {
        FluentTxDrivenModuleConfiguration configuration = FluentTxDrivenModuleConfiguration.defaultConfiguration().withInitializeUntil(System.currentTimeMillis() - 1);

        final TxDrivenModule mockModule = mockTxModule(configuration);

        DefaultTxDrivenModuleMetadata metadata = new DefaultTxDrivenModuleMetadata(NullTxDrivenModuleConfiguration.getInstance()).markedNeedingInitialization();

        try (Transaction tx = database.beginTx()) {
            txRepo.persistModuleMetadata(mockModule, metadata);
            tx.success();
        }

        GraphAwareRuntime runtime = createRuntime(database, defaultConfiguration(database).withTimingStrategy(TIMING_STRATEGY));
        runtime.registerModule(mockModule);

        runtime.start();

        verify(mockModule).start(database);
        verify(mockModule, atLeastOnce()).getConfiguration();
        verify(mockModule, atLeastOnce()).getId();
        verifyNoMoreInteractions(mockModule);

        try (Transaction tx = database.beginTx()) {
            TxDrivenModuleMetadata moduleMetadata = txRepo.getModuleMetadata(mockModule);
            assertEquals(configuration, moduleMetadata.getConfig());
            assertFalse(moduleMetadata.needsInitialization());
            assertEquals(-1, moduleMetadata.problemTimestamp());
        }
    }

    @Test(expected = IllegalStateException.class)
    public void shouldNotBeAbleToRegisterTheSameModuleTwice() {
        final TxDrivenModule mockModule = mockTxModule();

        GraphAwareRuntime runtime = createRuntime(database, defaultConfiguration(database).withTimingStrategy(TIMING_STRATEGY));
        runtime.registerModule(mockModule);
        runtime.registerModule(mockModule);
    }

    @Test(expected = IllegalStateException.class)
    public void shouldNotBeAbleToRegisterModuleWithTheSameIdTwice() {
        final TxDrivenModule mockModule1 = mockTxModule();
        final TxDrivenModule mockModule2 = mockTxModule();
        when(mockModule1.getId()).thenReturn("ID");
        when(mockModule2.getId()).thenReturn("ID");

        GraphAwareRuntime runtime = createRuntime(database, defaultConfiguration(database).withTimingStrategy(TIMING_STRATEGY));
        runtime.registerModule(mockModule1);
        runtime.registerModule(mockModule2);
    }

    @Test
    public void unusedModulesShouldBeRemoved() {
        final TxDrivenModule mockModule = mockTxModule();
        final TxDrivenModule unusedModule = mockTxModule("UNUSED");

        try (Transaction tx = database.beginTx()) {
            txRepo.persistModuleMetadata(mockModule, new DefaultTxDrivenModuleMetadata(NullTxDrivenModuleConfiguration.getInstance()));
            txRepo.persistModuleMetadata(unusedModule, new DefaultTxDrivenModuleMetadata(NullTxDrivenModuleConfiguration.getInstance()));
            tx.success();
        }

        GraphAwareRuntime runtime = createRuntime(database, defaultConfiguration(database).withTimingStrategy(TIMING_STRATEGY));
        runtime.registerModule(mockModule);

        runtime.start();

        verify(mockModule).start(database);
        verify(mockModule, atLeastOnce()).getId();
        verify(mockModule, atLeastOnce()).getConfiguration();
        verifyNoMoreInteractions(mockModule);

        try (Transaction tx = database.beginTx()) {
            assertEquals(1, txRepo.getAllModuleIds().size());
        }
    }

    @Test
    public void usedCorruptModulesShouldBeReInitialized() {
        final TxDrivenModule mockModule = mockTxModule();

        try (Transaction tx = database.beginTx()) {
            keyValueStore.set(GA_PREFIX + TX_MODULES_PROPERTY_PREFIX + "_" + MOCK, "CORRUPT");
            tx.success();
        }

        GraphAwareRuntime runtime = createRuntime(database, defaultConfiguration(database).withTimingStrategy(TIMING_STRATEGY));
        runtime.registerModule(mockModule);

        runtime.start();

        verify(mockModule).start(database);
        verify(mockModule, atLeastOnce()).getId();
        verify(mockModule, atLeastOnce()).getConfiguration();
        verify(mockModule).reinitialize(database, null);
        verifyNoMoreInteractions(mockModule);

        try (Transaction tx = database.beginTx()) {
            TxDrivenModuleMetadata moduleMetadata = txRepo.getModuleMetadata(mockModule);
            assertEquals(NullTxDrivenModuleConfiguration.getInstance(), moduleMetadata.getConfig());
            assertFalse(moduleMetadata.needsInitialization());
            assertEquals(-1, moduleMetadata.problemTimestamp());
        }
    }

    @Test
    public void usedCorruptModulesShouldBeInitialized2() {
        final TxDrivenModule mockModule = mockTxModule();

        try (Transaction tx = database.beginTx()) {
            keyValueStore.set(GA_PREFIX + TX_MODULES_PROPERTY_PREFIX + "_" + MOCK, new byte[]{2, 3, 4});
            tx.success();
        }

        GraphAwareRuntime runtime = createRuntime(database, defaultConfiguration(database).withTimingStrategy(TIMING_STRATEGY));
        runtime.registerModule(mockModule);

        runtime.start();

        verify(mockModule).start(database);
        verify(mockModule, atLeastOnce()).getId();
        verify(mockModule, atLeastOnce()).getConfiguration();
        verify(mockModule).reinitialize(database, null);
        verifyNoMoreInteractions(mockModule);

        try (Transaction tx = database.beginTx()) {
            TxDrivenModuleMetadata moduleMetadata = txRepo.getModuleMetadata(mockModule);
            assertEquals(NullTxDrivenModuleConfiguration.getInstance(), moduleMetadata.getConfig());
            assertFalse(moduleMetadata.needsInitialization());
            assertEquals(-1, moduleMetadata.problemTimestamp());
        }
    }

    @Test
    public void unusedCorruptModulesShouldBeRemoved() {
        final TxDrivenModule mockModule = mockTxModule();

        try (Transaction tx = database.beginTx()) {
            txRepo.persistModuleMetadata(mockModule, new DefaultTxDrivenModuleMetadata(NullTxDrivenModuleConfiguration.getInstance()));
            keyValueStore.set(GA_PREFIX + TX_MODULES_PROPERTY_PREFIX + "_UNUSED", "CORRUPT");
            keyValueStore.set(GA_PREFIX + TX_MODULES_PROPERTY_PREFIX + "_UNUSED2", new byte[]{1, 2, 3});
            tx.success();
        }

        GraphAwareRuntime runtime = createRuntime(database, defaultConfiguration(database).withTimingStrategy(TIMING_STRATEGY));
        runtime.registerModule(mockModule);

        runtime.start();

        verify(mockModule).start(database);
        verify(mockModule, atLeastOnce()).getId();
        verify(mockModule, atLeastOnce()).getConfiguration();
        verifyNoMoreInteractions(mockModule);

        try (Transaction tx = database.beginTx()) {
            assertEquals(1, txRepo.getAllModuleIds().size());
        }
    }

    @Test
    public void allRegisteredInterestedModulesShouldBeDelegatedTo() {
        TxDrivenModule mockModule1 = mockTxModule(MOCK + "1");
        TxDrivenModule mockModule2 = mockTxModule(MOCK + "2");
        TxDrivenModule mockModule3 = mockTxModule(MOCK + "3", FluentTxDrivenModuleConfiguration.defaultConfiguration().with(InclusionPolicies.none()));

        GraphAwareRuntime runtime = createRuntime(database, defaultConfiguration(database).withTimingStrategy(TIMING_STRATEGY));
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
        verify(mockModule1).start(database);
        verify(mockModule2).start(database);
        verify(mockModule3).start(database);
        verifyNoMoreInteractions(mockModule1, mockModule2, mockModule3);

        try (Transaction tx = database.beginTx()) {
            database.createNode(new Label[]{});
            tx.success();
        }

        verify(mockModule1).beforeCommit(any(ImprovedTransactionData.class));
        verify(mockModule1).afterCommit("TEST_" + MOCK + "1");
        verify(mockModule2).beforeCommit(any(ImprovedTransactionData.class));
        verify(mockModule2).afterCommit("TEST_" + MOCK + "2");
        verify(mockModule1, atLeastOnce()).getId();
        verify(mockModule1, atLeastOnce()).getConfiguration();
        verify(mockModule2, atLeastOnce()).getId();
        verify(mockModule2, atLeastOnce()).getConfiguration();
        verify(mockModule3, atLeastOnce()).getId();
        verify(mockModule3, atLeastOnce()).getConfiguration();

        //no interaction with module3, it is not interested!
        verifyNoMoreInteractions(mockModule1, mockModule2, mockModule3);
    }

    @Test
    public void moduleThrowingInitExceptionShouldBeMarkedForReinitialization() {
        final TxDrivenModule mockModule = mockTxModule();
        when(mockModule.getConfiguration()).thenReturn(NullTxDrivenModuleConfiguration.getInstance());
        Mockito.doThrow(new NeedsInitializationException()).when(mockModule).beforeCommit(any(ImprovedTransactionData.class));

        GraphAwareRuntime runtime = createRuntime(database, defaultConfiguration(database).withTimingStrategy(TIMING_STRATEGY));
        runtime.registerModule(mockModule);

        runtime.start();

        try (Transaction tx = database.beginTx()) {
            TxDrivenModuleMetadata moduleMetadata = txRepo.getModuleMetadata(mockModule);
            assertEquals(NullTxDrivenModuleConfiguration.getInstance(), moduleMetadata.getConfig());
            assertFalse(moduleMetadata.needsInitialization());
            assertEquals(-1, moduleMetadata.problemTimestamp());
        }

        try (Transaction tx = database.beginTx()) {
            database.createNode(new Label[]{});
            tx.success();
        }

        try (Transaction tx = database.beginTx()) {
            TxDrivenModuleMetadata moduleMetadata = txRepo.getModuleMetadata(mockModule);
            assertEquals(NullTxDrivenModuleConfiguration.getInstance(), moduleMetadata.getConfig());
            assertTrue(moduleMetadata.needsInitialization());
            assertTrue(moduleMetadata.problemTimestamp() > System.currentTimeMillis() - 1000);
        }
    }

    @Test
    public void moduleThrowingInitExceptionShouldBeMarkedForReinitializationOnlyTheFirstTime() throws InterruptedException {
        final TxDrivenModule mockModule = mockTxModule();
        when(mockModule.getConfiguration()).thenReturn(NullTxDrivenModuleConfiguration.getInstance());
        doThrow(new NeedsInitializationException()).when(mockModule).beforeCommit(any(ImprovedTransactionData.class));

        GraphAwareRuntime runtime = createRuntime(database, defaultConfiguration(database).withTimingStrategy(TIMING_STRATEGY));
        runtime.registerModule(mockModule);

        runtime.start();

        try (Transaction tx = database.beginTx()) {
            database.createNode(new Label[]{});
            tx.success();
        }

        long firstFailureTimestamp;
        try (Transaction tx = database.beginTx()) {
            TxDrivenModuleMetadata moduleMetadata = txRepo.getModuleMetadata(mockModule);
            firstFailureTimestamp = moduleMetadata.problemTimestamp();
        }

        Thread.sleep(1);

        try (Transaction tx = database.beginTx()) {
            database.createNode(new Label[]{});
            tx.success();
        }

        long secondFailureTimestamp;
        try (Transaction tx = database.beginTx()) {
            TxDrivenModuleMetadata moduleMetadata = txRepo.getModuleMetadata(mockModule);
            secondFailureTimestamp = moduleMetadata.problemTimestamp();
        }

        assertEquals(firstFailureTimestamp, secondFailureTimestamp);
    }

    @Test(expected = IllegalStateException.class)
    public void modulesCannotBeRegisteredAfterStart() {
        final TxDrivenModule mockModule = mockTxModule();

        GraphAwareRuntime runtime = createRuntime(database, defaultConfiguration(database).withTimingStrategy(TIMING_STRATEGY));
        runtime.start();
        runtime.registerModule(mockModule);
    }

    @Test
    public void multipleCallsToStartFrameworkHaveNoEffect() {
        GraphAwareRuntime runtime = createRuntime(database, defaultConfiguration(database).withTimingStrategy(TIMING_STRATEGY));
        runtime.start();
        runtime.start();
        runtime.start();
        runtime.start();
    }

    @Test
    public void shutdownShouldBeCalledBeforeShutdown() {
        TxDrivenModule mockModule = mockTxModule();

        GraphAwareRuntime runtime = createRuntime(database, defaultConfiguration(database).withTimingStrategy(TIMING_STRATEGY));
        runtime.registerModule(mockModule);
        runtime.start();

        database.shutdown();

        verify(mockModule).shutdown();
    }

    @Test
    public void whenOneModuleThrowsAnExceptionThenOtherModulesShouldNotBeDelegatedTo() {
        TxDrivenModule mockModule1 = mockTxModule(MOCK + "1");
        doThrow(new RuntimeException()).when(mockModule1).beforeCommit(any(ImprovedTransactionData.class));

        TxDrivenModule mockModule2 = mockTxModule(MOCK + "2");

        GraphAwareRuntime runtime = createRuntime(database, defaultConfiguration(database).withTimingStrategy(TIMING_STRATEGY));
        runtime.registerModule(mockModule1);
        runtime.registerModule(mockModule2);

        runtime.start();

        verify(mockModule1).initialize(database);
        verify(mockModule2).initialize(database);
        verify(mockModule1).start(database);
        verify(mockModule2).start(database);

        verify(mockModule1, atLeastOnce()).getConfiguration();
        verify(mockModule2, atLeastOnce()).getConfiguration();
        verify(mockModule1, atLeastOnce()).getId();
        verify(mockModule2, atLeastOnce()).getId();
        verifyNoMoreInteractions(mockModule1, mockModule2);

        try (Transaction tx = database.beginTx()) {
            database.createNode(new Label[]{});
            tx.success();
        } catch (RuntimeException e) {
            //ok
        }

        verify(mockModule1).beforeCommit(any(ImprovedTransactionData.class));
        verify(mockModule1).afterRollback(null);
        verify(mockModule1, atLeastOnce()).getId();
        verify(mockModule2, atLeastOnce()).getId();
        verify(mockModule1, atLeastOnce()).getConfiguration();
        verify(mockModule2, atLeastOnce()).getConfiguration();
        verifyNoMoreInteractions(mockModule1, mockModule2);
    }

    @Test
    public void whenOneModuleForcesRollbackThenModulesBeforeItShouldBeAware() {
        TxDrivenModule mockModule1 = mockTxModule(MOCK + "1");
        TxDrivenModule mockModule2 = mockTxModule(MOCK + "2");
        TxDrivenModule mockModule3 = mockTxModule(MOCK + "3");

        doThrow(new DeliberateTransactionRollbackException()).when(mockModule2).beforeCommit(any(ImprovedTransactionData.class));

        GraphAwareRuntime runtime = createRuntime(database, defaultConfiguration(database).withTimingStrategy(TIMING_STRATEGY));
        runtime.registerModule(mockModule1);
        runtime.registerModule(mockModule2);
        runtime.registerModule(mockModule3);

        runtime.start();

        verify(mockModule1).initialize(database);
        verify(mockModule2).initialize(database);
        verify(mockModule3).initialize(database);

        verify(mockModule1).start(database);
        verify(mockModule2).start(database);
        verify(mockModule3).start(database);

        verify(mockModule1, atLeastOnce()).getConfiguration();
        verify(mockModule2, atLeastOnce()).getConfiguration();
        verify(mockModule3, atLeastOnce()).getConfiguration();
        verify(mockModule1, atLeastOnce()).getId();
        verify(mockModule2, atLeastOnce()).getId();
        verify(mockModule3, atLeastOnce()).getId();
        verifyNoMoreInteractions(mockModule1, mockModule2, mockModule3);

        try {
            try (Transaction tx = database.beginTx()) {
                database.createNode(new Label[]{});
                tx.success();
            }
            fail();
        } catch (RuntimeException e) {
            //expected
        }

        verify(mockModule1).beforeCommit(any(ImprovedTransactionData.class));
        verify(mockModule2).beforeCommit(any(ImprovedTransactionData.class));
        verify(mockModule1).afterRollback("TEST_" + MOCK + "1");
        verify(mockModule2).afterRollback(null); //didn't produce object, threw exception
        verify(mockModule1, atLeastOnce()).getId();
        verify(mockModule2, atLeastOnce()).getId();
        verify(mockModule3, atLeastOnce()).getId();
        verify(mockModule1, atLeastOnce()).getConfiguration();
        verify(mockModule2, atLeastOnce()).getConfiguration();
        verifyNoMoreInteractions(mockModule1, mockModule2, mockModule3);
    }

    @Test(expected = RuntimeException.class)
    public void whenRuntimeIsNotStartedExceptionShouldBeThrown() {
        createRuntime(database, defaultConfiguration(database).withTimingStrategy(TIMING_STRATEGY));

        try (Transaction tx = database.beginTx()) {
            database.createNode(new Label[]{});
            tx.success();
        }
    }

    @Test
    public void shouldStartAndStopDatabaseWriter() {
        final AtomicBoolean started = new AtomicBoolean(false);

        GraphAwareRuntime runtime = createRuntime(database, defaultConfiguration(database).withWritingConfig(new WritingConfig() {
            @Override
            public Neo4jWriter produceWriter(GraphDatabaseService database) {
                return new BaseNeo4jWriter(database) {
                    @Override
                    public void start() {
                        started.set(true);
                    }

                    @Override
                    public void stop() {
                        started.set(false);
                    }

                    @Override
                    public <T> T write(Callable<T> task, String id, int waitMillis) {
                        return null;
                    }
                };
            }
        }));


        assertFalse(started.get());

        runtime.start();

        assertTrue(started.get());

        ((KernelEventHandler) runtime).beforeShutdown();

        assertFalse(started.get());
    }

    private TxDrivenModule mockTxModule() {
        return mockTxModule(MOCK);
    }

    private TxDrivenModule mockTxModule(String id) {
        return mockTxModule(id, NullTxDrivenModuleConfiguration.getInstance());
    }

    private TxDrivenModule mockTxModule(TxDrivenModuleConfiguration configuration) {
        return mockTxModule(MOCK, configuration);
    }

    private TxDrivenModule mockTxModule(String id, TxDrivenModuleConfiguration configuration) {
        TxDrivenModule mockModule = mock(TxDrivenModule.class);
        when(mockModule.getId()).thenReturn(id);
        when(mockModule.getConfiguration()).thenReturn(configuration);
        when(mockModule.beforeCommit(any(ImprovedTransactionData.class))).thenReturn("TEST_" + id);
        return mockModule;
    }

    private <T extends TxDrivenModule> T mockTxModule(String id, Class<T> cls) {
        T mockModule = mock(cls);
        when(mockModule.getId()).thenReturn(id);
        when(mockModule.getConfiguration()).thenReturn( NullTxDrivenModuleConfiguration.getInstance());
        when(mockModule.beforeCommit(any(ImprovedTransactionData.class))).thenReturn("TEST_" + id);
        return mockModule;
    }

    private TimerDrivenModule mockTimerModule() {
        return mockTimerModule(MOCK);
    }

    private TimerDrivenModule mockTimerModule(String id) {
        return mockTimerModule(id, TimerDrivenModule.class);
    }

    private <T extends TimerDrivenModule> T mockTimerModule(String id, Class<T> cls) {
        T mockModule = mock(cls);
        when(mockModule.getId()).thenReturn(id);
        when(mockModule.createInitialContext(database)).thenReturn(null);
        when(mockModule.getConfiguration()).thenReturn(NullTxAndTimerDrivenModuleConfiguration.getInstance());

        return mockModule;
    }

    interface M1 extends TxDrivenModule {

    }

    interface M2 extends TxDrivenModule {

    }

    interface M3 extends TimerDrivenModule {

    }

    interface M4 extends TimerDrivenModule {

    }

    interface M5 extends TxAndTimerDrivenModule {
        /**
         * {@inheritDoc}
         */
        @Override
        TxAndTimerDrivenModuleConfiguration getConfiguration();
    }

    interface M6 extends TxAndTimerDrivenModule {
        /**
         * {@inheritDoc}
         */
        @Override
        TxAndTimerDrivenModuleConfiguration getConfiguration();
    }

    interface M7 extends TxDrivenModule {

    }

    interface M8 extends TimerDrivenModule {

    }

    interface M9 extends TxAndTimerDrivenModule {
        /**
         * {@inheritDoc}
         */
        @Override
        TxAndTimerDrivenModuleConfiguration getConfiguration();
    }


}
