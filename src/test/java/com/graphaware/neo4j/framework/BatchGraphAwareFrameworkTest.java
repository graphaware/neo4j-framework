package com.graphaware.neo4j.framework;

import com.graphaware.neo4j.framework.config.DefaultFrameworkConfiguration;
import com.graphaware.neo4j.framework.config.FrameworkConfigured;
import com.graphaware.neo4j.framework.strategy.InclusionStrategiesImpl;
import com.graphaware.neo4j.tx.batch.api.TransactionSimulatingBatchInserter;
import com.graphaware.neo4j.tx.event.api.ImprovedTransactionData;
import com.graphaware.neo4j.tx.single.SimpleTransactionExecutor;
import com.graphaware.neo4j.tx.single.VoidReturningCallback;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.TransactionFailureException;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.test.TestGraphDatabaseFactory;
import org.neo4j.unsafe.batchinsert.BatchInserter;
import org.neo4j.unsafe.batchinsert.TransactionSimulatingBatchInserterImpl;

import static com.graphaware.neo4j.framework.GraphAwareFramework.*;
import static com.graphaware.neo4j.framework.config.FrameworkConfiguration.GA_PREFIX;
import static com.graphaware.neo4j.utils.IterableUtils.count;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

/**
 * Unit test for {@link com.graphaware.neo4j.framework.GraphAwareFramework}.
 */
public class BatchGraphAwareFrameworkTest {

    private String dir;

    @Before
    public void setUp() {
        dir = "/tmp/" + System.currentTimeMillis();
    }

    @Test(expected = IllegalStateException.class)
    public void shouldNotWorkOnDatabaseWithNoRootNode() {
        GraphDatabaseService database = new GraphDatabaseFactory().newEmbeddedDatabase(dir);

        new SimpleTransactionExecutor(database).executeInTransaction(new VoidReturningCallback() {
            @Override
            protected void doInTx(GraphDatabaseService database) {
                database.getNodeById(0).delete();
            }
        });

        database.shutdown();

        new BatchGraphAwareFramework(new TransactionSimulatingBatchInserterImpl(dir));
    }

    @Test
    public void moduleRegisteredForTheFirstTimeShouldBeInitialized() {
        GraphAwareModule mockModule = mock(GraphAwareModule.class);
        when(mockModule.getId()).thenReturn("MOCK");

        TransactionSimulatingBatchInserter batchInserter = new TransactionSimulatingBatchInserterImpl(dir);
        BatchGraphAwareFramework framework = new BatchGraphAwareFramework(batchInserter);
        framework.registerModule(mockModule);

        framework.start();

        verify(mockModule).initialize(batchInserter);
        verify(mockModule, atLeastOnce()).getId();
        verifyNoMoreInteractions(mockModule);

        assertEquals(HASH_CODE + mockModule.hashCode(), batchInserter.getNodeProperties(0).get(GA_PREFIX + CORE + "_MOCK").toString());
    }

    @Test
    public void moduleAlreadyRegisteredShouldNotBeInitialized() {
        final GraphAwareModule mockModule = mock(GraphAwareModule.class);
        when(mockModule.getId()).thenReturn("MOCK");

        TransactionSimulatingBatchInserter batchInserter = new TransactionSimulatingBatchInserterImpl(dir);
        batchInserter.setNodeProperty(0, GA_PREFIX + CORE + "_MOCK", HASH_CODE + mockModule.hashCode());

        BatchGraphAwareFramework framework = new BatchGraphAwareFramework(batchInserter);
        framework.registerModule(mockModule);

        framework.start();

        verify(mockModule, atLeastOnce()).getId();
        verifyNoMoreInteractions(mockModule);

        assertEquals(HASH_CODE + mockModule.hashCode(), batchInserter.getNodeProperties(0).get(GA_PREFIX + CORE + "_MOCK").toString());
    }

    @Test
    public void changedModuleShouldBeReInitialized() {
        final GraphAwareModule mockModule = mock(GraphAwareModule.class);
        when(mockModule.getId()).thenReturn("MOCK");

        TransactionSimulatingBatchInserter batchInserter = new TransactionSimulatingBatchInserterImpl(dir);
        batchInserter.setNodeProperty(0, GA_PREFIX + CORE + "_MOCK", HASH_CODE + "123");

        BatchGraphAwareFramework framework = new BatchGraphAwareFramework(batchInserter);
        framework.registerModule(mockModule);

        framework.start();

        verify(mockModule).reinitialize(batchInserter);
        verify(mockModule, atLeastOnce()).getId();
        verifyNoMoreInteractions(mockModule);

        assertEquals(HASH_CODE + mockModule.hashCode(), batchInserter.getNodeProperties(0).get(GA_PREFIX + CORE + "_MOCK").toString());
    }

