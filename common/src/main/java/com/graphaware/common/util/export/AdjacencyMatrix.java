package com.graphaware.common.util.export;

import org.la4j.matrix.Matrix;
import org.la4j.matrix.sparse.CRSMatrix;
import org.neo4j.graphdb.*;
import org.neo4j.tooling.GlobalGraphOperations;

import java.util.HashMap;

/**
 * DO NOT USE - WORK IN PROGRESS
 *
 * Exports the full adjacency matrix.
 *
 * WARNING: This is for testing purposes only and should not
 *          be executed on a regular basis.
 *
 * TODO: Use la4j for internal matrix storage to allow for sparse matrices?
 */

public class AdjacencyMatrix{
    private final GraphDatabaseService database;

    public AdjacencyMatrix(GraphDatabaseService database) {
        this.database = database;
    }

    /**
     * Returns an adjacency matrix in a raw ArrayList<ArrayList<Integer>> form.
     * TODO: Use la4j? Allow for weights & inclusion strategies?
     * @return int[][] raw matrix
     */
    public Matrix get() {
        Iterable<Node> nodes = GlobalGraphOperations.at(database).getAllNodes();
        HashMap<Node, Integer> matrixIndices = new HashMap<>();

        int length = 0;
        int discoveredIndex = 0;
        int originIndex;
        int targetIndex;

        // TODO: avoid iterating over all nodes to obtain the length, or find a dynamic sparse matrix storage
        for (Node node : nodes)
            length ++;

         CRSMatrix adjacency = new CRSMatrix(length, length);

        for (Node origin : nodes) {
            // if node is not yet present in the matrix, index it

            if(!matrixIndices.containsKey(origin)) {
                matrixIndices.put(origin, discoveredIndex);
                originIndex = discoveredIndex;
//                System.out.println(origin.toString() + " " + originIndex);
                discoveredIndex ++;
            }else
                originIndex = matrixIndices.get(origin);


            Iterable<Relationship> relationships = origin.getRelationships(Direction.OUTGOING); // does this also return undirected edges?
            for (Relationship relationship : relationships) {
                   Node target = relationship.getEndNode();

                   if (!matrixIndices.containsKey(target)) {
                       matrixIndices.put(target, discoveredIndex);
                       targetIndex = discoveredIndex;
//                       System.out.println(target.toString() + " " + targetIndex);
                       discoveredIndex ++;
                   } else
                       targetIndex = matrixIndices.get(target);

//                  System.out.println(targetIndex + " : " + originIndex);
                  adjacency.set(originIndex, targetIndex, 1);
                  adjacency.set(targetIndex, originIndex, 1);
             }
        }

        return adjacency;
    }
}
