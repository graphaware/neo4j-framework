package com.graphaware.runtime.monitor;


/**
 * A component capable of monitoring ongoing transactions with the intent to detect busy/quiet periods etc.
 */
public interface TransactionMonitor {

	/**
	 * Called by the framework to inform this monitor that a transaction is currently happening.
	 */
	void acknowledgeTransaction();

	/**
	 * Retrieves the current rate of transaction {@link Throughput} according to this monitor.
	 *
	 * @return The current level of throughput, never <code>null</code>.
	 */
	Throughput getCurrentThroughput();

}
