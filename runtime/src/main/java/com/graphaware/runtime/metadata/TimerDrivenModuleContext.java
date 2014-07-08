package com.graphaware.runtime.metadata;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.NotFoundException;

/**
 * Context of {@link com.graphaware.runtime.module.TimerDrivenModule}s encapsulating the position in the graph and additional
 * data, such as weight carried around for certain iterative algorithms, etc.
 * <p>
 * The position in the graph could be a single node, a collection of nodes, a single relationship, a collection of
 * relationships, a cluster of nodes and relationships, etc.
 * </p>
 *
 * @param <T> type of the position representation.
 */
public interface TimerDrivenModuleContext<T> {

    /**
     * Find the position in the database.
     *
     * @param database The {@link GraphDatabaseService} in which to find the position.
     * @return A representation of the position.
     * @throws NotFoundException if the position could not be found. Callers must handle this.
     */
	//TODO: make this throw a checked exception because we want module authors to handle this situation in their code
    T find(GraphDatabaseService database) throws NotFoundException;

}
