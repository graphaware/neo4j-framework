package com.graphaware.runtime.schedule;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.kernel.GraphDatabaseAPI;
import org.neo4j.kernel.impl.transaction.TxManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of {@link TimingStrategy} that pays attention to the current level of activity in the database in order to
 * decide how long to wait before scheduling the next task.
 */
@SuppressWarnings("deprecation")
public class AdaptiveTimingStrategy implements TimingStrategy {

	private static final Logger LOG = LoggerFactory.getLogger(AdaptiveTimingStrategy.class);

	private final TxManager transactionManager;
	private final long defaultDelayMillis;
	private final int threshold;

	private int txCountAtPreviousInvocation;
	private long delayAtPreviousInvocation;

	/**
	 * Work in progress...
	 *
	 * @param database
	 * @param defaultDelayMillis
	 */
	public AdaptiveTimingStrategy(GraphDatabaseService database, long defaultDelayMillis) {
		this.transactionManager = ((GraphDatabaseAPI) database).getDependencyResolver().resolveDependency(TxManager.class);
		this.defaultDelayMillis = defaultDelayMillis;
		this.threshold = 10; // TODO what constitutes busy?  Make this a configurable threshold
		this.txCountAtPreviousInvocation = -1;
	}

	@Override
	public long nextDelay(long lastTaskDuration) {
		int currentTxCount = transactionManager.getStartedTxCount();
		long newDelay = determineNewDelay(currentTxCount);

		LOG.debug("Next delay updated to {}ms", newDelay);

		txCountAtPreviousInvocation = currentTxCount;
		delayAtPreviousInvocation = newDelay;
		return newDelay;
	}

	private long determineNewDelay(int currentTxCount) {
		if (txCountAtPreviousInvocation == -1) {
			LOG.debug("First request for timing delay so returning default of: {}ms", defaultDelayMillis);
			return defaultDelayMillis;
		}
		if (currentTxCount - txCountAtPreviousInvocation > threshold) {
			// had a lot of transactions since last time so back off a bit
			return delayAtPreviousInvocation + 100; //TODO: introduce a mechanism with which the delay adjustment can be discerned
		}
		// no significant increase so let's get aggressive!
		return delayAtPreviousInvocation - 100;

	}

}
