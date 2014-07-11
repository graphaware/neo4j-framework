package com.graphaware.runtime.metadata;

/**
 * {@link ModuleMetadata} for {@link com.graphaware.runtime.module.TimerDrivenModule}s.
 */
public interface TimerDrivenModuleMetadata<P extends TimerDrivenModuleContext<?>> extends ModuleMetadata {

    /**
     * Get the context that the module produced when it last did some work.
     *
     * @return last context, <code>null</code> if there is no such context (first run).
     */
    P lastContext();
}
