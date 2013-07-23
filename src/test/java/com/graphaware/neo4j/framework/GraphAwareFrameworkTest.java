package com.graphaware.neo4j.framework;

import com.graphaware.neo4j.framework.config.DefaultFrameworkConfiguration;
import com.graphaware.neo4j.framework.config.FrameworkConfigured;
import com.graphaware.neo4j.framework.strategy.InclusionStrategiesImpl;
import com.graphaware.neo4j.tx.event.api.ImprovedTransactionData;
import com.graphaware.neo4j.tx.single.SimpleTransactionExecutor;
import com.graphaware.neo4j.tx.single.VoidReturningCallback;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.test.TestGraphDatabaseFactory;

import static com.graphaware.neo4j.framework.GraphAwareFramework.FORCE_INITIALIZATION;
import static com.graphaware.neo4j.framework.GraphAwareFramework.HASH_CODE;
import static com.graphaware.neo4j.framework.config.FrameworkConfiguration.*;
import static com.graphaware.neo4j.utils.IterableUtils.count;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

/**
 * Unit test for {@link GraphAwareFramework}.
 */
public class GraphAwareFrameworkTest {

    private GraphDatabaseService database;

    @Before
    public void setUp() {
        database = new TestGraphDatabaseFactory().newImpermanentDatabase();
    }

    @After
    public void tearDown() {
        database.shutdown();
    }

    @Test(expected = IllegalStateException.class)
    public void shouldNotWorkOnDatabaseWithNoRootNode() {
        new SimpleTransactionExecutor(database).executeInTransaction(new VoidReturningCallback() {
            @Override
            protected void doInTx(GraphDatabaseService database) {
                database.getNodeById(0).delete();
            }
        });

        new GraphAwareFramework(database);
    }

    @Test
    public void moduleRegisteredForTheFirstTimeShouldBeInitialized() {
        GraphAwareModule mockModule = mock(GraphAwareModule.class);
        when(mockModule.getId()).thenReturn("MOCK");

        GraphAwareFramework framework = new GraphAwareFramework(database);
        framework.registerModule(mockModule);

        framework.start();

        verify(mockModule).initialize(database);
        verify(mockModule, atLeastOnce()).getId();
        verifyNoMoreInteractions(mockModule);

        assertEquals(HASH_CODE + mockModule.hashCode(), database.getNodeById(0).getProperty(GA_PREFIX + "MOCK").toString());
    }

    @Test
    public void moduleAlreadyRegisteredShouldNotBeInitialized() {
        final GraphAwareModule mockModule = mock(GraphAwareModule.class);
        when(mockModule.getId()).thenReturn("MOCK");

        new SimpleTransactionExecutor(database).executeInTransaction(new VoidReturningCallback() {
            @Override
            protected void doInTx(GraphDatabaseService database) {
                database.getNodeById(0).setProperty(GA_PREFIX + "MOCK", HASH_CODE + mockModule.hashCode());
            }
        });

        GraphAwareFramework framework = new GraphAwareFramework(database);
        framework.registerModule(mockModule);

        framework.start();

        verify(mockModule, atLeastOnce()).getId();
        verifyNoMoreInteractions(mockModule);

        assertEquals(HASH_CODE + mockModule.hashCode(), database.getNodeById(0).getProperty(GA_PREFIX + "MOCK").toString());
    }

    @Test
    public void changedModuleShouldBeReInitialized() {
        final GraphAwareModule mockModule = mock(GraphAwareModule.class);
        when(mockModule.getId()).thenReturn("MOCK");

        new SimpleTransactionExecutor(database).executeInTransaction(new VoidReturningCallback() {
            @Override
            protected void doInTx(GraphDatabaseService database) {
                database.getNodeById(0).setProperty(GA_PREFIX + "MOCK", HASH_CODE + "123");
            }
        });

        GraphAwareFramework framework = new GraphAwareFramework(database);
        framework.registerModule(mockModule);

        framework.start();

        verify(mockModule).reinitialize(database);
        verify(mockModule, atLeastOnce()).getId();
        verifyNoMoreInteractions(mockModule);

        assertEquals(HASH_CODE + mockModule.hashCode(), database.getNodeById(0).getProperty(GA_PREFIX + "MOCK").toString());
    }

    @Test
    public void forcedModuleShouldBeReInitialized() {
        final GraphAwareModule mockModule = mock(GraphAwareModule.class);
        when(mockModule.getId()).thenReturn("MOCK");

        new SimpleTransactionExecutor(database).executeInTransaction(new VoidReturningCallback() {
            @Override
            protected void doInTx(GraphDatabaseService database) {
                database.getNodeById(0).setProperty(GA_PREFIX + "MOCK", FORCE_INITIALIZATION + "123");
            }
        });

        GraphAwareFramework framework = new GraphAwareFramework(database);
        framework.registerModule(mockModule);

        framework.start();

        verify(mockModule).reinitialize(database);
        verify(mockModule, atLeastOnce()).getId();
        verifyNoMoreInteractions(mockModule);

        assertEquals(HASH_CODE + mockModule.hashCode(), database.getNodeById(0).getProperty(GA_PREFIX + "MOCK").toString());
    }

