package com.graphaware.common.util.export;

import org.la4j.factory.CRSFactory;
import org.la4j.factory.Factory;
import org.la4j.matrix.Matrix;
import org.la4j.matrix.sparse.CRSMatrix;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import java.util.ArrayList;
import java.util.HashMap;

import static com.graphaware.common.util.IterableUtils.count;
import static org.neo4j.tooling.GlobalGraphOperations.at;

/**
 * EXPERIMENTAL - WORK IN PROGRESS
 *
 * Exports the full adjacency matrix.
 *
 * WARNING: This is for testing purposes only and should not
 *          be executed on a regular basis.
 *
 * TODO: Allow for directed edges?
 * TODO: Return the rank <-> node map as well. Possibly return a pair, or some custom class?
 *       UPDATE: this is done by a simple workaround now not to waste time on this too much,
 *               but it could be definitelly more cleaner.
 * 
 */

public class NetworkMatrixFactory {
    private final GraphDatabaseService database;

    public NetworkMatrixFactory(GraphDatabaseService database) {
        this.database = database;
    }

    /**
     * Returns an adjacency matrix in a raw ArrayList<ArrayList<Integer>> form.
     * TODO: Allow for weights & inclusion strategies?
     * @return la4j matrix sparse matrix
     */
    public NetworkMatrix getAdjacencyMatrix() {
        Iterable<Node> nodes = at(database).getAllNodes();
        ArrayList<Node> nodeList = new ArrayList<Node>();
        HashMap<Node, Integer> matrixIndices = new HashMap<>();

        int discoveredIndex = 0;
        int originIndex;
        int targetIndex;

        long length = count(at(database).getAllNodes());

        if (length > Integer.MAX_VALUE) {
            throw new IllegalStateException("too much");
        }

        int smallLength = Long.valueOf(length).intValue();

        CRSMatrix adjacency = new CRSMatrix(smallLength, smallLength);

        for (Node origin : nodes) {
            // if node is not yet present in the matrix, rank it

            if(!matrixIndices.containsKey(origin)) {
                matrixIndices.put(origin, discoveredIndex);
                nodeList.add(origin);
                originIndex = discoveredIndex;
                discoveredIndex ++;
            }else
                originIndex = matrixIndices.get(origin);


            Iterable<Relationship> relationships = origin.getRelationships(Direction.OUTGOING); // does this also return undirected edges?
            for (Relationship relationship : relationships) {
                   Node target = relationship.getEndNode();

                   if (!matrixIndices.containsKey(target)) {
                       matrixIndices.put(target, discoveredIndex);
                       nodeList.add(target);
                       targetIndex = discoveredIndex;
                       discoveredIndex ++;
                   } else
                       targetIndex = matrixIndices.get(target);

                  adjacency.set(originIndex, targetIndex, 1); // The only difference between the two methods is here
                  adjacency.set(targetIndex, originIndex, 1);
             }
        }

        NetworkMatrix toReturn = new NetworkMatrix(adjacency, nodeList);
        return toReturn;
    }

    /**
     * Returns a Markov transition matrix (all entries are weighted by their out degree)
     * The matrix is in format (i <- j), the sum of any column is 1.
     */
    public NetworkMatrix getTransitionMatrix() {
        Iterable<Node> nodes = at(database).getAllNodes();
        ArrayList<Node> nodeList = new ArrayList<>();
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
            // if node is not yet present in the matrix, rank it

            if(!matrixIndices.containsKey(origin)) {
                matrixIndices.put(origin, discoveredIndex);
                nodeList.add(origin);
                originIndex = discoveredIndex;
                discoveredIndex ++;
            }else
                originIndex = matrixIndices.get(origin);


            Iterable<Relationship> relationships = origin.getRelationships(Direction.OUTGOING); // does this also return undirected edges?

            for (Relationship relationship : relationships) {
                Node target = relationship.getEndNode();

                if (!matrixIndices.containsKey(target)) {
                    matrixIndices.put(target, discoveredIndex);
                    nodeList.add(target);
                    targetIndex = discoveredIndex;
                    discoveredIndex ++;
                } else
                    targetIndex = matrixIndices.get(target);

                adjacency.set(originIndex, targetIndex, 1.0/ ((float) target.getDegree()));
                adjacency.set(targetIndex, originIndex, 1.0/ ((float) origin.getDegree()));
            }
        }

        NetworkMatrix toReturn = new NetworkMatrix(adjacency, nodeList);
        return toReturn;
    }

    /**
     * Returns a google matrix given the specified damping constant.
     * The Google matrix is an iterative mtx for the pageRank algorithm.
     *
     * See.: The Anatomy of a Large-Scale Hypertextual Web Search Engine by Brin & Page
     *
     * @return Google matrix of the database, given the damping
     */
    public NetworkMatrix getGoogleMatrix(double damping) {
        Factory matrixFactory = new CRSFactory();

        NetworkMatrix transitionMatrixData = getTransitionMatrix();
        ArrayList<Node> nodeList = transitionMatrixData.getNodeList();
        Matrix transitionMatrix = getTransitionMatrix().getMatrix();

        int size = transitionMatrix.rows();
        Matrix identityMatrix =  matrixFactory.createIdentityMatrix(size);
        Matrix googleMatrix = identityMatrix.multiply((1-damping)/((float) size)).add(transitionMatrix.multiply(damping));

        NetworkMatrix toReturn = new NetworkMatrix(googleMatrix, nodeList);
        return toReturn;

    }

    /**
     * Returns an node <-> rank map. This is useful to identify the indices with
     * Node objects in Page Rank algorithm.
     * @return
     */
    /* public ArrayList<Node> getIndexMap() {
        return indexMap;
    } */
}
