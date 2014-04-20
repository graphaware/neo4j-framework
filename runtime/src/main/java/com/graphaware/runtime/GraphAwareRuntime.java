package com.graphaware.runtime;

import org.neo4j.graphdb.event.KernelEventHandler;
import org.neo4j.graphdb.event.TransactionEventHandler;

/**
 * Runtime that delegates to registered {@link GraphAwareRuntimeModule}s to perform useful work.
 * There must be exactly one instance of this runtime for a single {@link org.neo4j.graphdb.GraphDatabaseService}.
 * <p/>
 * The runtime registers itself as a Neo4j {@link org.neo4j.graphdb.event.TransactionEventHandler},
 * translates {@link org.neo4j.graphdb.event.TransactionData} into {@link com.graphaware.tx.event.improved.api.ImprovedTransactionData}
 * and lets registered {@link GraphAwareRuntimeModule}s deal with the data before each transaction
 * commits, in the order the modules were registered.
 * <p/>
 * After all desired modules have been registered, {@link #start()} can be called in order to initialize the runtime and
 * all its modules before the database is exposed to callers. No more modules can be registered thereafter.
 * <p/>
 * If not called explicitly, the {@link #start()} method will be called automatically by the runtime upon first
 * transaction received from callers. In such case, all other transaction will be blocked until the runtime and all its
 * modules have been initialized.
 * <p/>
 * Every new {@link GraphAwareRuntimeModule} and every {@link GraphAwareRuntimeModule}
 * whose configuration has changed since the last run will be forced to (re-)initialize, which can lead to very long
 * startup times, as (re-)initialization could be a global graph operation. Re-initialization will also be automatically
 * performed for all modules, for which it has been detected that something is out-of-sync
 * (module threw a {@link NeedsInitializationException}).
 * <p/>
 * A single GraphAware Runtime Reference Node with label {@link com.graphaware.runtime.config.RuntimeConfiguration#GA_ROOT} needs to be present in the
 * database in order for this runtime to work. It does not need to be used by the application, nor does it need to be
 * connected to any other node, but it needs to be present in the database. It is automatically created if not present,
 * but should not be deleted from the database. In fact, the runtime will prevent its deletion from taking place, if running.
 */
public interface GraphAwareRuntime extends TransactionEventHandler<Void>, KernelEventHandler {

    /**
     * Register a {@link GraphAwareRuntimeModule}. Note that modules are delegated to in the order they are registered.
     * Must be called before the Runtime is started.
     *
     * @param module to register.
     */
    void registerModule(GraphAwareRuntimeModule module);

    /**
     * Register a {@link GraphAwareRuntimeModule} and optionally force its (re-)initialization.
     * <p/>
     * Forcing re-initialization should only be necessary in exceptional circumstances, such as that the database has
     * been written to without the module being registered / runtime running. Re-initialization can be a very
     * expensive, graph-global operation, should only be run once, database stopped and started again without forcing
     * re-initialization.
     * <p/>
     * New modules and modules with changed configuration will be (re-)initialized automatically; there is no need to use
     * this method for that purpose.
     * <p/>
     * Note that modules are delegated to in the order they are registered. Must be called before the Runtime is started.
     *
     * @param module              to register.
     * @param forceInitialization true to force (re-)initialization.
     */
    void registerModule(GraphAwareRuntimeModule module, boolean forceInitialization);

    /**
     * Start the Runtime. Must be called before anything gets written into the database, but will be called automatically
     * if not called explicitly. Automatic invocation means that first transactions run against the database will have
     * to wait for the Runtime to be started and modules initialized.
     */
    void start();

    /**
     * Start the runtime, optionally skipping the initialization phase. It is not recommended to skip initialization;
     * un-initialized modules might not behave correctly.
     *
     * @param skipInitialization true for skipping initialization.
     */
    void start(boolean skipInitialization);
}
