package com.graphaware.common.util.export;

import org.la4j.factory.CRSFactory;
import org.la4j.factory.Factory;
import org.la4j.matrix.Matrix;
import org.la4j.matrix.sparse.CRSMatrix;
import org.neo4j.graphdb.*;
import org.neo4j.tooling.GlobalGraphOperations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * EXPERIMENTAL - WORK IN PROGRESS
 *
 * Exports the full adjacency matrix.
 *
 * WARNING: This is for testing purposes only and should not
 *          be executed on a regular basis.
 *
 * TODO: Allow for directed edges?
 * TODO: Return the index <-> node map as well. Possibly return a pair, or some custom class?
 *       UPDATE: this is done by a simple workaround now not to waste time on this too much,
 *               but it could be definitelly more cleaner.
 * 
 */

public class AdjacencyMatrix {
    private final GraphDatabaseService database;
    private ArrayList<Node> indexMap; // matrix index <-> node pairs

    public AdjacencyMatrix(GraphDatabaseService database) {
        this.database = database;
    }

    /**
     * Returns an adjacency matrix in a raw ArrayList<ArrayList<Integer>> form.
     * TODO: Allow for weights & inclusion strategies?
     * @return la4j matrix sparse matrix
     */
    public Matrix getAdjacencyMatrix() {
        Iterable<Node> nodes = GlobalGraphOperations.at(database).getAllNodes();
        indexMap = new ArrayList<>();
        HashMap<Node, Integer> matrixIndices = new HashMap<>();

        int length = 0;
        int discoveredIndex = 0;
        int originIndex;
        int targetIndex;

        // TODO: avoid iterating over all nodes to obtain the length, or find a dynamic sparse matrix storage
        for (Node ignored : nodes)
            length ++;

        CRSMatrix adjacency = new CRSMatrix(length, length);

        for (Node origin : nodes) {
            // if node is not yet present in the matrix, index it

            if(!matrixIndices.containsKey(origin)) {
                matrixIndices.put(origin, discoveredIndex);
                indexMap.add(origin);
                originIndex = discoveredIndex;
                discoveredIndex ++;
            }else
                originIndex = matrixIndices.get(origin);


            Iterable<Relationship> relationships = origin.getRelationships(Direction.OUTGOING); // does this also return undirected edges?
            for (Relationship relationship : relationships) {
                   Node target = relationship.getEndNode();

                   if (!matrixIndices.containsKey(target)) {
                       matrixIndices.put(target, discoveredIndex);
                       indexMap.add(target);
                       targetIndex = discoveredIndex;
                       discoveredIndex ++;
                   } else
                       targetIndex = matrixIndices.get(target);

                  adjacency.set(originIndex, targetIndex, 1);
                  adjacency.set(targetIndex, originIndex, 1);
             }
        }
        return adjacency;
    }

    /**
     * Returns a Markov transition matrix (all entries are weighted by their out degree)
     * The matrix is in format (i <- j), the sum of any column is 1.
     */
    public Matrix getTransitionMatrix() {
        Iterable<Node> nodes = GlobalGraphOperations.at(database).getAllNodes();
        indexMap = new ArrayList<>();
        HashMap<Node, Integer> matrixIndices = new HashMap<>();

        int length = 0;
        int discoveredIndex = 0;
        int originIndex;
        int targetIndex;

        // TODO: avoid iterating over all nodes to obtain the length, or find a dynamic sparse matrix storage
        for (Node ignored : nodes)
            length ++;

        CRSMatrix adjacency = new CRSMatrix(length, length);

        for (Node origin : nodes) {
            // if node is not yet present in the matrix, index it

            if(!matrixIndices.containsKey(origin)) {
                matrixIndices.put(origin, discoveredIndex);
                indexMap.add(origin);
                originIndex = discoveredIndex;
                discoveredIndex ++;
            }else
                originIndex = matrixIndices.get(origin);


            Iterable<Relationship> relationships = origin.getRelationships(Direction.OUTGOING); // does this also return undirected edges?

            for (Relationship relationship : relationships) {
                Node target = relationship.getEndNode();

                if (!matrixIndices.containsKey(target)) {
                    matrixIndices.put(target, discoveredIndex);
                    indexMap.add(target);
                    targetIndex = discoveredIndex;
                    discoveredIndex ++;
                } else
                    targetIndex = matrixIndices.get(target);

                adjacency.set(originIndex, targetIndex, 1.0/ ((float) target.getDegree()));
                adjacency.set(targetIndex, originIndex, 1.0/ ((float) origin.getDegree()));
            }
        }

        return adjacency;
    }

    /**
     * Returns a google matrix given the specified damping constant.
     * The Google matrix is an iterative mtx for the pageRank algorithm.
     *
     * See.: The Anatomy of a Large-Scale Hypertextual Web Search Engine by Brin & Page
     *
     * @return Google matrix of the database, given the damping
     */
    public Matrix getGoogleMatrix(double damping) {
        Factory matrixFactory = new CRSFactory();

        Matrix transitionMatrix = getTransitionMatrix();
        int size = transitionMatrix.rows();
        Matrix identityMatrix =  matrixFactory.createIdentityMatrix(size);


        // return delta_ij * (1-d) / N + d*T_ij
        return identityMatrix.multiply((1-damping)/((float) size)).add(transitionMatrix.multiply(damping));

    }

    /**
     * Returns an node <-> index map. This is useful to identify the indices with
     * Node objects in Page Rank algorithm.
     * @return
     */
    public ArrayList<Node> getIndexMap() {
        return indexMap;
    }
}
