package com.graphaware.runtime.crawler;

import static org.mockito.Mockito.only;
import static org.mockito.Mockito.stub;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.test.TestGraphDatabaseFactory;

public class TimedProductionGraphAwareRuntimeTest {

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

	@Test(expected = IllegalStateException.class)
	public void throwsIllegalStateExceptionUponAttemptToSignifyRuntimeHasStartedIfDatabaseIsNotAvailable() {
		GraphDatabaseService mockDatabase = Mockito.mock(GraphDatabaseService.class);
        this.runtime = new TimedProductionGraphAwareRuntime(mockDatabase);

        stub(mockDatabase.isAvailable(-1)).toReturn(false);

        this.runtime.runtimeStarted();

        verify(mockDatabase, only()).isAvailable(0);
        verifyNoMoreInteractions(mockDatabase);
	}

}
