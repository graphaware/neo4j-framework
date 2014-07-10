package com.graphaware.runtime.metadata;

import com.graphaware.runtime.config.TxDrivenModuleConfiguration;

/**
 * {@link ModuleMetadata} for {@link com.graphaware.runtime.module.TxDrivenModule}s.
 */
public interface TxDrivenModuleMetadata extends ModuleMetadata {

    /**
     * Get the configuration of the module. It is part of the {@link TxDrivenModuleMetadata} in order to detect
     * configuration changes after a restart.
     *
     * @return configuration.
     */
    TxDrivenModuleConfiguration getConfig();

    /**
     * Does the module need initialization?
     *
     * @return true iff the module needs initialization. This is usually the case when something out-of-sync has been
     *         detected during the last run of the database.
     */
    boolean needsInitialization();

    /**
     * Get the time in milliseconds since 1/1/1970 of the first occurrence of a problem that caused the {@link #needsInitialization()}
     * method to return <code>true</code>.
     *
     * @return timestamp of the first problem occurrence, -1 if {@link #needsInitialization()} returns <code>false</code>.
     */
    long problemTimestamp();

    /**
     * Create a new instance of this class with {@link #needsInitialization()} returning <code>true</code>. This must cause
     * {@link #problemTimestamp()} to return time representing the instant when this method was called. Same instance
     * of the implementation of this class should be returned when {@link #needsInitialization()} already returns
     * <code>true</code>.
     *
     * @return new instance of this class with {@link #needsInitialization()} returning <code>true</code>, same instance
     *         if that's already the case.
     */
    TxDrivenModuleMetadata markedNeedingInitialization();
}
