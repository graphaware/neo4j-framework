package com.graphaware.common.util.testing;

import com.graphaware.common.util.Pair;
import org.neo4j.graphdb.Node;

/**
 * IndexNode pair, used for sorting the results of Page Rank algorithm
 */
public class RankNodePair extends Pair {

    /**
     * Construct a new pair.
     *
     * @param  rank
     * @param node
     */
    public RankNodePair(double rank, Node node) {
        super(rank, node);
    }

    /**
     * Return rank stored in the INP
     * TODO: Optimise?
     * @return
     */
    public double rank() {
        return (double) first();
    }

    /**
     * Returns node stored in the INP
     * TODO: Optimise, clean up?
     */
    public Node node() {
        return (Node) second();
    }
}
