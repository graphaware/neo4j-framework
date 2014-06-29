package com.graphaware.runtime.module;

import com.graphaware.runtime.metadata.TimerDrivenModuleContext;
import com.graphaware.runtime.metadata.TimerDrivenModuleMetadata;
import org.neo4j.graphdb.GraphDatabaseService;

/**
 * Specialisation of {@link RuntimeModule} that can be driven by a timing strategy as opposed to a response to transaction
 * events.
 */
public interface TimerDrivenModule<C extends TimerDrivenModuleContext> extends RuntimeModule {

    /**
     * Create the initial context for this module, when no previously produced context is available.
     *
     * @param database against which the module is running.
     * @return initial context.
     */
    C createInitialContext(GraphDatabaseService database);

    /**
     * Perform the work which is the reason for this module's existence. Implementations can (and should) assume a running
     * transaction.
     *
     * @param lastContext context produced by the last run of this method.
     * @param database    against which the module is running.
     * @return context that will be presented next time the module is run.
     */
    C doSomeWork(C lastContext, GraphDatabaseService database);
}
