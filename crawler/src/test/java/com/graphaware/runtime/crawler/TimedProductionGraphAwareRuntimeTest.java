package com.graphaware.runtime.crawler;

import org.junit.After;
import org.junit.Before;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.test.TestGraphDatabaseFactory;

import com.graphaware.runtime.GraphAwareRuntimeTest;

public class TimedProductionGraphAwareRuntimeTest extends GraphAwareRuntimeTest {

	private TimedProductionGraphAwareRuntime runtime;
	private GraphDatabaseService databaseService;

	@Before
	public void setUp() {
		this.databaseService = new TestGraphDatabaseFactory().newImpermanentDatabase();
		this.runtime = new TimedProductionGraphAwareRuntime(this.databaseService);
	}

	@After
	public void shutDownDatabase() {
		this.databaseService.shutdown();
	}

}
