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
     * Construct a new position.
     *
     * @param node the node. Must not be null.
     */
    public NodeBasedContext(Node node) {
        this(node.getId());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Node find(GraphDatabaseService database) {
        return database.getNodeById(nodeId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NodeBasedContext that = (NodeBasedContext) o;

        if (nodeId != that.nodeId) return false;

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return (int) (nodeId ^ (nodeId >>> 32));
    }
}
