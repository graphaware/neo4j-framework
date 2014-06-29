package com.graphaware.runtime.schedule;

import com.graphaware.runtime.monitor.TransactionMonitor;

/**
 * {@link TimingStrategy} that causes tasks to be scheduled adaptively based on busy/quiet periods measured by the
 * provided {@link TransactionMonitor}.
 */
public class AdaptiveTimingStrategy implements TimingStrategy {

    private final TransactionMonitor transactionMonitor;

    public AdaptiveTimingStrategy(TransactionMonitor transactionMonitor) {
        this.transactionMonitor = transactionMonitor;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long nextDelay(long lastTaskDuration) {
        //todo implement this
        throw new UnsupportedOperationException("not yet implemented");
    }
}
