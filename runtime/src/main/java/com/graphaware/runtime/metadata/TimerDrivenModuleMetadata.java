package com.graphaware.runtime.metadata;

/**
 * {@link ModuleMetadata} for {@link com.graphaware.runtime.module.TimerDrivenModule}s.
 */
public interface TimerDrivenModuleMetadata<P extends GraphPosition<?>> extends ModuleMetadata {

    /**
     * Get the last position where this module did some work.
     *
     * @return last position.
     */
    P getLastPosition();
}
