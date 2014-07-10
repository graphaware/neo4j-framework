package com.graphaware.example.pagerank;

import com.graphaware.runtime.metadata.NodeBasedContext;
import com.graphaware.runtime.module.BaseRuntimeModule;
import com.graphaware.runtime.module.TimerDrivenModule;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.NotFoundException;
import org.neo4j.graphdb.Relationship;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link TimerDrivenModule} that perpetually walks the graph by randomly following relationships and increments
 * a node property called <code>pageRankValue</code> as it goes.  Eventually this will converge on a reasonable measure
 * of the degree to which notes are interconnected, in that the most "popular" nodes will have the highest rank value.
 */
public class RandomWalkerPageRankModule extends BaseRuntimeModule implements TimerDrivenModule<NodeBasedContext> {

	private static final Logger LOG = LoggerFactory.getLogger(RandomWalkerPageRankModule.class);

	/** The name of the property that is maintained on visited nodes by this module. */
	public static final String PAGE_RANK_PROPERTY_KEY = "pageRankValue";

	private RandomNodeSelector randomNodeSelector;
	private RelationshipChooser relationshipChooser;

    /**
     * Constructs a new {@link RandomWalkerPageRankModule} with the given ID using the default module configuration.
     *
     * @param moduleId The unique identifier for this module instance in the GraphAware runtime
     */
    public RandomWalkerPageRankModule(String moduleId) {
    	this(moduleId, PageRankModuleConfiguration.defaultConfiguration());
    }

    /**
     * Constructs a new {@link RandomWalkerPageRankModule} with the given ID and configuration settings.
     *
     * @param moduleId The unique identifier for this module instance in the GraphAware runtime
     * @param moduleConfig The {@link PageRankModuleConfiguration} to use
     */
	public RandomWalkerPageRankModule(String moduleId, PageRankModuleConfiguration moduleConfig) {
		super(moduleId);
		this.randomNodeSelector = new HyperJumpRandomNodeSelector(moduleConfig.getNodeInclusionStrategy());
		this.relationshipChooser = new RandomRelationshipChooser(moduleConfig.getRelationshipInclusionStrategy(),
				moduleConfig.getNodeInclusionStrategy());
	}

    /**
     * {@inheritDoc}
     */
	@Override
	public void shutdown() {
		//nothing needed for now
	}

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeBasedContext createInitialContext(GraphDatabaseService database) {
    	LOG.info("Starting page rank graph walker from random start node...");
        return new NodeBasedContext(randomNodeSelector.selectRandomNode(database).getId());
    }

    /**
     * {@inheritDoc}
     */
    @Override
	public NodeBasedContext doSomeWork(NodeBasedContext lastContext, GraphDatabaseService database) {
        Node currentNode;
        try {
            currentNode = lastContext.find(database);
        } catch (NotFoundException e) {
        	LOG.warn("Node referenced in last context with ID: {} was not found in the database.  Selecting new node at random to continue.");
            currentNode = this.randomNodeSelector.selectRandomNode(database);
        }

        Node nextNode = determineNextNode(currentNode, database);

        int pageRankValue = (int) nextNode.getProperty(PAGE_RANK_PROPERTY_KEY, 0);
        nextNode.setProperty(PAGE_RANK_PROPERTY_KEY, pageRankValue + 1);

        return new NodeBasedContext(nextNode);
    }

	private Node determineNextNode(Node currentNode, GraphDatabaseService database) {
		Relationship chosenRelationship = this.relationshipChooser.chooseRelationship(currentNode);
		if (chosenRelationship != null) {
			return chosenRelationship.getOtherNode(currentNode);
		}
		return this.randomNodeSelector.selectRandomNode(database);
	}

}
