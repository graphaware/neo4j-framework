package com.graphaware.runtime.module;

import com.graphaware.runtime.metadata.GraphPosition;
import com.graphaware.runtime.metadata.TimerDrivenModuleMetadata;
import org.neo4j.graphdb.GraphDatabaseService;

/**
 * Specialisation of {@link RuntimeModule} that can be driven by a timing strategy as opposed to a response to transaction
 * events.
 */
public interface TimerDrivenModule<T, P extends GraphPosition<T>, M extends TimerDrivenModuleMetadata<P>> extends RuntimeModule<TimerDrivenModuleMetadata<P>> {

	M doSomeWork(M lastMetadata, GraphDatabaseService graphDatabaseService);
}
