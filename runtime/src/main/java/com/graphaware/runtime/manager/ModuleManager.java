package com.graphaware.runtime.manager;

import java.util.Set;

import com.graphaware.runtime.module.RuntimeModule;

/**
 * A manager of {@link RuntimeModule}s, which takes care of their lifecycle.
 *
 * @param <T> type of module this manager can manage.
 */
public interface ModuleManager<T extends RuntimeModule> {

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
    void removeUnusedModules(Set<String> usedModules);

    /**
     * Bring all modules to an orderly shutdown, when the database is stopped.
     */
    void shutdownModules();
}
