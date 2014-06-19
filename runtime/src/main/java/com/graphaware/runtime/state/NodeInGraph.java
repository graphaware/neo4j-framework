package com.graphaware.runtime.state;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;

/**
 *
 */
public class NodeInGraph implements GraphPosition<Node> {

    private final long nodeId;

    public NodeInGraph(long nodeId) {
        this.nodeId = nodeId;
    }

    public long getNodeId() {
        return nodeId;
    }

    @Override
    public Node find(GraphDatabaseService database) {
        return database.getNodeById(nodeId);
    }
}
