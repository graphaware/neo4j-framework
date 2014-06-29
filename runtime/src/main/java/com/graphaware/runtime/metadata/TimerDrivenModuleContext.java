package com.graphaware.runtime.metadata;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.NotFoundException;

/**
 * Context of {@link com.graphaware.runtime.module.TimerDrivenModule}s encapsulating the position in the graph and additional
 * data, such as weight carried around for certain iterative algorithms, etc.
 * <p/>
 * The position in the graph could be a single node, a collection of nodes, a single relationship, a collection of
 * relationships, a cluster of nodes and relationships, etc.
 *
 * @param <T> type of the position representation.
 */
public interface TimerDrivenModuleContext<T> {

    /**
     * Find the position in the database.
     *
     * @param database to find the position in.
     * @return position.
     * @throws org.neo4j.graphdb.NotFoundException
     *          if the position could not be found. Callers must handle this.
     */
    T find(GraphDatabaseService database) throws NotFoundException;
}
