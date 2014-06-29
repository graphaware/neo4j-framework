package com.graphaware.runtime.metadata;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;

/**
 * {@link TimerDrivenModuleContext} with {@link Node} as the position representation and no extra data.
 */
public class NodeBasedContext implements TimerDrivenModuleContext<Node> {

    private final long nodeId;

    /**
     * Construct a new position.
     *
     * @param nodeId ID of the node.
     */
    public NodeBasedContext(long nodeId) {
        this.nodeId = nodeId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Node find(GraphDatabaseService database) {
        return database.getNodeById(nodeId);
    }
}
