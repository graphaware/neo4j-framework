package com.graphaware.runtime.schedule;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.neo4j.graphdb.DependencyResolver;
import org.neo4j.kernel.GraphDatabaseAPI;
import org.neo4j.kernel.impl.transaction.TxManager;

@SuppressWarnings("deprecation")
public class AdaptiveTimingStrategyTest {

    private TxManager txManager;
    private AdaptiveTimingStrategy timingStrategy;

    /**
     * Sets the up.
     */
    @Before
    public void setUp() {
        txManager = Mockito.mock(TxManager.class);
        GraphDatabaseAPI graphDatabase = Mockito.mock(GraphDatabaseAPI.class);
        DependencyResolver dependencyResolver = Mockito.mock(DependencyResolver.class);
        Mockito.stub(graphDatabase.getDependencyResolver()).toReturn(dependencyResolver);
        Mockito.stub(dependencyResolver.resolveDependency(TxManager.class)).toReturn(txManager);

        timingStrategy = AdaptiveTimingStrategy
                .defaultConfiguration()
                .withBusyThreshold(10)
                .withDefaultDelayMillis(1000L)
                .withMinimumDelayMillis(100L)
                .withMaximumDelayMillis(10_000L)
                .withDelta(100)
                .withMaxSamples(10)
                .withMaxTime(1000);

        timingStrategy.initialize(graphDatabase);
    }

    @Test
    public void shouldUseInitialDelayFromGivenConfiguration() {
        Mockito.stub(txManager.getStartedTxCount()).toReturn(9);

        long nextDelay = timingStrategy.nextDelay(0L);
        assertThat(nextDelay, is(equalTo(1000L)));
    }

    @Test
    public void shouldIncreaseDelayFromPreviousIfCurrentPeriodIsDeemedToBeBusy() throws InterruptedException {
        Mockito.stub(txManager.getStartedTxCount()).toReturn(10).toReturn(20).toReturn(30);

        // set the state so that we have established a concept of business
        timingStrategy.nextDelay(30_000_000L);
        Thread.sleep(100);
        timingStrategy.nextDelay(27_000_000L);
        Thread.sleep(100);

        long nextDelay = timingStrategy.nextDelay(0L);
        assertTrue(nextDelay > 1000L);
    }

    @Test
    public void shouldDecreaseDelayFromPreviousIfCurrentPeriodIsDeemedToBeQuiet() throws InterruptedException {
        Mockito.stub(txManager.getStartedTxCount()).toReturn(1).toReturn(2).toReturn(3).toReturn(3);

        timingStrategy.nextDelay(16_000_000L);
        Thread.sleep(100);
        timingStrategy.nextDelay(12_000_000L);
        Thread.sleep(100);
        timingStrategy.nextDelay(14_000_000L);
        Thread.sleep(100);

        long nextDelay = timingStrategy.nextDelay(0L);
        assertTrue(nextDelay < 1000L);
    }
}
