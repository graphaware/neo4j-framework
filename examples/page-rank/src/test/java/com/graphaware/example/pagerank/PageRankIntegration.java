package com.graphaware.example.pagerank;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.la4j.vector.Vector;
import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.cypher.javacompat.ExecutionResult;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.test.TestGraphDatabaseFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.graphaware.common.util.export.AdjacencyMatrix;
import com.graphaware.common.util.testing.PageRank;
import com.graphaware.generator.GraphGenerator;
import com.graphaware.generator.Neo4jGraphGenerator;
import com.graphaware.generator.config.BarabasiAlbertConfig;
import com.graphaware.generator.config.BasicGeneratorConfiguration;
import com.graphaware.generator.config.SimpleDegreeDistribution;
import com.graphaware.generator.node.SocialNetworkNodeCreator;
import com.graphaware.generator.relationship.BarabasiAlbertGraphRelationshipGenerator;
import com.graphaware.generator.relationship.SimpleGraphRelationshipGenerator;
import com.graphaware.generator.relationship.SocialNetworkRelationshipCreator;
import com.graphaware.runtime.GraphAwareRuntime;
import com.graphaware.runtime.GraphAwareRuntimeFactory;
import com.graphaware.runtime.metadata.NodeBasedContext;

/**
 * Integration tests for page rank module.  Note that it's not called "Test" in order to prevent Maven running it.
 */
@RunWith(JUnit4.class)
public class PageRankIntegration {

    private static final Logger LOG = LoggerFactory.getLogger(PageRankIntegration.class);

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

    @After
    public void tearDown() {
    	this.database.shutdown();
    }

    /** */
    @Test
    public void generateSocialNetworkAndWorkOutSomePageRankStatistics() {
        try (Transaction transaction = this.database.beginTx()) {
            LOG.info("Creating arbitrary social network database...");

            long time = System.currentTimeMillis();
            GraphGenerator graphGenerator = new Neo4jGraphGenerator(this.database);
            graphGenerator.generateGraph(new BasicGeneratorConfiguration(
                    9,
                    new SimpleGraphRelationshipGenerator(new SimpleDegreeDistribution(Arrays.asList(1, 1, 1, 2, 3, 4, 4, 3, 3))),
                    SocialNetworkNodeCreator.getInstance(),
                    SocialNetworkRelationshipCreator.getInstance()));

            time = System.currentTimeMillis() - time;

            LOG.info("Created database in " + time + "ms");

            final int totalSteps = 100;
            LOG.info("Performing " + totalSteps + " steps to convergence on page rank");

            NodeBasedContext context = this.pageRankModule.createInitialContext(database);
            time = System.currentTimeMillis();
            for (int i = 0; i < totalSteps; i++) {
                context = this.pageRankModule.doSomeWork(context, this.database);
            }
            time = System.currentTimeMillis() - time;
            LOG.info("All steps completed in " + time + "ms");

            ExecutionResult result = this.executionEngine.execute("MATCH (node) RETURN node ORDER BY node.pageRankValue DESC");
            for (Map<String, Object> map : result) {
                Node n = (Node) map.get("node");
                LOG.info(n.getProperty("name") + " has " + n.getDegree() + " relationships and a page rank value of: "
                        + n.getProperty(RandomWalkerPageRankModule.PAGE_RANK_PROPERTY_KEY, 0));
            }
        }
    }

	/**
	 * @throws InterruptedException If the test is interrupted when waiting for the page rank module to do its work
	 */
	@Test
	public void verifyRandomWalkerModuleCorrectlyGeneratesReasonablePageRankMeasurements() throws InterruptedException {
		// firstly, generate a graph
		final int numberOfNodes = 10;
		GraphGenerator graphGenerator = new Neo4jGraphGenerator(database);

		LOG.info("Generating Barabasi-Albert social network graph with {} nodes...", numberOfNodes);

		graphGenerator.generateGraph(new BasicGeneratorConfiguration(numberOfNodes, new BarabasiAlbertGraphRelationshipGenerator(
				new BarabasiAlbertConfig(numberOfNodes, 5)), SocialNetworkNodeCreator.getInstance(), SocialNetworkRelationshipCreator
				.getInstance()));

		LOG.info("Computing adjacency matrix for graph...");

		// secondly, compute the adjacency matrix for this graph
		AdjacencyMatrix adjacencyMatrix = new AdjacencyMatrix(database);

		try (Transaction tx = database.beginTx()) {
			LOG.info("Computing page rank based on adjacency matrix...");

			// thirdly, compute the page rank of this graph based on the adjacency matrix
			PageRank pageRank = new PageRank();
			Vector pageRankTopTen = pageRank.getPageRank(adjacencyMatrix, 0.5); // XXX: no idea what the damping constant should be

			LOG.info(pageRankTopTen.toString());
			LOG.info("Applying random graph walker module to page rank graph");

			// fourthly, run the rage rank module to compute the random walker's page rank
			GraphAwareRuntime runtime = GraphAwareRuntimeFactory.createRuntime(database);
			runtime.registerModule(pageRankModule);
			runtime.start();

			LOG.info("Waiting for module walker to do its work");
			TimeUnit.SECONDS.sleep(30);

			// finally, compare both page rank metrics and verify the module is producing what it should
			// XXX: I understand this is WIP, but why does this return a list if it's called get..Map?
			List<Node> indexMap = adjacencyMatrix.getIndexMap();
			LOG.info("Top of the index map is: {}", indexMap.get(0).getProperty("name"));
			for (int i=0 ; i<indexMap.size() ; i++) {
				Node node = indexMap.get(i);
				System.out.printf("%s\t%s\t%s\n", node.getProperty("name"),
						node.getProperty(RandomWalkerPageRankModule.PAGE_RANK_PROPERTY_KEY).toString(),
						pageRankTopTen.get(i));
			}

		}
	}

}
