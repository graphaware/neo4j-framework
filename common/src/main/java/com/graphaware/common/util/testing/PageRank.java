package com.graphaware.common.util.testing;

import com.graphaware.common.util.export.AdjacencyMatrix;

import org.la4j.LinearAlgebra;
import org.la4j.factory.Basic1DFactory;
import org.la4j.factory.CRSFactory;
import org.la4j.factory.Factory;
import org.la4j.matrix.Matrix;
import org.la4j.vector.Vector;

/**
 * EXPERIEMNTAL
 *
 * The following is an testing implementation of PageRank. As PR is a global operation which requires a full
 * adjacency matrix this class is meant to be used for TESTING PURPOSES only and mainly as a benchmark against
 * the WEAKER NeoRank.
 */
public class PageRank {



    /**
     * Returns a 10 nodes of the highest PageRank from the network.
     *
     * WARNING: The stored graph must be single-component (the
     *          matrix must be irreducible for the algorithm to
     *          succeed)
     *
     * @param graphRepresentation
     * @return
     */
    public Vector getPageRank(AdjacencyMatrix graphRepresentation, double damping)
    {
        if (damping > 1.0 || damping < 0) {
            try {
                throw new Exception("Wrong arguments passed on input");  // TODO: consult & improve
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        Factory vectorFactory = new Basic1DFactory();
        Factory matrixFactory = new CRSFactory();

        int size;

        // Calculates the pageRank. The convergence to PageRank is guaranteed
        // by picking the vector which converges to Perron Vector by Perron-
        // -Frobenius theorem

        Matrix transitionMatrix = graphRepresentation.getTransitionMatrix();
        size = transitionMatrix.rows();

        Matrix identityMatrix = matrixFactory.createIdentityMatrix(size);
        Vector testVector = vectorFactory.createConstantVector(size, 1);
        Matrix inverse = identityMatrix.add(transitionMatrix.multiply(-damping)).withInverter(LinearAlgebra.InverterFactory.SMART).inverse();
        Matrix pageRankOperator = identityMatrix.multiply(1-damping).multiply(inverse);
        Vector pageRank = pageRankOperator.multiply(testVector);

        return pageRank;
    }
}
