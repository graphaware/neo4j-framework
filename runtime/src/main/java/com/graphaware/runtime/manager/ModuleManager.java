package com.graphaware.runtime.manager;

import com.graphaware.runtime.module.RuntimeModule;
import org.neo4j.graphdb.event.TransactionData;

import java.util.Set;

/**
 * A manager of {@link RuntimeModule}s, which takes care of their lifecycle.
 *
 * @param <T> type of module this manager can manage.
 */
public interface ModuleManager<T extends RuntimeModule> {

    /**
     * Throw an exception if the transaction that's about to be committed does something illegal from the manager's
     * point of view. This is a performance optimization.
     *
     * @param transactionData about-to-be-committed transaction data.
     * @throws IllegalStateException if the transaction is illegal.
     */
    void throwExceptionIfIllegal(TransactionData transactionData);

    /**
     * Register a module with this manager.
     *
     * @param module to register.
     * @throws IllegalStateException in case the module is already registered.
     */
    void registerModule(T module);

    /**
     * Initialize modules if needed. A module should only be "initialised" if it has been established that it has been
     * registered for the first time, or that its configuration has changed since the last time the database was started.
     *
     * @return IDs of all registered modules, no matter if they have or have not been initialized.
     */
    Set<String> initializeModules();

    /**
     * Perform cleanup of unused modules that might have written their metadata into the graph but are no longer present.
     *
     * @param usedModules IDs of modules that are known to be used.
     */
    void performCleanup(Set<String> usedModules);

    /**
     * Bring all modules to an orderly shutdown, when the database is stopped.
     */
    void stopModules();
}
