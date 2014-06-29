package com.graphaware.runtime.module;

import com.graphaware.runtime.metadata.TimerDrivenModuleMetadata;
import org.neo4j.graphdb.GraphDatabaseService;

/**
 * Specialisation of {@link RuntimeModule} that can be driven by a timing strategy as opposed to a response to transaction
 * events.
 */
public interface TimerDrivenModule<M extends TimerDrivenModuleMetadata<?>> extends RuntimeModule<M> {

	M doSomeWork(M lastMetadata, GraphDatabaseService database);
}
