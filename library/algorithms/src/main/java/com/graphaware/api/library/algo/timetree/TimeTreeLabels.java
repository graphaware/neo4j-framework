package com.graphaware.api.library.algo.timetree;

import org.apache.log4j.Logger;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.helpers.collection.Iterables;

/**
 * {@link Label}s for {@link TimeTree}.
 */
public enum TimeTreeLabels implements Label {

    ROOT, YEAR, MONTH, DAY, HOUR, MINUTE, SECOND, MILLISECOND;

    private static final Logger LOG = Logger.getLogger(TimeTreeLabels.class);

    public Label getChild() {
        if (this.ordinal() >= values().length - 1) {
            LOG.error("Label " + this.toString() + " does not have children. This is a bug.");
            throw new IllegalArgumentException("Label " + this.toString() + " does not have children. This is a bug.");
        }

        return values()[this.ordinal() + 1];
    }

    public static Label getChild(Node node) {
        return TimeTreeLabels.valueOf(Iterables.first(node.getLabels()).name()).getChild();
    }
}