    @Test
    public void forcedModuleShouldBeReInitialized() {
        final GraphAwareModule mockModule = mock(GraphAwareModule.class);
        when(mockModule.getId()).thenReturn("MOCK");

        TransactionSimulatingBatchInserter batchInserter = new TransactionSimulatingBatchInserterImpl(dir);
        batchInserter.setNodeProperty(0, GA_PREFIX + CORE + "_MOCK", FORCE_INITIALIZATION + "123");

        BatchGraphAwareFramework framework = new BatchGraphAwareFramework(batchInserter);
        framework.registerModule(mockModule);

        framework.start();

        verify(mockModule).reinitialize(batchInserter);
        verify(mockModule, atLeastOnce()).getId();
        verifyNoMoreInteractions(mockModule);

        assertEquals(HASH_CODE + mockModule.hashCode(), batchInserter.getNodeProperties(0).get(GA_PREFIX + CORE + "_MOCK").toString());
    }

    @Test
    public void moduleAlreadyRegisteredShouldBeInitializedWhenForced() {
        final GraphAwareModule mockModule = mock(GraphAwareModule.class);
        when(mockModule.getId()).thenReturn("MOCK");

        TransactionSimulatingBatchInserter batchInserter = new TransactionSimulatingBatchInserterImpl(dir);
        batchInserter.setNodeProperty(0, GA_PREFIX + CORE + "_MOCK", HASH_CODE + mockModule.hashCode());

        BatchGraphAwareFramework framework = new BatchGraphAwareFramework(batchInserter);
        framework.registerModule(mockModule, true);

        framework.start();

        verify(mockModule).reinitialize(batchInserter);
        verify(mockModule, atLeastOnce()).getId();
        verifyNoMoreInteractions(mockModule);

        assertEquals(HASH_CODE + mockModule.hashCode(), batchInserter.getNodeProperties(0).get(GA_PREFIX + CORE + "_MOCK").toString());
    }

    @Test
    public void changedModuleShouldNotBeReInitializedWhenInitializationSkipped() {
        final GraphAwareModule mockModule = mock(GraphAwareModule.class);
        when(mockModule.getId()).thenReturn("MOCK");

        TransactionSimulatingBatchInserter batchInserter = new TransactionSimulatingBatchInserterImpl(dir);
        batchInserter.setNodeProperty(0, GA_PREFIX + CORE + "_MOCK", HASH_CODE + "123");

        BatchGraphAwareFramework framework = new BatchGraphAwareFramework(batchInserter);
        framework.registerModule(mockModule);

        framework.start(true);

        verify(mockModule, atLeastOnce()).getId();
        verifyNoMoreInteractions(mockModule);

        assertEquals(HASH_CODE + "123", batchInserter.getNodeProperties(0).get(GA_PREFIX + CORE + "_MOCK").toString());
    }

    @Test(expected = IllegalStateException.class)
    public void shouldNotBeAbleToRegisterModuleWithTheSameIdTwice() {
        final GraphAwareModule mockModule = mock(GraphAwareModule.class);
        when(mockModule.getId()).thenReturn("MOCK");

        TransactionSimulatingBatchInserter batchInserter = new TransactionSimulatingBatchInserterImpl(dir);
        BatchGraphAwareFramework framework = new BatchGraphAwareFramework(batchInserter);
        framework.registerModule(mockModule);
        framework.registerModule(mockModule);
    }

    @Test
    public void unusedModulesShouldBeRemoved() {
        final GraphAwareModule mockModule = mock(GraphAwareModule.class);
        when(mockModule.getId()).thenReturn("MOCK");

        TransactionSimulatingBatchInserter batchInserter = new TransactionSimulatingBatchInserterImpl(dir);
        batchInserter.setNodeProperty(0, GA_PREFIX + CORE + "_MOCK", HASH_CODE + mockModule.hashCode());
        batchInserter.setNodeProperty(0, GA_PREFIX + CORE + "_UNUSED", HASH_CODE + "123");

        BatchGraphAwareFramework framework = new BatchGraphAwareFramework(batchInserter);
        framework.registerModule(mockModule);

        framework.start();

        verify(mockModule, atLeastOnce()).getId();
        verifyNoMoreInteractions(mockModule);

        assertEquals(1, count(batchInserter.getNodeProperties(0).keySet()));
    }

    @Test(expected = IllegalStateException.class)
    public void usedCorruptModulesShouldThrowException() {
        final GraphAwareModule mockModule = mock(GraphAwareModule.class);
        when(mockModule.getId()).thenReturn("MOCK");


        TransactionSimulatingBatchInserter batchInserter = new TransactionSimulatingBatchInserterImpl(dir);
        batchInserter.setNodeProperty(0, GA_PREFIX + CORE + "_MOCK", "CORRUPT");

        BatchGraphAwareFramework framework = new BatchGraphAwareFramework(batchInserter);
        framework.registerModule(mockModule);

        framework.start();
    }

