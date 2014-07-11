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
     * Check that the given module isn't already registered with the manager.
     *
     * @param module to check.
     * @throws IllegalStateException in case the module is already registered.
     */
    void checkNotAlreadyRegistered(RuntimeModule module);

    /**
     * Register a module with this manager.
     *
     * @param module to register.
     * @throws IllegalStateException in case the module is already registered.
     */
    void registerModule(T module);

    /**
     * Load module metadata from wherever they are stored in between database restarts and do whatever is necessary
     * to do with this metadata before the modules can be used.
     *
     * @return IDs of all modules registered with this manager.
     */
    Set<String> loadMetadata();

    /**
     * Perform cleanup metadata written by modules that are no longer present.
     *
     * @param usedModules IDs of all modules that are known to be used by the runtime during the current run.
     */
    void cleanupMetadata(Set<String> usedModules);

    /**
     * Perform work needed to make modules start doing their job. Called exactly once each time the database is started.
     */
    void startModules();

    /**
     * Bring all modules to an orderly shutdown, when the database is stopped.
     */
    void shutdownModules();
}
