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
import com.graphaware.runtime.config.ModuleConfiguration;
import com.graphaware.runtime.config.NullModuleConfiguration;
import com.graphaware.runtime.module.DeliberateTransactionRollbackException;
import com.graphaware.runtime.module.Module;
import com.graphaware.tx.event.improved.api.ImprovedTransactionData;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.neo4j.graphdb.*;
import org.neo4j.harness.Neo4j;
import org.neo4j.harness.Neo4jBuilders;
import org.neo4j.internal.helpers.collection.Iterables;

import java.util.concurrent.atomic.AtomicBoolean;

import static com.graphaware.runtime.GraphAwareRuntimeFactory.createRuntime;
import static com.graphaware.runtime.config.FluentRuntimeConfiguration.defaultConfiguration;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit test for {@link CommunityRuntime}.
 */
public class CommunityRuntimeTest {

    private static final String MOCK = "MOCK";

    private Neo4j controls;
    private GraphDatabaseService database;

    @BeforeEach
    public void setUp() {
        controls = Neo4jBuilders.newInProcessBuilder().build();
        database = controls.defaultDatabaseService();
    }

    @AfterEach
    public void tearDown() {
        controls.close();
    }

    @Test
    public void shouldNotBeAllowedToCreateTwoRuntimes() {
        createRuntime(controls.databaseManagementService(), database);

        assertThrows(IllegalStateException.class, () -> {
            createRuntime(controls.databaseManagementService(), database);
        });
    }

    @Test
    public void nullShouldBeReturnedWhenNoRuntimeHasBeenRegisteredForDatabase() {
        assertNull(RuntimeRegistry.getRuntime(database.databaseName()));
    }

    @Test
    public void exceptionShouldBeThrownWhenNoRuntimeHasBeenRegisteredForDatabase() {
        assertThrows(IllegalStateException.class, () -> {
            assertNull(RuntimeRegistry.getStartedRuntime(database.databaseName()));
        });
    }

    @Test
    public void exceptionShouldBeThrownWhenRuntimeHasNotBeenStarted() {
        createRuntime(controls.databaseManagementService(), database);
        assertThrows(IllegalStateException.class, () -> {
            RuntimeRegistry.getStartedRuntime(database.databaseName());
        });
    }

    @Test
    public void registeredRuntimeShouldBeRetrieved() {
        GraphAwareRuntime runtime = createRuntime(controls.databaseManagementService(), database);
        assertEquals(runtime, RuntimeRegistry.getRuntime(database.databaseName()));
    }

    @Test
    public void registeredRuntimeShouldBeRetrieved2() {
        GraphAwareRuntime runtime = createRuntime(controls.databaseManagementService(), database);
        runtime.start();
        assertEquals(runtime, RuntimeRegistry.getStartedRuntime(database.databaseName()));
    }

    @Test
    public void shouldFailWaitingForRuntimeThatHasNotBeenStarted() {
        GraphAwareRuntime runtime = createRuntime(controls.databaseManagementService(), database);
        assertThrows(IllegalStateException.class, () -> {
            runtime.waitUntilStarted();
        });
    }