    @Test
    public void moduleAlreadyRegisteredShouldBeInitializedWhenForced() {
        final GraphAwareModule mockModule = mock(GraphAwareModule.class);
        when(mockModule.getId()).thenReturn("MOCK");

        new SimpleTransactionExecutor(database).executeInTransaction(new VoidReturningCallback() {
            @Override
            protected void doInTx(GraphDatabaseService database) {
                database.getNodeById(0).setProperty(GA_PREFIX + "MOCK", HASH_CODE + mockModule.hashCode());
            }
        });

        GraphAwareFramework framework = new GraphAwareFramework(database);
        framework.registerModule(mockModule, true);

        framework.start();

        verify(mockModule).reinitialize(database);
        verify(mockModule, atLeastOnce()).getId();
        verifyNoMoreInteractions(mockModule);

        assertEquals(HASH_CODE + mockModule.hashCode(), database.getNodeById(0).getProperty(GA_PREFIX + "MOCK").toString());
    }

    @Test
    public void changedModuleShouldNotBeReInitializedWhenInitializationSkipped() {
        final GraphAwareModule mockModule = mock(GraphAwareModule.class);
        when(mockModule.getId()).thenReturn("MOCK");

        new SimpleTransactionExecutor(database).executeInTransaction(new VoidReturningCallback() {
            @Override
            protected void doInTx(GraphDatabaseService database) {
                database.getNodeById(0).setProperty(GA_PREFIX + "MOCK", HASH_CODE + "123");
            }
        });

        GraphAwareFramework framework = new GraphAwareFramework(database);
        framework.registerModule(mockModule);

        framework.start(true);

        verify(mockModule, atLeastOnce()).getId();
        verifyNoMoreInteractions(mockModule);

        assertEquals(HASH_CODE + "123", database.getNodeById(0).getProperty(GA_PREFIX + "MOCK").toString());
    }

    @Test(expected = IllegalStateException.class)
    public void shouldNotBeAbleToRegisterModuleWithTheSameIdTwice() {
        final GraphAwareModule mockModule = mock(GraphAwareModule.class);
        when(mockModule.getId()).thenReturn("MOCK");

        GraphAwareFramework framework = new GraphAwareFramework(database);
        framework.registerModule(mockModule);
        framework.registerModule(mockModule);
    }

    @Test
    public void unusedModulesShouldBeRemoved() {
        final GraphAwareModule mockModule = mock(GraphAwareModule.class);
        when(mockModule.getId()).thenReturn("MOCK");

        new SimpleTransactionExecutor(database).executeInTransaction(new VoidReturningCallback() {
            @Override
            protected void doInTx(GraphDatabaseService database) {
                Node root = database.getNodeById(0);
                root.setProperty(GA_PREFIX + "MOCK", HASH_CODE + mockModule.hashCode());
                root.setProperty(GA_PREFIX + "UNUSED", HASH_CODE + "123");
            }
        });

        GraphAwareFramework framework = new GraphAwareFramework(database);
        framework.registerModule(mockModule);

        framework.start();

        verify(mockModule, atLeastOnce()).getId();
        verifyNoMoreInteractions(mockModule);

        assertEquals(1, count(database.getNodeById(0).getPropertyKeys()));
    }

    @Test(expected = IllegalStateException.class)
    public void usedCorruptModulesShouldThrowException() {
        final GraphAwareModule mockModule = mock(GraphAwareModule.class);
        when(mockModule.getId()).thenReturn("MOCK");

        new SimpleTransactionExecutor(database).executeInTransaction(new VoidReturningCallback() {
            @Override
            protected void doInTx(GraphDatabaseService database) {
                Node root = database.getNodeById(0);
                root.setProperty(GA_PREFIX + "MOCK", "CORRUPT");
            }
        });

        GraphAwareFramework framework = new GraphAwareFramework(database);
        framework.registerModule(mockModule);

        framework.start();
    }

    @Test
    public void unusedCorruptModulesShouldBeRemoved() {
        final GraphAwareModule mockModule = mock(GraphAwareModule.class);
        when(mockModule.getId()).thenReturn("MOCK");

        new SimpleTransactionExecutor(database).executeInTransaction(new VoidReturningCallback() {
            @Override
            protected void doInTx(GraphDatabaseService database) {
                Node root = database.getNodeById(0);
                root.setProperty(GA_PREFIX + "MOCK", HASH_CODE + mockModule.hashCode());
                root.setProperty(GA_PREFIX + "UNUSED", "CORRUPT");
            }
        });

        GraphAwareFramework framework = new GraphAwareFramework(database);
        framework.registerModule(mockModule);

        framework.start();

        verify(mockModule, atLeastOnce()).getId();
        verifyNoMoreInteractions(mockModule);

        assertEquals(1, count(database.getNodeById(0).getPropertyKeys()));
    }

