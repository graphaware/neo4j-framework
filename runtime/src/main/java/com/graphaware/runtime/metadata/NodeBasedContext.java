package com.graphaware.runtime.metadata;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;

/**
 * {@link BaseTimerDrivenModuleContext} with {@link Node} as the position representation and no extra data.
 */
public class NodeBasedContext extends BaseTimerDrivenModuleContext<Node> {

    private final long nodeId;

    /**
     * Construct a new context.
     *
     * @param nodeId ID of the node.
     */
    public NodeBasedContext(long nodeId) {
        this.nodeId = nodeId;
    }

    /**
     * Construct a new context.
     *
     * @param node the node. Must not be null.
     */
    public NodeBasedContext(Node node) {
        this(node.getId());
    }

    /**
     * Construct a new context.
     *
     * @param nodeId           ID of the node.
     * @param earliestNextCall time in ms since 1/1/1970 when the module wants to be called next at the earliest.
     */
    public NodeBasedContext(long nodeId, long earliestNextCall) {
        super(earliestNextCall);
        this.nodeId = nodeId;
    }

    /**
     * Construct a new context.
     *
     * @param node             the node. Must not be null.
     * @param earliestNextCall time in ms since 1/1/1970 when the module wants to be called next at the earliest.
     */
    public NodeBasedContext(Node node, long earliestNextCall) {
        this(node.getId(), earliestNextCall);
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
        if (!super.equals(o)) return false;

        NodeBasedContext that = (NodeBasedContext) o;

        if (nodeId != that.nodeId) return false;

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (int) (nodeId ^ (nodeId >>> 32));
        return result;
    }
}
