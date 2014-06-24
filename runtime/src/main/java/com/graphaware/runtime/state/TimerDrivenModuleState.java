package com.graphaware.runtime.state;

/**
 * {@link ModuleState} for {@link com.graphaware.runtime.module.TimerDrivenRuntimeModule}.
 */
public interface TimerDrivenModuleState<P extends GraphPosition> extends ModuleState {

    /**
     * Get the last position where this module did some work.
     *
     * @return last position.
     */
    P getLastPosition();
}