    @Test
    public void shouldWaitForRuntimeToStart() throws InterruptedException {
        final GraphAwareRuntime runtime = createRuntime(controls.databaseManagementService(), database);

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

    @Test
    public void shouldFailWhenStartIsNotCalledInOneSecond() {
        final GraphAwareRuntime runtime = createRuntime(controls.databaseManagementService(), database);

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

        assertThrows(IllegalStateException.class, () -> {
            runtime.waitUntilStarted();
        });
    }

    @Test
    public void moduleThrowingExceptionShouldRollbackTransaction() {
        Module mockModule = mockTxModule(MOCK);
        doThrow(new DeliberateTransactionRollbackException("Deliberate testing exception")).when(mockModule).beforeCommit(any(ImprovedTransactionData.class));

        GraphAwareRuntime runtime = createRuntime(controls.databaseManagementService(), database, defaultConfiguration(database));
        runtime.registerModule(mockModule);
        runtime.start();

        try (Transaction tx = database.beginTx()) {
            Node node = tx.createNode(new Label[]{});
            node.setProperty("test", "test");
            tx.commit();
        } catch (Exception e) {
            //ok
        }

        try (Transaction tx = database.beginTx()) {
            assertEquals(0, Iterables.count(tx.getAllNodes()));
            tx.commit();
        }
    }

    @Test
    public void moduleThrowingRuntimeExceptionShouldRollbackTransaction() {
        Module mockModule = mockTxModule(MOCK);
        doThrow(new RuntimeException("Deliberate testing exception")).when(mockModule).beforeCommit(any(ImprovedTransactionData.class));

        GraphAwareRuntime runtime = createRuntime(controls.databaseManagementService(), database, defaultConfiguration(database));
        runtime.registerModule(mockModule);
        runtime.start();

        try (Transaction tx = database.beginTx()) {
            Node node = tx.createNode(new Label[]{});
            node.setProperty("test", "test");
            tx.commit();
        } catch (Exception e) {
            //ok
        }

        try (Transaction tx = database.beginTx()) {
            assertEquals(0, Iterables.count(tx.getAllNodes()));
            tx.commit();
        }
    }

    @Test
    public void shouldNotBeAbleToRegisterDifferentModulesWithSameId() {
        GraphAwareRuntime runtime = createRuntime(controls.databaseManagementService(), database, defaultConfiguration(database));
        runtime.registerModule(mockTxModule());
        assertThrows(IllegalStateException.class, () -> {
            runtime.registerModule(mockTxModule());
        });
    }

    @Test
    public void modulesShouldBeAwareOfRollbackAfterConstraintViolation() {
        Node node1;

        try (Transaction tx = database.beginTx()) {
            node1 = tx.createNode(new Label[]{});
            node1.createRelationshipTo(tx.createNode(new Label[]{}), RelationshipType.withName("TEST"));
            tx.commit();
        }

        Module mockModule1 = mockTxModule(MOCK + "1");
        Module mockModule2 = mockTxModule(MOCK + "2");

        GraphAwareRuntime runtime = createRuntime(controls.databaseManagementService(), database, defaultConfiguration(database));
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
                tx.commit();
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

        GraphAwareRuntime runtime = createRuntime(controls.databaseManagementService(), database, defaultConfiguration(database));
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

    @Test
    public void shouldThrowExceptionWhenAskedForNonExistingModule() {
        GraphAwareRuntime runtime = createRuntime(controls.databaseManagementService(), database, defaultConfiguration(database));
        assertThrows(NotFoundException.class, () -> {
            runtime.getModule("non-existing", Module.class);
        });
    }

    @Test
    public void shouldNotBeAbleToRegisterTheSameModuleTwice() {
        final Module mockModule = mockTxModule();

        GraphAwareRuntime runtime = createRuntime(controls.databaseManagementService(), database, defaultConfiguration(database));
        runtime.registerModule(mockModule);
        assertThrows(IllegalStateException.class, () -> {
            runtime.registerModule(mockModule);
        });
    }

    @Test
    public void shouldNotBeAbleToRegisterModuleWithTheSameIdTwice() {
        final Module mockModule1 = mockTxModule();
        final Module mockModule2 = mockTxModule();
        when(mockModule1.getId()).thenReturn("ID");
        when(mockModule2.getId()).thenReturn("ID");

        GraphAwareRuntime runtime = createRuntime(controls.databaseManagementService(), database, defaultConfiguration(database));
        runtime.registerModule(mockModule1);
        assertThrows(IllegalStateException.class, () -> {
            runtime.registerModule(mockModule2);
        });
    }

    @Test
    public void allRegisteredInterestedModulesShouldBeDelegatedTo() {
        Module mockModule1 = mockTxModule(MOCK + "1");
        Module mockModule2 = mockTxModule(MOCK + "2");
        Module mockModule3 = mockTxModule(MOCK + "3", FluentModuleConfiguration.defaultConfiguration().with(InclusionPolicies.none()));

        GraphAwareRuntime runtime = createRuntime(controls.databaseManagementService(), database, defaultConfiguration(database));
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
            tx.createNode(new Label[]{});
            tx.commit();
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
    public void modulesCannotBeRegisteredAfterStart() {
        final Module mockModule = mockTxModule();

        GraphAwareRuntime runtime = createRuntime(controls.databaseManagementService(), database, defaultConfiguration(database));
        runtime.start();
        assertThrows(IllegalStateException.class, () -> {
            runtime.registerModule(mockModule);
        });
    }

    @Test
    public void multipleCallsToStartFrameworkHaveNoEffect() {
        GraphAwareRuntime runtime = createRuntime(controls.databaseManagementService(), database, defaultConfiguration(database));
        runtime.start();
        runtime.start();
        runtime.start();
        runtime.start();
    }

    @Test
    public void shutdownShouldBeCalledBeforeShutdown() {
        Module mockModule = mockTxModule();

        GraphAwareRuntime runtime = createRuntime(controls.databaseManagementService(), database, defaultConfiguration(database));
        runtime.registerModule(mockModule);
        runtime.start();

        controls.close();

        verify(mockModule).shutdown();
    }

    @Test
    public void whenOneModuleThrowsAnExceptionThenOtherModulesShouldNotBeDelegatedTo() {
        Module mockModule1 = mockTxModule(MOCK + "1");
        doThrow(new RuntimeException()).when(mockModule1).beforeCommit(any(ImprovedTransactionData.class));

        Module mockModule2 = mockTxModule(MOCK + "2");

        GraphAwareRuntime runtime = createRuntime(controls.databaseManagementService(), database, defaultConfiguration(database));
        runtime.registerModule(mockModule1);
        runtime.registerModule(mockModule2);

        runtime.start();

        verify(mockModule1).start(database);
        verify(mockModule2).start(database);

        verify(mockModule1, atLeastOnce()).getId();
        verify(mockModule2, atLeastOnce()).getId();
        verifyNoMoreInteractions(mockModule1, mockModule2);

        try (Transaction tx = database.beginTx()) {
            tx.createNode(new Label[]{});
            tx.commit();
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
        Module mockModule1 = mockTxModule(MOCK + "1");
        Module mockModule2 = mockTxModule(MOCK + "2");
        Module mockModule3 = mockTxModule(MOCK + "3");

        doThrow(new DeliberateTransactionRollbackException()).when(mockModule2).beforeCommit(any(ImprovedTransactionData.class));

        GraphAwareRuntime runtime = createRuntime(controls.databaseManagementService(), database, defaultConfiguration(database));
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
                tx.createNode(new Label[]{});
                tx.commit();
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

    @Test
    public void whenRuntimeIsNotStartedExceptionShouldBeThrown() {
        createRuntime(controls.databaseManagementService(), database, defaultConfiguration(database));

        assertThrows(RuntimeException.class, () -> {
            try (Transaction tx = database.beginTx()) {
                tx.createNode(new Label[]{});
                tx.commit();
            }
        });
    }

    private Module mockTxModule() {
        return mockTxModule(MOCK);
    }

    private Module mockTxModule(String id) {
        return mockTxModule(id, NullModuleConfiguration.getInstance());
    }

    private Module mockTxModule(ModuleConfiguration configuration) {
        return mockTxModule(MOCK, configuration);
    }

    private Module mockTxModule(String id, ModuleConfiguration configuration) {
        Module mockModule = mock(Module.class);
        when(mockModule.getId()).thenReturn(id);
        when(mockModule.getConfiguration()).thenReturn(configuration);
        when(mockModule.beforeCommit(any(ImprovedTransactionData.class))).thenReturn("TEST_" + id);
        return mockModule;
    }

    private <T extends Module> T mockTxModule(String id, Class<T> cls) {
        T mockModule = mock(cls);
        when(mockModule.getId()).thenReturn(id);
        when(mockModule.getConfiguration()).thenReturn( NullModuleConfiguration.getInstance());
        when(mockModule.beforeCommit(any(ImprovedTransactionData.class))).thenReturn("TEST_" + id);
        return mockModule;
    }

    interface M1 extends Module {

    }

    interface M2 extends Module {

    }

    interface M7 extends Module {

    }
}
