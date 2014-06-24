package com.graphaware.runtime.module;

import com.graphaware.runtime.state.TimerDrivenModuleState;
import org.neo4j.graphdb.GraphDatabaseService;

/**
 * Specialisation of {@link RuntimeModule} that can be driven by a timing strategy as opposed to a response to transaction
 * events.
 */
public interface TimerDrivenRuntimeModule<C extends TimerDrivenModuleState<?>> extends RuntimeModule {

	C doSomeWork(C lastContext, GraphDatabaseService graphDatabaseService);

}
