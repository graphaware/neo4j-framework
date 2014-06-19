package com.graphaware.runtime.state;

import org.neo4j.graphdb.GraphDatabaseService;

/**
 *
 */
public interface GraphPosition<T> {

    T find(GraphDatabaseService database);
}
