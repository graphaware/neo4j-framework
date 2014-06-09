/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.graphaware.crawler.internal;

import com.graphaware.common.strategy.IncludeAllNodes;
import com.graphaware.common.strategy.InclusionStrategy;
import com.graphaware.crawler.api.ThingThatGetsCalledWhenWeFindSomething;

import org.apache.log4j.Logger;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.tooling.GlobalGraphOperations;

/**
 *
 * @author Vojtech Havlicek (Graphaware), Adam George (Graphaware)
 */
public class NeoRankCrawler implements PerpetualGraphCrawler {

    private static final Logger LOG = Logger.getLogger(SimpleRecursiveGraphCrawler.class);

    private InclusionStrategy<? super Node> nodeInclusionStrategy;
    private InclusionStrategy<? super Relationship> relInclusionStrategy;

    private ThingThatGetsCalledWhenWeFindSomething handler;
    private int normalization = 0;

    // Constants governing the PageRank process
    private static final double DIFFUSE = 0.9;
    private static final double HYPERJUMP = 0.1;

    // Mainly for testing purposes.
    private Node mostImportant;
    private int highestRank = 0;

    @Override
    public void setRelationshipInclusionStrategy(InclusionStrategy<? super Relationship> relInclusionStrategy) {
        this.relInclusionStrategy = relInclusionStrategy;
    }

    @Override
    public void setNodeInclusionStrategy(InclusionStrategy<? super Node> nodeInclusionStrategy) {
        this.nodeInclusionStrategy = nodeInclusionStrategy;
    }

    @Override
    public void addInclusionHandler(ThingThatGetsCalledWhenWeFindSomething inclusionHandler) {
        this.handler = inclusionHandler;
    }

    @Override
    public void startCrawling(GraphDatabaseService databaseService) {
        if (this.nodeInclusionStrategy == null) {
            this.nodeInclusionStrategy = IncludeAllNodes.getInstance();
        }

        try (Transaction transaction = databaseService.beginTx()) {
            Iterable<Node> nodes;

            nodes = GlobalGraphOperations.at(databaseService).getAllNodes();
            Node start = makeHyperJump(nodes);

            // TODO: actually make this crawl perpetually
            crawl(start, 9, 0, nodes);

            transaction.success(); // I reckon we want this trans'n to be read-only, but I don't know whether that's possible
        }
    }

    /**
     * Freshly implemented NeoRank
     * Adam: please could you suggest a reasonable structure to make it
     *       "perpetual"? I was thinking that we could fire a request to crawl
     *       through the API and wait for some completion event to be fired,
     *       but if you know of something better, I would be quite happy if
     *       you implemented it. Thanks!
     *
     * @param startNode
     * @param maxDepth
     * @param currentDepth
     * @param howDidIGetHere
     */
    private void crawl(Node current,
                      int maxDepth,
                      int currentDepth,
                      Iterable<Node> nodes) {
        if(random() < DIFFUSE)
            current = chooseNeighbourAtRandom(current, relInclusionStrategy);

            // If the crawler ended up at loneseome guy, just choose another at randoms
            //if(current == null)
              //  current = makeHyperJump(nodes);
        else
            current = makeHyperJump(nodes);

        int rank = (int) current.getProperty(PropertyKeys.NEORANK, 0);
        current.setProperty(PropertyKeys.NEORANK, rank+1);

        // Just to have something to return
        if (rank + 1 > highestRank) {
               mostImportant = current;
               highestRank = rank+1;
        }

        // increment normalization constant to allow to present results consistently
        normalization++;


        // Does it really have to be recursive?
        // also, howDidIGetHere doesn't have to be compulsory I think.
        if(currentDepth < maxDepth){
            currentDepth++;
            crawl(current, maxDepth, currentDepth, nodes);

        }


    }

    /**
     * Chooses a random neighbour of n. Assuming single component
     * graph, all nodes have at least degree 1.
     *
     * Uses  reservoir sampling
     * http://en.wikipedia.org/wiki/Reservoir_sampling
     * to get a random guy from the iterable
     *
     * LCG used so far, I recommend to use Mersenne Twister/RANLUX or similar
     * later on.
     * http://stackoverflow.com/questions/453479/how-good-is-java-util-random
     *
     * @param n
     * @return
     */
    private Node chooseNeighbourAtRandom(Node n, InclusionStrategy<? super Relationship> strategy)
    {
        System.out.println("[VOJTA] Look at this dude");
        Relationship edgeChoice = null; // Not entirely kosher, correct later
        Iterable<Relationship> relationships = n.getRelationships(Direction.BOTH); // Only directed rels later

        double max =  .0;
        for (Relationship temp : relationships) {
            System.out.println("[VOJTA] Dude has some friends!");
            if (!strategy.include(temp))
                continue; // Exclude nodes not included in the strategy

            double rnd = random();
            if (rnd > max){
                max = rnd;
                edgeChoice = temp;
            }
        }

        if (edgeChoice == null){
            System.out.println("[VOJTA] Dude has no friends!");
            return null; // Is exception more elegant here?
        }


        return edgeChoice.getEndNode();
    }

    /**
     * It is necessary to make a hyperjump sometimes to
     * reach weakly connected (or entirely unconnected)
     * components of the network.
     *
     * The method selects a node at random.
     *
     * Wouldn't it be better to choose an ID rather then
     * iterate over?
     *
     * @return
     */
    private Node makeHyperJump(Iterable<Node> nodes)
    {
        Node target = null; // again, could be formulated in a better way.

        double max = .0;
        for (Node temp : nodes) {
            double rnd = random();
            if (rnd > max)
            {
                max = rnd;
                target = temp;
            }
        }

        return target;
    }

    /**
     * Returns null if no rank has been done yet.
     * Not very optimal.
     * @return
     */
    private Node getHighestRanked()
    {
        return mostImportant;
    }

    /**
     * Substitute method to allow for change of random() implementation
     * later on.
     *
     * @return
     */
    private double random()
    {
        return Math.random();
    }

    /**
     * Log on debug
     * @param message
     * @param currentDepth
     */
    private static void debug(String message, int currentDepth) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < currentDepth; i++) {
            sb.append('*');
        }
        sb.append(' ').append(message);
        LOG.debug(sb.toString());
    }

}
