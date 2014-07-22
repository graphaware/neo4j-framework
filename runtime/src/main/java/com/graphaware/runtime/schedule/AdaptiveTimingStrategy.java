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
	private final DelayAdjuster delayAdjuster;
	private final long defaultDelayMillis;
	private final int threshold;

	private int txCountAtPreviousInvocation;
	private long delayAtPreviousInvocation;

	/**
	 * Constructs a new {@link AdaptiveTimingStrategy} based on the given argument.
	 *
	 * @param database The {@link GraphDatabaseService} to monitor for activity and adapt accordingly.
	 * @param defaultDelayMillis The default initial timing delay in milliseconds.
	 * @param txDeltaThreshold A setting related to the number of transactions happening between invocations of
	 *        {@link #nextDelay(long)} that determines how many transactions between invocations is required to constitute a
	 *        busy period of activity.  The higher the number, the more throughput is needed to constitute busy.
	 */
	public AdaptiveTimingStrategy(GraphDatabaseService database, long defaultDelayMillis, int txDeltaThreshold) {
		this.transactionManager = ((GraphDatabaseAPI) database).getDependencyResolver().resolveDependency(TxManager.class);
		this.delayAdjuster = new ConstantDeltaDelayAdjuster(100);
		this.defaultDelayMillis = defaultDelayMillis;
		this.threshold = txDeltaThreshold;
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
		return delayAdjuster.determineNextDelay(delayAtPreviousInvocation, currentTxCount - txCountAtPreviousInvocation, threshold);
	}

}
