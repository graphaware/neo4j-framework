package com.graphaware.crawler.integration;

import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.ResourceIterable;
import org.neo4j.graphdb.Transaction;
import org.neo4j.helpers.Pair;
import org.neo4j.test.TestGraphDatabaseFactory;

import com.graphaware.crawler.CrawlerRuntimeModule;
import com.graphaware.runtime.ProductionGraphAwareRuntime;

/**
 * This is a just playground, in truth. It won't be here for very long.
 */
public class ArbitraryTest {

	private GraphDatabaseService database;

	@Before
	public void setUp() {
		this.database = new TestGraphDatabaseFactory().newImpermanentDatabase();
	}

	@Test
	public void shouldBeAbleToCrawlAnArbitraryGraph() {
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
		try (Transaction transaction = this.database.beginTx()) {
			Label personLabel = DynamicLabel.label("Person");
			DynamicRelationshipType relationshipType = DynamicRelationshipType.withName("BOSS_OF");

			for (Pair<String, String> pairOfPeople : folks) {
				Node person = findOrCreateNode(personLabel, pairOfPeople.first());
				Node colleague = findOrCreateNode(personLabel, pairOfPeople.other());
				person.createRelationshipTo(colleague, relationshipType);
			}

			transaction.success();
		}

		// so, now we have a graph, we can set up a crawler to find big bosses (i.e., who's got no incoming BOSS_OF relationship)
		/*
		 Now, how's this going to work, then?

		 Traversal strategy, for now, could be to pick some random nodes.
		 What-to-do-with-each-node strategy will be the code provided here, probably as a callback

		 */

		ProductionGraphAwareRuntime graphAwareRuntime = new ProductionGraphAwareRuntime(this.database);
		this.database.registerKernelEventHandler(graphAwareRuntime);
		graphAwareRuntime.registerModule(new CrawlerRuntimeModule("TestingCrawler"));
		graphAwareRuntime.start();
	}

	// TODO: this should be part of GraphUnit, I reckon
	private Node findOrCreateNode(Label label, String name) {
		ResourceIterable<Node> existingNodes = this.database.findNodesByLabelAndProperty(label, "name", name);
		if (existingNodes.iterator().hasNext()) {
			return existingNodes.iterator().next();
		}
		Node newNode = this.database.createNode(label);
		newNode.setProperty("name", name);
		return newNode;
	}

	/*
MERGE (p:Person{name:'Jeff'}) MERGE (q:Person{name:'Chris'}) MERGE (p)-[:WORKS_WITH]->(q) RETURN p,q
MERGE (p:Person{name:'Jeff'}) MERGE (q:Person{name:'Paul'}) MERGE (p)-[:WORKS_WITH]->(q) RETURN p,q
MERGE (p:Person{name:'Jeff'}) MERGE (q:Person{name:'Matthew'}) MERGE (p)-[:WORKS_WITH]->(q) RETURN p,q
MERGE (p:Person{name:'Gary'}) MERGE (q:Person{name:'Alan'}) MERGE (p)-[:WORKS_WITH]->(q) RETURN p,q
MERGE (p:Person{name:'Gary'}) MERGE (q:Person{name:'Robbie'}) MERGE (p)-[:WORKS_WITH]->(q) RETURN p,q
MERGE (p:Person{name:'Gary'}) MERGE (q:Person{name:'Mark'}) MERGE (p)-[:WORKS_WITH]->(q) RETURN p,q
MERGE (p:Person{name:'Gary'}) MERGE (q:Person{name:'Sue'}) MERGE (p)-[:WORKS_WITH]->(q) RETURN p,q
MERGE (p:Person{name:'John'}) MERGE (q:Person{name:'Sue'}) MERGE (p)-[:WORKS_WITH]->(q) RETURN p,q
MERGE (p:Person{name:'John'}) MERGE (q:Person{name:'Matthew'}) MERGE (p)-[:WORKS_WITH]->(q) RETURN p,q
	 */
}
