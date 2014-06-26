package com.graphaware.crawler.pagerank;

import static org.junit.Assert.*;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.cypher.javacompat.ExecutionResult;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.test.TestGraphDatabaseFactory;

import com.graphaware.runtime.state.GraphPosition;
import com.graphaware.runtime.state.ModuleContext;

public class RandomWalkerPageRankModuleTest {

	private RandomWalkerPageRankModule pageRankModule;
	private GraphDatabaseService graphDatabaseService;
	private ExecutionEngine executionEngine;

	/** */
	@Before
	public void setUp() {
		this.graphDatabaseService = new TestGraphDatabaseFactory().newImpermanentDatabase();
		this.executionEngine = new ExecutionEngine(this.graphDatabaseService);

		this.pageRankModule = new RandomWalkerPageRankModule();
	}

	@Test
	public void shouldTolerateEmptyContextGivenIfNoPreviousStepsHaveBeenMade() {
		this.executionEngine.execute("CREATE (arbitraryNode)-[:RELATES_TO]->(otherNode) RETURN otherNode");

		ModuleContext<GraphPosition<Node>> nullPositionModuleContext = new ModuleContext<GraphPosition<Node>>() {
			@Override
			public GraphPosition<Node> getPosition() {
				return new GraphPosition<Node>() {
					@Override
					public Node find(GraphDatabaseService database) {
						return null;
					}};
			}
		};

		try (Transaction transaction = this.graphDatabaseService.beginTx()) {
			this.pageRankModule.doSomeWork(nullPositionModuleContext, this.graphDatabaseService);
		}
	}

	@Test
	public void shouldExecuteSingleStepTowardsConvergenceAndUpdateNodePropertiesAccordingly() {
		try (Transaction transaction = this.graphDatabaseService.beginTx()) {
			ExecutionResult executionResult = this.executionEngine.execute(
					"CREATE (p:Person{name:'Gary'})-[:KNOWS]->(q:Person{name:'Sheila'}) RETURN p, q");

			Map<String, Object> insertionResults = executionResult.iterator().next();
			Node startNode = (Node) insertionResults.get("p");
			ModuleContext<GraphPosition<Node>> lastContext = new PageRankModuleContext(startNode);

			Node expectedNextNode = (Node) insertionResults.get("q");

			ModuleContext<GraphPosition<Node>> newContext = this.pageRankModule.doSomeWork(lastContext, this.graphDatabaseService);
			assertNotNull("The new context shouldn't be null", newContext);
			Node nextNode = newContext.getPosition().find(this.graphDatabaseService);
			assertNotNull("The next node in the new context shouldn't be null", nextNode);
			assertEquals("The next node wasn't selected as expected", expectedNextNode, nextNode);
			assertEquals("The expected page rank property wasn't updated", 1, nextNode.getProperty("pageRankValue"));
		}
	}

}
