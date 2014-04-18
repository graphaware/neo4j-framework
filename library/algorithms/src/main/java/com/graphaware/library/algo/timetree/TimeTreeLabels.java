package com.graphaware.library.algo.timetree;

import org.apache.log4j.Logger;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;

/**
 * {@link Label}s for {@link TimeTree}.
 */
public enum TimeTreeLabels implements Label {

    TimeTreeRoot, Year, Month, Day, Hour, Minute, Second, Millisecond;

    private static final Logger LOG = Logger.getLogger(TimeTreeLabels.class);

    /**
     * Get the label representing a resolution one level lower than this label.
     *
     * @return child label.
     * @throws IllegalStateException if there is no child label.
     */
    public Label getChild() {
        if (this.ordinal() >= values().length - 1) {
            LOG.error("Label " + this.toString() + " does not have children. This is a bug.");
            throw new IllegalArgumentException("Label " + this.toString() + " does not have children. This is a bug.");
        }

        return values()[this.ordinal() + 1];
    }

    /**
     * Get the label representing a resolution one level lower than represented by the given node. The node must be
     * from a GraphAware TimeTree
     *
     * @param node to find child label for.
     * @return child label.
     * @throws IllegalArgumentException in case the given node is not from GraphAware TimeTree.
     */
    public static Label getChild(Node node) {
        for (Label label : node.getLabels()) {
            try {
                return TimeTreeLabels.valueOf(label.name()).getChild();
            } catch (IllegalArgumentException e) {
                //ok
            }
        }

        throw new IllegalArgumentException("Node " + node.toString() + " is not from the GraphAware TimeTree. This is a bug.");
    }
}
