package com.graphaware.runtime.metadata;

import com.graphaware.runtime.config.TxDrivenModuleConfiguration;

/**
 *
 */
public interface TxDrivenModuleMetadata extends ModuleMetadata {

    TxDrivenModuleConfiguration getConfig();

    /**
     * Does the module need initialization?
     *
     * @return true iff the module needs initialization.
     */
    boolean needsInitialization();

    long timestamp();

    TxDrivenModuleMetadata markedNeedingInitialization();
}