    @Test
    public void unusedCorruptModulesShouldBeRemoved() {
        final GraphAwareModule mockModule = mock(GraphAwareModule.class);
        when(mockModule.getId()).thenReturn("MOCK");

        TransactionSimulatingBatchInserter batchInserter = new TransactionSimulatingBatchInserterImpl(dir);
        batchInserter.setNodeProperty(0, GA_PREFIX + CORE + "_MOCK", HASH_CODE + mockModule.hashCode());
        batchInserter.setNodeProperty(0, GA_PREFIX + CORE + "_UNUSED", "CORRUPT");

        BatchGraphAwareFramework framework = new BatchGraphAwareFramework(batchInserter);
        framework.registerModule(mockModule);

        framework.start();

        verify(mockModule, atLeastOnce()).getId();
        verifyNoMoreInteractions(mockModule);

        assertEquals(1, count(batchInserter.getNodeProperties(0).keySet()));
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

        TransactionSimulatingBatchInserter batchInserter = new TransactionSimulatingBatchInserterImpl(dir);
        BatchGraphAwareFramework framework = new BatchGraphAwareFramework(batchInserter);
        framework.registerModule(mockModule1);
        framework.registerModule(mockModule2);
        framework.registerModule(mockModule3);

        framework.start();

        verify(mockModule1).initialize(batchInserter);
        verify(mockModule2).initialize(batchInserter);
        verify(mockModule3).initialize(batchInserter);
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
    public void moduleThrowingInitExceptionShouldBeMarkedForReinitialization() {
        final GraphAwareModule mockModule = mock(GraphAwareModule.class);
        when(mockModule.getId()).thenReturn("MOCK");
        when(mockModule.getInclusionStrategies()).thenReturn(InclusionStrategiesImpl.all());
        doThrow(new NeedsInitializationException()).when(mockModule).beforeCommit(any(ImprovedTransactionData.class));

        TransactionSimulatingBatchInserter batchInserter = new TransactionSimulatingBatchInserterImpl(dir);
        BatchGraphAwareFramework framework = new BatchGraphAwareFramework(batchInserter);
        framework.registerModule(mockModule);

        framework.start();

        assertTrue(batchInserter.getNodeProperties(0).get(GA_PREFIX + CORE + "_MOCK").toString().startsWith(HASH_CODE));

        batchInserter.createNode(null);
        batchInserter.shutdown();

        batchInserter = new TransactionSimulatingBatchInserterImpl(dir);
        assertTrue(batchInserter.getNodeProperties(0).get(GA_PREFIX + CORE + "_MOCK").toString().startsWith(FORCE_INITIALIZATION));
    }

    @Test(expected = IllegalStateException.class)
    public void modulesCannotBeRegisteredAfterStart() {
        final GraphAwareModule mockModule = mock(GraphAwareModule.class);
        when(mockModule.getId()).thenReturn("MOCK");

        TransactionSimulatingBatchInserter batchInserter = new TransactionSimulatingBatchInserterImpl(dir);
        BatchGraphAwareFramework framework = new BatchGraphAwareFramework(batchInserter);
        framework.start(true);
        framework.registerModule(mockModule);
    }

    @Test(expected = IllegalStateException.class)
    public void frameworkCanOnlyBeStartedOnce() {
        TransactionSimulatingBatchInserter batchInserter = new TransactionSimulatingBatchInserterImpl(dir);
        BatchGraphAwareFramework framework = new BatchGraphAwareFramework(batchInserter);
        framework.start();
        framework.start();
    }

    @Test
    public void frameworkConfiguredModulesShouldBeConfigured() {
        FrameworkConfiguredModule mockModule = mock(FrameworkConfiguredModule.class);
        when(mockModule.getId()).thenReturn("MOCK");

        TransactionSimulatingBatchInserter batchInserter = new TransactionSimulatingBatchInserterImpl(dir);
        BatchGraphAwareFramework framework = new BatchGraphAwareFramework(batchInserter);
        framework.registerModule(mockModule);

        verify(mockModule).configurationChanged(DefaultFrameworkConfiguration.getInstance());
        verify(mockModule, atLeastOnce()).getId();
        verifyNoMoreInteractions(mockModule);
    }

    @Test
    public void frameworkConfiguredModulesShouldBeConfigured2() {
        FrameworkConfiguredModule mockModule = mock(FrameworkConfiguredModule.class);
        when(mockModule.getId()).thenReturn("MOCK");

        TransactionSimulatingBatchInserter batchInserter = new TransactionSimulatingBatchInserterImpl(dir);
        BatchGraphAwareFramework framework = new BatchGraphAwareFramework(batchInserter);
        framework.registerModule(mockModule, true);

        verify(mockModule).configurationChanged(DefaultFrameworkConfiguration.getInstance());
        verify(mockModule, atLeastOnce()).getId();
        verifyNoMoreInteractions(mockModule);
    }

    @Test
    public void shutdownShouldBeCalledBeforeShutdown() {
        FrameworkConfiguredModule mockModule = mock(FrameworkConfiguredModule.class);
        when(mockModule.getId()).thenReturn("MOCK");

        TransactionSimulatingBatchInserter batchInserter = new TransactionSimulatingBatchInserterImpl(dir);
        BatchGraphAwareFramework framework = new BatchGraphAwareFramework(batchInserter);
        framework.registerModule(mockModule);
        framework.start();

        batchInserter.shutdown();

        verify(mockModule).shutdown();
    }

    private interface FrameworkConfiguredModule extends GraphAwareModule, FrameworkConfigured {

    }
}
