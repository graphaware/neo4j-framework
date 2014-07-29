package com.graphaware.common.util.export;


import org.la4j.matrix.Matrix;
import org.neo4j.graphdb.Node;
import java.util.ArrayList;

/**
 * A data-object returned by NetworkMatrixFactory matrix methods.
 * Contains nodeList of the nodes and an adjacency matrix.
 *
 * The nodeList of nodes is an ordered array of nodes with
 * indices corresponding to rows/columns in the adjacency matrix (and derivates)
 *
 * TODO: Possibly rename NetworkMatrixFactory class and related classes such that its behavior is properly reflected
 */

public class NetworkMatrix {

    private final ArrayList<Node> nodeList;
    private final Matrix matrix;

    public NetworkMatrix(Matrix matrix, ArrayList<Node> nodeList)
    {
        this.matrix = matrix;
        this.nodeList = nodeList;
    }

    /**
     * Returns an ordered array of nodes. Node indices correspond to
     * rows/columns of the adjacency matrix.
     * @return
     */
    public ArrayList<Node> getNodeList() {
        return nodeList;
    }


    /**
     * Returns the stored matrix
     * @return
     */
    public Matrix getMatrix() {
        return matrix;
    }
}
