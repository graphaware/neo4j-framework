package com.graphaware.neo4j.framework;

import com.graphaware.neo4j.tx.event.api.ImprovedTransactionData;
import com.graphaware.neo4j.tx.event.strategy.InclusionStrategies;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.unsafe.batchinsert.BatchInserter;

/**
 * A {@link GraphAwareFramework} module performing some useful work based on about-to-be-committed transaction data.
 * <p/>
 * The implementation should override {@link #hashCode()}, which should ideally change when the module's configuration
 * changes.
 */
public interface GraphAwareModule {

    /**
     * Get a human-readable (ideally short) ID of this module. This ID must be unique across all {@link GraphAwareModule}s
     * used in a single {@link GraphAwareFramework} instance.
     *
     * @return short ID of this module.
     */
    String getId();

    /**
     * Initialize this module. This method must bring the module to a state equivalent to a state of the same module that
     * has been registered at all times since the database was empty. It can perform global-graph operations to achieve
     * this.
     *
     * @param database to initialize this module for.
     */
    void initialize(GraphDatabaseService database);

    /**
     * Re-initialize this module. This method must remove all metadata written to the graph by this module and bring the
     * module to a state equivalent to a state of the same module that has been registered at all times since the
     * database was empty. It can perform global-graph operations to achieve this.
     *
     * @param database to initialize this module for.
     */
    void reinitialize(GraphDatabaseService database);

    /**
     * Initialize this module. This method must bring the module to a state equivalent to a state of the same module that
     * has been registered at all times since the database was empty. It can perform global-graph operations to achieve
     * this.
     *
     * @param batchInserter to initialize this module for.
     */
    void initialize(BatchInserter batchInserter);

    /**
     * Re-initialize this module. This method must remove all metadata written to the graph by this module and bring the
     * module to a state equivalent to a state of the same module that has been registered at all times since the
     * database was empty. It can perform global-graph operations to achieve this.
     *
     * @param batchInserter to initialize this module for.
     */
    void reinitialize(BatchInserter batchInserter);

    /**
     * Perform cleanup if needed before database shutdown.
     */
    void shutdown();

    /**
     * Perform the core business logic of this module before a transaction commits.
     *
     * @param transactionData data about the soon-to-be-committed transaction. It is already filtered based on {@link #getInclusionStrategies()}.
     * @throws NeedsInitializationException if it detects data is out of sync. {@link #initialize(org.neo4j.graphdb.GraphDatabaseService)}  will be called next
     *                                      time the {@link GraphAwareFramework} is started. Until then, the module
     *                                      should perform on best-effort basis.
     */
    void beforeCommit(ImprovedTransactionData transactionData);

    /**
     * Get the inclusion strategies used by this module. If unsure, return {@link com.graphaware.neo4j.framework.strategy.InclusionStrategiesImpl#all()},
     * which includes all non-internal nodes, properties, and relationships.
     *
     * @return strategy.
     */
    InclusionStrategies getInclusionStrategies();
}
