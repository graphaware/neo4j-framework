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

package com.graphaware.runtime.schedule;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.neo4j.graphdb.DependencyResolver;
import org.neo4j.kernel.impl.transaction.TransactionCounters;
import org.neo4j.kernel.internal.GraphDatabaseAPI;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@SuppressWarnings("deprecation")
public class AdaptiveTimingStrategyTest {

    private TransactionCounters txCounters;
    private AdaptiveTimingStrategy timingStrategy;

    /**
     * Sets the up.
     */
    @Before
    public void setUp() {
        txCounters = Mockito.mock(TransactionCounters.class);
        GraphDatabaseAPI graphDatabase = Mockito.mock(GraphDatabaseAPI.class);
        DependencyResolver dependencyResolver = Mockito.mock(DependencyResolver.class);
        Mockito.stub(graphDatabase.getDependencyResolver()).toReturn(dependencyResolver);
        Mockito.stub(dependencyResolver.resolveDependency(TransactionCounters.class)).toReturn(txCounters);

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
        Mockito.stub(txCounters.getNumberOfStartedTransactions()).toReturn(9L);

        long nextDelay = timingStrategy.nextDelay(0L);
        assertThat(nextDelay, is(equalTo(1000L)));
    }

    @Test
    public void shouldIncreaseDelayFromPreviousIfCurrentPeriodIsDeemedToBeBusy() throws InterruptedException {
        Mockito.stub(txCounters.getNumberOfStartedTransactions()).toReturn(10L).toReturn(20L).toReturn(30L);

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
        Mockito.stub(txCounters.getNumberOfStartedTransactions()).toReturn(1L).toReturn(2L).toReturn(3L).toReturn(3L);

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
