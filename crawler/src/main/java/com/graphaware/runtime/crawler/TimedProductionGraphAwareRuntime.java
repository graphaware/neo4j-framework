package com.graphaware.runtime.crawler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.neo4j.graphdb.GraphDatabaseService;

import com.graphaware.crawler.algo.CrawlerAlgorithm;
import com.graphaware.crawler.algo.DefaultConfiguration;
import com.graphaware.runtime.ProductionGraphAwareRuntime;
import com.graphaware.runtime.timer.FixedDelayTimingStrategy;
import com.graphaware.runtime.timer.TimingStrategy;

/**
 * Specialisation of {@link ProductionGraphAwareRuntime} that perpetually crawls the graph based on a {@link TimingStrategy} and
 * informs registered modules about its progress.
 */
public class TimedProductionGraphAwareRuntime extends ProductionGraphAwareRuntime {

	private CrawlerAlgorithm crawlerAlgorithm;
	private TimingStrategy timingStrategy;
	private ExecutorService executor;

	/**
	 * Constructs a new {@link TimedProductionGraphAwareRuntime} that acts upon the given database.
	 *
	 * @param database The {@link GraphDatabaseService} upon which to act
	 * @see ProductionGraphAwareRuntime#ProductionGraphAwareRuntime(GraphDatabaseService)
	 */
	public TimedProductionGraphAwareRuntime(GraphDatabaseService database) {
		super(database);
		this.timingStrategy = new FixedDelayTimingStrategy(5000);
		this.crawlerAlgorithm = new CrawlerAlgorithm(new DefaultConfiguration(), null) {
			@Override
			public void iterate() {
				// I'm not sure why nodes get passed to this object's constructor.
				// My understanding is that this'll run one iteration towards the subject of convergence
			}
		};
	}

	// TODO: I can't see an obvious method to override so I need to decide how to tap into the superclass to call this method
	protected void runtimeStarted() {
		if (runtimeHasNotYetInitialised()) {
			throw new IllegalStateException("Tried to start the timed runtime without a database available!");
		}

		/*
		 * just like the ProductionGraphAwareRuntime, this does a "check prerequisites" and then does "notify modules"
		 */

		this.executor = Executors.newCachedThreadPool();
		this.executor.execute(new Worker());
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This implementation ensures that any background threads are killed and resources freed.
	 * </p>
	 */
	@Override
	protected void shutdownRuntime() {
		if (this.executor != null) {
			this.executor.shutdown();
		}
	}

	/**
	 * Responsible for implementing the pause given by the timing strategy and invoking the algorithm where appropriate.
	 */
	class Worker implements Runnable {

		@Override
		public void run() {
			try {
				System.out.println("working...");
				Thread.sleep(timingStrategy.nextDelay(-1));
			} catch (InterruptedException e) {
				e.printStackTrace(System.err);
			}

			// does this run until convergence or is it just one iteration?
			// do we want to have a separate throttling thing to control between iterations?
			crawlerAlgorithm.iterate();
		}

	}

}
