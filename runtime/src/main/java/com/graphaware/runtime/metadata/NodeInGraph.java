package com.graphaware.runtime.metadata;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;

/**
 * {@link GraphPosition} which is a single {@link Node}.
 */
public class NodeInGraph implements GraphPosition<Node> {

    private final long nodeId;

    /**
     * Construct a new position.
     *
     * @param nodeId ID of the node.
     */
    public NodeInGraph(long nodeId) {
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
