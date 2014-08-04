package com.graphaware.common.util.testing;

import com.graphaware.common.util.Pair;
import org.neo4j.graphdb.Node;

import java.util.ArrayList;
import java.util.List;

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

    /**
     * Converts RankNodePairs to ArrayList of nodes
     * @param rankNodePairs
     * @return
     */
    public static ArrayList<Node> convertToRankedNodeList(List<RankNodePair> rankNodePairs) {
        ArrayList<Node> toReturn = new ArrayList<>();

        // I am sure there is a plenty of room for improvement here ;)
        for (RankNodePair indexNodePair : rankNodePairs) {
            toReturn.add(indexNodePair.node());
        }

        return toReturn;
    }
}
