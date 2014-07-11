package com.graphaware.common.util.export;

import org.neo4j.graphdb.*;
import org.neo4j.tooling.GlobalGraphOperations;

import java.util.ArrayList;
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

public class AdjacencyMatrix {
    private final GraphDatabaseService database;

    public AdjacencyMatrix(GraphDatabaseService database) {
        this.database = database;
    }

    /**
     * Returns an adjacency matrix in a raw ArrayList<ArrayList<Integer>> form.
     * TODO: Use la4j? Allow for weights & inclusion strategies?
     * @return int[][] raw matrix
     */
    public ArrayList<ArrayList<Integer>> getRaw() {
        Iterable<Node> nodes = GlobalGraphOperations.at(database).getAllNodes();
        HashMap<Node, Integer> matrixIndices = new HashMap<>();

        int discoveredIndex = 0;
        int originIndex = 0;
        int targetIndex = 0;

        ArrayList<ArrayList<Integer>> adjacency = new ArrayList<>();

        for (Node origin : nodes) {
            ArrayList<Integer> row = new ArrayList<>();

            // if node is not yet present in the matrix, index it
            if (!matrixIndices.containsKey(origin)) {
                matrixIndices.put(origin, discoveredIndex);
                originIndex = discoveredIndex;
                discoveredIndex++;
            } else
                originIndex = matrixIndices.get(origin);

            Iterable<Relationship> relationships = origin.getRelationships(Direction.OUTGOING); // does this also return undirected edges?
            for (Relationship relationship : relationships) {
                Node target = relationship.getEndNode();

                if (!matrixIndices.containsKey(target)) {
                    matrixIndices.put(target, discoveredIndex);
                    targetIndex = discoveredIndex;
                    discoveredIndex++;
                } else {
                    targetIndex = matrixIndices.get(target);
                }

                row.set(targetIndex, 1); // Unweighted relationships. TODO: Pad with zeroes!
            }

            // set the current row to adjacency.
            adjacency.set(originIndex, row);

            // TODO: Pad the adjacency list accordingly with zeroes.
        }
        return adjacency;
    }
}
