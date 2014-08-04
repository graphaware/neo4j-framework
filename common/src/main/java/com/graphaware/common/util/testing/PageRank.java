package com.graphaware.common.util.testing;

import com.graphaware.common.util.export.NetworkMatrix;
import org.la4j.LinearAlgebra;
import org.la4j.factory.Basic1DFactory;
import org.la4j.factory.CRSFactory;
import org.la4j.factory.Factory;
import org.la4j.matrix.Matrix;
import org.la4j.vector.Vector;
import org.neo4j.graphdb.Node;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.sort;

/**
 * EXPERIMENTAL
 *
 * The following is an testing implementation of PageRank. As PR is a global operation which requires a full
 * adjacency matrix this class is meant to be used for TESTING PURPOSES only and mainly as a benchmark against
 * the WEAKER NeoRank.
 */
public class PageRank {

    /**
     * Returns PageRank of the nodes from the network.
     *
     * WARNING: The stored graph must be single-component (the
     *          matrix must be irreducible for the algorithm to
     *          succeed)
     *
     * @param transitionMatrix
     * @return
     */
    public Vector getPageRankVector(NetworkMatrix transitionMatrix, double damping) {
        try {
            validateArguments(transitionMatrix, damping);
        }catch (Exception e){
            System.err.println(e.toString());
            return null;
        }

        Factory vectorFactory = new Basic1DFactory();
        Factory matrixFactory = new CRSFactory();

        int size;

        // Calculates the pageRank. The convergence to PageRank is guaranteed
        // by picking the vector which converges to Perron Vector by Perron-
        // -Frobenius theorem

        size = transitionMatrix.getMatrix().rows();

        Matrix identityMatrix = matrixFactory.createIdentityMatrix(size);
        Vector testVector = vectorFactory.createConstantVector(size, 1);
        Matrix inverse = identityMatrix.add(transitionMatrix.getMatrix().multiply(-damping)).withInverter(LinearAlgebra.InverterFactory.SMART).inverse();
        Matrix pageRankOperator = identityMatrix.multiply(1-damping).multiply(inverse);
        Vector pageRank = pageRankOperator.multiply(testVector);

        return pageRank;
    }

    /**
     * Returns a pageRanked array list of
     * nodes contained in the network.
     * @return
     */
    public ArrayList<Node> getPageRank(NetworkMatrix transitionMatrix, double damping) {

        ArrayList<RankNodePair> rankNodePairs = getPageRankPairs(transitionMatrix, damping);
        return RankNodePair.convertToRankedNodeList(rankNodePairs);
    }



    /**
     * Returns (rank, node) pairs
     * sorted in descending order by pageRank
     * @param transitionMatrix
     * @param damping
     * @return
     */
    public ArrayList<RankNodePair> getPageRankPairs(NetworkMatrix transitionMatrix, double damping) {
        Vector pageRankVector = getPageRankVector(transitionMatrix, damping);
        ArrayList<Node> nodeList = transitionMatrix.getNodeList();
        ArrayList<RankNodePair> rankNodePairs = new ArrayList<>(pageRankVector.length());

        for (int i = 0; i < pageRankVector.length(); ++i)
            rankNodePairs.add(new RankNodePair(pageRankVector.get(i), nodeList.get(i))); // prepare the pairs

        sort(rankNodePairs, new RankNodePairComparator());

        return  rankNodePairs;
    }

    /**
     * Fires an exception if the argument set is invalid.
     * WARNING: throws exceptions
     *
     * TODO: Is the way the exceptions are fired optimal?
     * @param transitionMatrix
     * @param damping
     */
    private void validateArguments(NetworkMatrix transitionMatrix, double damping) throws Exception
    {
        if (damping > 1.0 || damping < 0 || transitionMatrix.equals(null))
              throw new Exception("Wrong arguments passed on input");  // TODO: consult & improve

    }

}
