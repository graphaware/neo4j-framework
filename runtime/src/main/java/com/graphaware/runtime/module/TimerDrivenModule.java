package com.graphaware.runtime.module;

import com.graphaware.runtime.metadata.TimerDrivenModuleMetadata;
import org.neo4j.graphdb.GraphDatabaseService;

/**
 * Specialisation of {@link RuntimeModule} that can be driven by a timing strategy as opposed to a response to transaction
 * events.
 */
public interface TimerDrivenModule<M extends TimerDrivenModuleMetadata<?>> extends RuntimeModule<M> {

    /**
     * Create the first metadata for this module, when no previously produced metadata is available.
     *
     * @param database against which the module is running.
     * @return first metadata.
     */
    M createFirstMetadata(GraphDatabaseService database);

    /**
     * Perform the work which is the reason for this module's existence. Implementations can (and should) assume a running
     * transaction.
     *
     * @param lastMetadata metadata produced by the last run of this method.
     * @param database     against which the module is running.
     * @return metadata that will be presented next time the module is run.
     */
    M doSomeWork(M lastMetadata, GraphDatabaseService database);
}
