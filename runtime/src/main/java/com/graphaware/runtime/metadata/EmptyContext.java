package com.graphaware.runtime.metadata;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.NotFoundException;

/**
 * A {@link TimerDrivenModuleContext} that is empty, i.e. holds no information about the module's position in the graph.
 */
public final class EmptyContext extends BaseTimerDrivenModuleContext<Void> {

    /**
     * Construct an empty context requesting that the module be called again ASAP.
     */
    public EmptyContext() {
    }

    /**
     * Construct an empty context requesting that the module be called again earliest at a specified point in time.
     *
     * @param earliestNextCall time in ms since 1/1/1970 when the module should be called again at the earliest.
     */
    public EmptyContext(long earliestNextCall) {
        super(earliestNextCall);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Void find(GraphDatabaseService database) throws NotFoundException {
        throw new UnsupportedOperationException("Empty context holds no information about the module's position in the graph");
    }
}
