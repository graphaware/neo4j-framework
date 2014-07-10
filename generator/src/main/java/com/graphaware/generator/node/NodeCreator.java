package com.graphaware.generator.node;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;

/**
 * A component creating {@link org.neo4j.graphdb.Node}s with labels and properties.
 */
public interface NodeCreator {

    /**
     * Create a node with labels and properties.
     *
     * @param database to create the node in.
     * @return created node.
     */
    Node createNode(GraphDatabaseService database);
}
