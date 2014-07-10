package com.graphaware.example.pagerank;

import static org.junit.Assert.*;

import java.util.Map;

import com.graphaware.common.strategy.IncludeAllRelationships;
import com.graphaware.common.strategy.InclusionStrategy;
import com.graphaware.common.strategy.NodeInclusionStrategy;
import com.graphaware.common.strategy.RelationshipInclusionStrategy;
import com.graphaware.example.pagerank.RandomWalkerPageRankModule;

import org.junit.Before;
import org.junit.Test;
import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.cypher.javacompat.ExecutionResult;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
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

	@Test
	public void shouldHonourInclusionStrategiesForNodesAndRelationships() {
        // recreate page rank module configured to include only certain types of nodes and relationships
        InclusionStrategy<? super Node> carNodesOnly = new NodeInclusionStrategy() {
			@Override
			public boolean include(Node object) {
				return object.hasLabel(DynamicLabel.label("Car"));
			}
		};
		InclusionStrategy<? super Relationship> ownsRelationshipsOnly = new RelationshipInclusionStrategy() {
			@Override
			public boolean include(Relationship object) {
				return object.isType(DynamicRelationshipType.withName("OWNS"));
			}
		};
		this.pageRankModule = new RandomWalkerPageRankModule("TEST2", new PageRankModuleConfiguration(carNodesOnly, ownsRelationshipsOnly));

		// set up test data and run test
		ExecutionResult executionResult = this.executionEngine.execute(
				"CREATE (p:Person{name:'Sanjiv'})-[:KNOWS]->(:Person{name:'Lakshmipathy'}),"
						+ " (p)-[:KNOWS]->(:Person{name:'Rajani'}), "
						+ " (p)-[:OWNS]->(:Laptop{manufacturer:'Dell'}), "
						+ " (p)-[:OWNS]->(:MobilePhone{manufacturer:'Nokia'}), "
						+ " (p)-[:OWNS]->(:Car{manufacturer:'Vauxhall'}), "
						+ " (p)-[:OWNS]->(:Tablet{manufacturer:'Samsung'}) "
						+ "RETURN p");

		Map<String, Object> insertionResults = executionResult.iterator().next();

        try (Transaction tx = this.database.beginTx()) {
        	Node person = (Node) insertionResults.get("p");

        	NodeBasedContext newContext = this.pageRankModule.doSomeWork(new NodeBasedContext(person), this.database);
			assertNotNull("The new context shouldn't be null", newContext);
			Node nextNode = newContext.find(this.database);
			assertNotNull("The next node in the new context shouldn't be null", nextNode);
			assertEquals("The wrong next node was selected", "Car", nextNode.getLabels().iterator().next().name());
        }
	}

	@Test
	public void shouldChooseLegitimateRandomStartNodeInAccordanceWithInclusionStrategy() {
		// recreate page rank module configured to include only certain types of nodes
		InclusionStrategy<? super Node> veganInclusionStrategy = new NodeInclusionStrategy() {
			@Override
			public boolean include(Node object) {
				return object.hasLabel(DynamicLabel.label("Vegan"));
			}
		};
		this.pageRankModule = new RandomWalkerPageRankModule("TEST3",
				new PageRankModuleConfiguration(veganInclusionStrategy, IncludeAllRelationships.getInstance()));

		this.executionEngine.execute("CREATE (:Meat{name:'Chicken'}), (:Meat{name:'Mutton'}), (:Vegan{name:'Potato'}), "
						+ "(:Vegetarian{name:'Milk'}), (:Vegetarian{name:'Cheese'}), (:Meat{name:'Pork'})");

        try (Transaction tx = this.database.beginTx()) {
        	NodeBasedContext initialContext = this.pageRankModule.createInitialContext(this.database);
			assertNotNull("The initial context shouldn't be null", initialContext);
			Node startNode = initialContext.find(this.database);
			assertEquals("The wrong start node was selected", "Potato", startNode.getProperty("name"));
        }
	}

}
