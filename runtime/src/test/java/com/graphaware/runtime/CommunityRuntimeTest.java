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

package com.graphaware.runtime;

import com.graphaware.common.policy.inclusion.InclusionPolicies;
import com.graphaware.runtime.config.FluentModuleConfiguration;
import com.graphaware.runtime.config.NullRuntimeModuleConfiguration;
import com.graphaware.runtime.config.RuntimeModuleConfiguration;
import com.graphaware.runtime.module.DeliberateTransactionRollbackException;
import com.graphaware.runtime.module.RuntimeModule;
import com.graphaware.runtime.write.WritingConfig;
import com.graphaware.tx.event.improved.api.ImprovedTransactionData;
import com.graphaware.writer.neo4j.BaseNeo4jWriter;
import com.graphaware.writer.neo4j.Neo4jWriter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.event.KernelEventHandler;
import org.neo4j.helpers.collection.Iterables;
import org.neo4j.test.TestGraphDatabaseFactory;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.graphaware.common.util.DatabaseUtils.registerShutdownHook;
import static com.graphaware.runtime.GraphAwareRuntimeFactory.createRuntime;
import static com.graphaware.runtime.config.FluentRuntimeConfiguration.defaultConfiguration;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit test for {@link CommunityRuntime}.
 */
public class CommunityRuntimeTest {

    private static final String MOCK = "MOCK";

    private GraphDatabaseService database;

    @Before
    public void setUp() {
        database = new TestGraphDatabaseFactory()
                .newImpermanentDatabaseBuilder()
                .newGraphDatabase();

        registerShutdownHook(database);
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
        RuntimeModule mockModule = mockTxModule(MOCK);
        doThrow(new DeliberateTransactionRollbackException("Deliberate testing exception")).when(mockModule).beforeCommit(any(ImprovedTransactionData.class));

        GraphAwareRuntime runtime = createRuntime(database, defaultConfiguration(database));
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
        RuntimeModule mockModule = mockTxModule(MOCK);
        doThrow(new RuntimeException("Deliberate testing exception")).when(mockModule).beforeCommit(any(ImprovedTransactionData.class));

        GraphAwareRuntime runtime = createRuntime(database, defaultConfiguration(database));
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
        GraphAwareRuntime runtime = createRuntime(database, defaultConfiguration(database));
        runtime.registerModule(mockTxModule());
        runtime.registerModule(mockTxModule());
    }

