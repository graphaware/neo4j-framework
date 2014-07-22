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

import com.graphaware.runtime.config.ScheduleConfiguration;

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

		ScheduleConfiguration scheduleConfiguration = Mockito.mock(ScheduleConfiguration.class);
		Mockito.stub(scheduleConfiguration.databaseActivityThreshold()).toReturn(10);
		Mockito.stub(scheduleConfiguration.defaultDelayMillis()).toReturn(2000L);
		Mockito.stub(scheduleConfiguration.maximumDelayMillis()).toReturn(10_000L);
		Mockito.stub(scheduleConfiguration.minimumDelayMillis()).toReturn(100L);

		timingStrategy = new AdaptiveTimingStrategy(graphDatabase, scheduleConfiguration);
	}

	@Test
	public void shouldUseInitialDelayFromGivenConfiguration() {
		Mockito.stub(txManager.getStartedTxCount()).toReturn(9);

		long nextDelay = timingStrategy.nextDelay(9L);
		assertThat(nextDelay, is(equalTo(2000L)));
	}

	@Test
	public void shouldIncreaseDelayFromPreviousIfCurrentPeriodIsDeemedToBeBusy() {
		Mockito.stub(txManager.getStartedTxCount()).toReturn(15).toReturn(32).toReturn(49);

		// set the state so that we have established a concept of business
		timingStrategy.nextDelay(30L);
		timingStrategy.nextDelay(27L);

		long nextDelay = timingStrategy.nextDelay(19L);
		assertTrue(nextDelay > 2000L);
	}

	@Test
	public void shouldDecreaseDelayFromPreviousIfCurrentPeriodIsDeemedToBeQuiet() {
		Mockito.stub(txManager.getStartedTxCount()).toReturn(25).toReturn(29).toReturn(32).toReturn(35);

		timingStrategy.nextDelay(16L);
		timingStrategy.nextDelay(12L);
		timingStrategy.nextDelay(14L);

		long nextDelay = timingStrategy.nextDelay(40L);
		assertTrue(nextDelay < 2000L);
	}

}
