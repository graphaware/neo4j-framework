package com.graphaware.runtime.crawler;

import org.neo4j.graphdb.GraphDatabaseService;

import com.graphaware.crawler.algo.CrawlerAlgorithm;
import com.graphaware.runtime.ProductionGraphAwareRuntime;
import com.graphaware.runtime.timer.TimingStrategy;

/**
 * Specialisation of {@link ProductionGraphAwareRuntime} that perpetually crawls the graph based on a {@link TimingStrategy} and
 * informs registered modules about its progress.
 */
public class TimedProductionGraphAwareRuntime extends ProductionGraphAwareRuntime {

	private CrawlerAlgorithm crawlerAlgorithm;
	private TimingStrategy timingStrategy;

	/**
	 * Constructs a new {@link TimedProductionGraphAwareRuntime} that acts upon the given database.
	 *
	 * @param database The {@link GraphDatabaseService} upon which to act
	 * @see ProductionGraphAwareRuntime#ProductionGraphAwareRuntime(GraphDatabaseService)
	 */
	public TimedProductionGraphAwareRuntime(GraphDatabaseService database) {
		super(database);
	}

	// work in progress...

}
