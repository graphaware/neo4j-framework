package com.graphaware.runtime.metadata;

import org.neo4j.graphdb.GraphDatabaseService;

/**
 *   //todo Adam's context will be one implementation of this
 */
public interface GraphPosition<T> {

    T find(GraphDatabaseService database);
}