    @Test
    public void allRegisteredInterestedModulesShouldBeDelegatedTo() {
        GraphAwareModule mockModule1 = mock(GraphAwareModule.class);
        when(mockModule1.getId()).thenReturn("MOCK1");
        when(mockModule1.getInclusionStrategies()).thenReturn(InclusionStrategiesImpl.all());

        GraphAwareModule mockModule2 = mock(GraphAwareModule.class);
        when(mockModule2.getId()).thenReturn("MOCK2");
        when(mockModule2.getInclusionStrategies()).thenReturn(InclusionStrategiesImpl.all());

        GraphAwareModule mockModule3 = mock(GraphAwareModule.class);
        when(mockModule3.getId()).thenReturn("MOCK3");
        when(mockModule3.getInclusionStrategies()).thenReturn(InclusionStrategiesImpl.none());

        GraphAwareFramework framework = new GraphAwareFramework(database);
        framework.registerModule(mockModule1);
        framework.registerModule(mockModule2);
        framework.registerModule(mockModule3);

        framework.start();

        verify(mockModule1).initialize(database);
        verify(mockModule2).initialize(database);
        verify(mockModule3).initialize(database);
        verify(mockModule1, atLeastOnce()).getId();
        verify(mockModule2, atLeastOnce()).getId();
        verify(mockModule3, atLeastOnce()).getId();
        verifyNoMoreInteractions(mockModule1, mockModule2, mockModule3);

        new SimpleTransactionExecutor(database).executeInTransaction(new VoidReturningCallback() {
            @Override
            protected void doInTx(GraphDatabaseService database) {
                database.createNode();
            }
        });

        verify(mockModule1).getInclusionStrategies();
        verify(mockModule2).getInclusionStrategies();
        verify(mockModule3).getInclusionStrategies();
        verify(mockModule1).beforeCommit(any(ImprovedTransactionData.class));
        verify(mockModule2).beforeCommit(any(ImprovedTransactionData.class));
        //no interaction with module3, it is not interested!
        verifyNoMoreInteractions(mockModule1, mockModule2, mockModule3);
    }

    @Test
    public void moduleThrowingInitExceptionShouldBeMarkedForReinitialization() {
        final GraphAwareModule mockModule = mock(GraphAwareModule.class);
        when(mockModule.getId()).thenReturn("MOCK");
        when(mockModule.getInclusionStrategies()).thenReturn(InclusionStrategiesImpl.all());
        doThrow(new NeedsInitializationException()).when(mockModule).beforeCommit(any(ImprovedTransactionData.class));

        GraphAwareFramework framework = new GraphAwareFramework(database);
        framework.registerModule(mockModule);

        framework.start();

        assertTrue(database.getNodeById(0).getProperty(GA_PREFIX + "MOCK").toString().startsWith(HASH_CODE));

        new SimpleTransactionExecutor(database).executeInTransaction(new VoidReturningCallback() {
            @Override
            protected void doInTx(GraphDatabaseService database) {
                database.createNode();
            }
        });

        assertTrue(database.getNodeById(0).getProperty(GA_PREFIX + "MOCK").toString().startsWith(FORCE_INITIALIZATION));
    }

    @Test(expected = IllegalStateException.class)
    public void modulesCannotBeRegisteredAfterStart() {
        final GraphAwareModule mockModule = mock(GraphAwareModule.class);
        when(mockModule.getId()).thenReturn("MOCK");

        GraphAwareFramework framework = new GraphAwareFramework(database);
        framework.start(true);
        framework.registerModule(mockModule);
    }

    @Test(expected = IllegalStateException.class)
    public void frameworkCanOnlyBeStartedOnce() {
        GraphAwareFramework framework = new GraphAwareFramework(database);
        framework.start();
        framework.start();
    }

    @Test
    public void frameworkConfiguredModulesShouldBeConfigured() {
        FrameworkConfiguredModule mockModule = mock(FrameworkConfiguredModule.class);
        when(mockModule.getId()).thenReturn("MOCK");

        GraphAwareFramework framework = new GraphAwareFramework(database);
        framework.registerModule(mockModule);

        verify(mockModule).configurationChanged(DefaultFrameworkConfiguration.getInstance());
        verify(mockModule, atLeastOnce()).getId();
        verifyNoMoreInteractions(mockModule);
    }

    @Test
    public void frameworkConfiguredModulesShouldBeConfigured2() {
        FrameworkConfiguredModule mockModule = mock(FrameworkConfiguredModule.class);
        when(mockModule.getId()).thenReturn("MOCK");

        GraphAwareFramework framework = new GraphAwareFramework(database);
        framework.registerModule(mockModule, true);

        verify(mockModule).configurationChanged(DefaultFrameworkConfiguration.getInstance());
        verify(mockModule, atLeastOnce()).getId();
        verifyNoMoreInteractions(mockModule);
    }

    @Test
    public void shutdownShouldBeCalledBeforeShutdown() {
        FrameworkConfiguredModule mockModule = mock(FrameworkConfiguredModule.class);
        when(mockModule.getId()).thenReturn("MOCK");

        GraphAwareFramework framework = new GraphAwareFramework(database);
        framework.registerModule(mockModule);
        framework.start();

        database.shutdown();

        verify(mockModule).shutdown();
    }

    private interface FrameworkConfiguredModule extends GraphAwareModule, FrameworkConfigured {

    }
}
