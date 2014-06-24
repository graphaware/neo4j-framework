package com.graphaware.runtime.state;

/**
 * A {@link ModuleState} for {@link com.graphaware.runtime.module.TransactionDrivenRuntimeModule}s.
 */
public interface TransactionDrivenModuleState extends ModuleState {

    /**
     * Does the module need initialization?
     *
     * @return true iff the module needs initialization.
     */
    boolean needsInitialization();
}
