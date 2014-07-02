package com.graphaware.neo4j.example.pagerank;

import com.graphaware.runtime.metadata.NodeBasedContext;
import com.graphaware.runtime.module.BaseRuntimeModule;
import com.graphaware.runtime.module.TimerDrivenModule;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.NotFoundException;
import org.neo4j.graphdb.Relationship;

public class RandomWalkerPageRankModule extends BaseRuntimeModule implements TimerDrivenModule<NodeBasedContext> {

	/** The name of the property key that is modified on visited nodes by this module. */
	public static final String PAGE_RANK_PROPERTY_KEY = "pageRankValue";

	private RandomNodeSelector randomNodeSelector;
	private RelationshipChooser relationshipChooser;

    public RandomWalkerPageRankModule(String moduleId) {
        super(moduleId);
        this.randomNodeSelector = new HyperJumpRandomNodeSelector();
        // TODO: add relationship type and direction filtering based on module configuration
        this.relationshipChooser = new RandomRelationshipChooser();
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
            currentNode = this.randomNodeSelector.selectRandomNode(database);
        }

        Node nextNode = determineNextNode(currentNode, database);

        int pageRankValue = (int) nextNode.getProperty(PAGE_RANK_PROPERTY_KEY, 0);
        nextNode.setProperty(PAGE_RANK_PROPERTY_KEY, pageRankValue + 1);

        return new NodeBasedContext(nextNode);
    }

	private Node determineNextNode(Node currentNode, GraphDatabaseService graphDatabaseService) {
		Relationship chosenRelationship = this.relationshipChooser.chooseRelationship(currentNode);
		if (chosenRelationship != null) {
			return chosenRelationship.getOtherNode(currentNode);
		}
		return this.randomNodeSelector.selectRandomNode(graphDatabaseService);
	}

}
