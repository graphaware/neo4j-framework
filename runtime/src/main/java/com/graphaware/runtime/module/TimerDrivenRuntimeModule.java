package com.graphaware.runtime.module;

import org.neo4j.graphdb.GraphDatabaseService;

import com.graphaware.runtime.state.ModuleContext;

/**
 * Specialisation of {@link RuntimeModule} that can be driven by a timing strategy as opposed to a response to transaction
 * events.
 */
public interface TimerDrivenRuntimeModule<C extends ModuleContext<?>> extends RuntimeModule {

	C doSomeWork(C lastContext, GraphDatabaseService graphDatabaseService);

}
