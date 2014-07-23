package com.graphaware.runtime.schedule;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.kernel.GraphDatabaseAPI;
import org.neo4j.kernel.impl.transaction.TxManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.graphaware.runtime.config.ScheduleConfiguration;

/**
 * Implementation of {@link TimingStrategy} that pays attention to the current level of activity in the database in order to
 * decide how long to wait before scheduling the next task.
 */
@SuppressWarnings("deprecation")
public class AdaptiveTimingStrategy implements TimingStrategy {

	private static final Logger LOG = LoggerFactory.getLogger(AdaptiveTimingStrategy.class);

	private final TxManager transactionManager;
	private final DelayAdjuster delayAdjuster;
	private final long defaultDelayMillis;
	private final int threshold;

	private int txCountAtPreviousInvocation;
	private long delayAtPreviousInvocation;

	/**
	 * Constructs a new {@link AdaptiveTimingStrategy} based on the activity in the given database and tuned with the specified
	 * configuration settings.
	 *
	 * @param database The {@link GraphDatabaseService} to monitor for activity and adapt accordingly.
	 * @param config The {@link ScheduleConfiguration} containing the settings for this timing strategy.
	 */
	public AdaptiveTimingStrategy(GraphDatabaseService database, ScheduleConfiguration config) {
		this.transactionManager = ((GraphDatabaseAPI) database).getDependencyResolver().resolveDependency(TxManager.class);
		this.delayAdjuster = new ConstantDeltaDelayAdjuster(100, config.minimumDelayMillis(), config.maximumDelayMillis());
		this.defaultDelayMillis = config.defaultDelayMillis();
		this.threshold = config.databaseActivityThreshold();
		this.txCountAtPreviousInvocation = -1;
	}

	@Override
	public long nextDelay(long lastTaskDuration) {
		int currentTxCount = transactionManager.getStartedTxCount();
		long newDelay = determineNewDelay(currentTxCount);

		LOG.trace("Next delay updated to {}ms based on transaction count difference of {}", newDelay, currentTxCount - txCountAtPreviousInvocation);

		txCountAtPreviousInvocation = currentTxCount;
		delayAtPreviousInvocation = newDelay;
		return newDelay;
	}

	private long determineNewDelay(int currentTxCount) {
		if (txCountAtPreviousInvocation == -1) {
			LOG.debug("First request for timing delay so returning default of: {}ms", defaultDelayMillis);
			return defaultDelayMillis;
		}
		return delayAdjuster.determineNextDelay(delayAtPreviousInvocation, currentTxCount - txCountAtPreviousInvocation, threshold);
	}

}