    @Test
    public void modulesShouldBeAwareOfRollbackAfterConstraintViolation() {
        Node node1;

        try (Transaction tx = database.beginTx()) {
            node1 = database.createNode(new Label[]{});
            node1.createRelationshipTo(database.createNode(new Label[]{}), RelationshipType.withName("TEST"));
            tx.success();
        }

        RuntimeModule mockModule1 = mockTxModule(MOCK + "1");
        RuntimeModule mockModule2 = mockTxModule(MOCK + "2");

        GraphAwareRuntime runtime = createRuntime(database, defaultConfiguration(database));
        runtime.registerModule(mockModule1);
        runtime.registerModule(mockModule2);

        runtime.start();

        verify(mockModule1).start(database);
        verify(mockModule2).start(database);
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
    public void shouldObtainModulesOfCorrectTypesWhenIdNotSpecified() {
        M1 mockM1 = mockTxModule("M1", M1.class);
        M2 mockM2a = mockTxModule("M2a", M2.class);
        M2 mockM2b = mockTxModule("M2b", M2.class);

        GraphAwareRuntime runtime = createRuntime(database, defaultConfiguration(database));
        runtime.registerModule(mockM1);
        runtime.registerModule(mockM2a);
        runtime.registerModule(mockM2b);

        assertEquals(mockM1, runtime.getModule(M1.class));

        try {
            runtime.getModule(M2.class);
        } catch (IllegalStateException e) {
            //ok
        }
        try {
            runtime.getModule(M7.class);
        } catch (NotFoundException e) {
            //ok
        }
    }

    @Test(expected = NotFoundException.class)
    public void shouldThrowExceptionWhenAskedForNonExistingModule() {
        GraphAwareRuntime runtime = createRuntime(database, defaultConfiguration(database));
        runtime.getModule("non-existing", RuntimeModule.class);
    }

    @Test(expected = IllegalStateException.class)
    public void shouldNotBeAbleToRegisterTheSameModuleTwice() {
        final RuntimeModule mockModule = mockTxModule();

        GraphAwareRuntime runtime = createRuntime(database, defaultConfiguration(database));
        runtime.registerModule(mockModule);
        runtime.registerModule(mockModule);
    }

    @Test(expected = IllegalStateException.class)
    public void shouldNotBeAbleToRegisterModuleWithTheSameIdTwice() {
        final RuntimeModule mockModule1 = mockTxModule();
        final RuntimeModule mockModule2 = mockTxModule();
        when(mockModule1.getId()).thenReturn("ID");
        when(mockModule2.getId()).thenReturn("ID");

        GraphAwareRuntime runtime = createRuntime(database, defaultConfiguration(database));
        runtime.registerModule(mockModule1);
        runtime.registerModule(mockModule2);
    }

    @Test
    public void allRegisteredInterestedModulesShouldBeDelegatedTo() {
        RuntimeModule mockModule1 = mockTxModule(MOCK + "1");
        RuntimeModule mockModule2 = mockTxModule(MOCK + "2");
        RuntimeModule mockModule3 = mockTxModule(MOCK + "3", FluentModuleConfiguration.defaultConfiguration().with(InclusionPolicies.none()));

        GraphAwareRuntime runtime = createRuntime(database, defaultConfiguration(database));
        runtime.registerModule(mockModule1);
        runtime.registerModule(mockModule2);
        runtime.registerModule(mockModule3);

        runtime.start();

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

    @Test(expected = IllegalStateException.class)
    public void modulesCannotBeRegisteredAfterStart() {
        final RuntimeModule mockModule = mockTxModule();

        GraphAwareRuntime runtime = createRuntime(database, defaultConfiguration(database));
        runtime.start();
        runtime.registerModule(mockModule);
    }

    @Test
    public void multipleCallsToStartFrameworkHaveNoEffect() {
        GraphAwareRuntime runtime = createRuntime(database, defaultConfiguration(database));
        runtime.start();
        runtime.start();
        runtime.start();
        runtime.start();
    }

    @Test
    public void shutdownShouldBeCalledBeforeShutdown() {
        RuntimeModule mockModule = mockTxModule();

        GraphAwareRuntime runtime = createRuntime(database, defaultConfiguration(database));
        runtime.registerModule(mockModule);
        runtime.start();

        database.shutdown();

        verify(mockModule).shutdown();
    }

    @Test
    public void whenOneModuleThrowsAnExceptionThenOtherModulesShouldNotBeDelegatedTo() {
        RuntimeModule mockModule1 = mockTxModule(MOCK + "1");
        doThrow(new RuntimeException()).when(mockModule1).beforeCommit(any(ImprovedTransactionData.class));

        RuntimeModule mockModule2 = mockTxModule(MOCK + "2");

        GraphAwareRuntime runtime = createRuntime(database, defaultConfiguration(database));
        runtime.registerModule(mockModule1);
        runtime.registerModule(mockModule2);

        runtime.start();

        verify(mockModule1).start(database);
        verify(mockModule2).start(database);

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
        verifyNoMoreInteractions(mockModule1, mockModule2);
    }

    @Test
    public void whenOneModuleForcesRollbackThenModulesBeforeItShouldBeAware() {
        RuntimeModule mockModule1 = mockTxModule(MOCK + "1");
        RuntimeModule mockModule2 = mockTxModule(MOCK + "2");
        RuntimeModule mockModule3 = mockTxModule(MOCK + "3");

        doThrow(new DeliberateTransactionRollbackException()).when(mockModule2).beforeCommit(any(ImprovedTransactionData.class));

        GraphAwareRuntime runtime = createRuntime(database, defaultConfiguration(database));
        runtime.registerModule(mockModule1);
        runtime.registerModule(mockModule2);
        runtime.registerModule(mockModule3);

        runtime.start();

        verify(mockModule1).start(database);
        verify(mockModule2).start(database);
        verify(mockModule3).start(database);

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
        createRuntime(database, defaultConfiguration(database));

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

    private RuntimeModule mockTxModule() {
        return mockTxModule(MOCK);
    }

    private RuntimeModule mockTxModule(String id) {
        return mockTxModule(id, NullRuntimeModuleConfiguration.getInstance());
    }

    private RuntimeModule mockTxModule(RuntimeModuleConfiguration configuration) {
        return mockTxModule(MOCK, configuration);
    }

    private RuntimeModule mockTxModule(String id, RuntimeModuleConfiguration configuration) {
        RuntimeModule mockModule = mock(RuntimeModule.class);
        when(mockModule.getId()).thenReturn(id);
        when(mockModule.getConfiguration()).thenReturn(configuration);
        when(mockModule.beforeCommit(any(ImprovedTransactionData.class))).thenReturn("TEST_" + id);
        return mockModule;
    }

    private <T extends RuntimeModule> T mockTxModule(String id, Class<T> cls) {
        T mockModule = mock(cls);
        when(mockModule.getId()).thenReturn(id);
        when(mockModule.getConfiguration()).thenReturn( NullRuntimeModuleConfiguration.getInstance());
        when(mockModule.beforeCommit(any(ImprovedTransactionData.class))).thenReturn("TEST_" + id);
        return mockModule;
    }

    interface M1 extends RuntimeModule {

    }

    interface M2 extends RuntimeModule {

    }

    interface M7 extends RuntimeModule {

    }
}
