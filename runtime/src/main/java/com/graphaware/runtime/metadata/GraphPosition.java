package com.graphaware.runtime.metadata;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.NotFoundException;

/**
 * Representation of a position in the graph. This position could be a single node, a collection of nodes, a single
 * relationship, a collection of relationships, a cluster of nodes and relationships, etc.
 *
 * @param <T> type of the position object.
 */
public interface GraphPosition<T> {

    /**
     * Find the position in the database.
     *
     * @param database to find the position in.
     * @return position.
     * @throws org.neo4j.graphdb.NotFoundException if the position could not be found. Callers must handle this.
     */
    T find(GraphDatabaseService database) throws NotFoundException;
}
