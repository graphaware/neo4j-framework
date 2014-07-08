package com.graphaware.neo4j.example.pagerank;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Map;

import com.graphaware.example.pagerank.RandomWalkerPageRankModule;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.cypher.javacompat.ExecutionResult;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.test.TestGraphDatabaseFactory;

import com.graphaware.runtime.metadata.NodeBasedContext;

public class RandomWalkerPageRankModuleTest {

	private RandomWalkerPageRankModule pageRankModule;
	private GraphDatabaseService database;
	private ExecutionEngine executionEngine;

	/** */
	@Before
	public void setUp() {
		this.database = new TestGraphDatabaseFactory().newImpermanentDatabase();
		this.executionEngine = new ExecutionEngine(this.database);
		this.pageRankModule = new RandomWalkerPageRankModule("TEST");
	}

	@Test
	public void shouldTolerateEmptyContextGivenIfNoPreviousStepsHaveBeenMade() {
		this.executionEngine.execute("CREATE (arbitraryNode)-[:RELATES_TO]->(otherNode);");

		try (Transaction tx = this.database.beginTx()) {
			this.pageRankModule.doSomeWork(this.pageRankModule.createInitialContext(database), this.database);
		}
	}

	@Test
	public void shouldExecuteSingleStepTowardsConvergenceAndUpdateNodePropertiesAccordingly() {
        ExecutionResult executionResult = this.executionEngine.execute(
                "CREATE (p:Person{name:'Gary'})-[:KNOWS]->(q:Person{name:'Sheila'}) RETURN p, q");
        Map<String, Object> insertionResults = executionResult.iterator().next();

        try (Transaction tx = this.database.beginTx()) {
			Node startNode = (Node) insertionResults.get("p");
            NodeBasedContext lastContext = new NodeBasedContext(startNode);

			Node expectedNextNode = (Node) insertionResults.get("q");

			NodeBasedContext newContext = this.pageRankModule.doSomeWork(lastContext, this.database);
			assertNotNull("The new context shouldn't be null", newContext);
			Node nextNode = newContext.find(this.database);
			assertNotNull("The next node in the new context shouldn't be null", nextNode);
			assertEquals("The next node wasn't selected as expected", expectedNextNode, nextNode);
			assertEquals("The expected page rank property wasn't updated", 1, nextNode.getProperty("pageRankValue"));
		}
	}
}
