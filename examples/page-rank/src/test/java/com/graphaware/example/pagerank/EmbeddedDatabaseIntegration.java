package com.graphaware.example.pagerank;

import static org.junit.Assert.assertTrue;

import java.util.LinkedList;
import java.util.List;

import com.graphaware.example.pagerank.RandomWalkerPageRankModule;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.cypher.javacompat.ExecutionResult;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.ResourceIterable;
import org.neo4j.graphdb.Transaction;
import org.neo4j.helpers.Pair;
import org.neo4j.test.TestGraphDatabaseFactory;
import org.neo4j.test.impl.EphemeralFileSystemAbstraction;

@RunWith(JUnit4.class)
public class EmbeddedDatabaseIntegration/*Test*/ {

	private TestGraphDatabaseFactory testGraphDatabaseFactory;

	/**
	 * Pre-populates an ephemeral file system in the {@link TestGraphDatabaseFactory} so that when it's used in tests it creates
	 * and starts a database that already contains data.
	 * <p>
	 * The reason for this is to verify the behaviour of the module when the database is started but already contains data from
	 * a previous run.  Therefore, instead of all module tests starting on a blank database, we can start the test with a pre-
	 * defined graph and see how this affects module bootstrapping.  It ensures that the transactions used to insert the test
	 * data don't interfere with the module's life cycle.
	 * </p>
	 */
	@Before
	public void prePopulateDatabase() {
		this.testGraphDatabaseFactory = new TestGraphDatabaseFactory();
		this.testGraphDatabaseFactory.setFileSystem(new EphemeralFileSystemAbstraction());

		GraphDatabaseService interimDatabase = this.testGraphDatabaseFactory.newImpermanentDatabase();
		makeTestGraph(interimDatabase);
		interimDatabase.shutdown();
	}

	@Test
	public void shouldSuccessfullyInitialiseAndRunModuleWhenDatabaseIsStarted() throws InterruptedException {
		GraphDatabaseService database = this.testGraphDatabaseFactory.newImpermanentDatabaseBuilder()
				.loadPropertiesFromFile("src/test/resources/test-neo4j.properties")
				.newGraphDatabase();

		try (Transaction transaction = database.beginTx()) {
			ExecutionResult executionResult = new ExecutionEngine(database).execute(
					String.format("MATCH (p:Person) WHERE p.%s > 0 RETURN p", RandomWalkerPageRankModule.PAGE_RANK_PROPERTY_KEY));

			assertTrue("The page rank module didn't run on startup", executionResult.iterator().hasNext());
			transaction.success();
		}
		finally {
			database.shutdown();
		}
	}

	private static void makeTestGraph(GraphDatabaseService database) {
		@SuppressWarnings("serial")
		List<Pair<String, String>> folks = new LinkedList<Pair<String, String>>() {
			{
				add(Pair.of("Jeff", "Chris"));
				add(Pair.of("Jeff", "Paul"));
				add(Pair.of("Jeff", "Matthew"));
				add(Pair.of("Gary", "Alan"));
				add(Pair.of("Gary", "Robbie"));
				add(Pair.of("Gary", "Mark"));
				add(Pair.of("Gary", "Sue"));
				add(Pair.of("John", "Matthew"));
				add(Pair.of("John", "Sue"));
			}
		};

		// first, we need a graph to crawl
		try (Transaction transaction = database.beginTx()) {
			Label personLabel = DynamicLabel.label("Person");
			DynamicRelationshipType relationshipType = DynamicRelationshipType.withName("BOSS_OF");

			for (Pair<String, String> pairOfPeople : folks) {
				Node person = findOrCreateNode(personLabel, pairOfPeople.first(), database);
				Node colleague = findOrCreateNode(personLabel, pairOfPeople.other(), database);
				person.createRelationshipTo(colleague, relationshipType);
			}

			transaction.success();
		}
	}

	private static Node findOrCreateNode(Label label, String name, GraphDatabaseService database) {
		ResourceIterable<Node> existingNodes = database.findNodesByLabelAndProperty(label, "name", name);
		if (existingNodes.iterator().hasNext()) {
			return existingNodes.iterator().next();
		}
		Node newNode = database.createNode(label);
		newNode.setProperty("name", name);
		return newNode;
	}

}
