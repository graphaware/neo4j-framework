package com.graphaware.runtime.schedule;

import com.graphaware.runtime.metadata.TimerDrivenModuleContext;
import com.graphaware.runtime.module.TimerDrivenModule;

/**
 * A component delegating to registered {@link TimerDrivenModule}s on a scheduled basis.
 */
public interface TaskScheduler {

    /**
     * Register a module and its context. Registered modules will be delegated to and contexts managed by the implementation
     * of this interface after the first registration. Must be called before {@link #start()}.
     *
     * @param module   to register.
     * @param context of the module.
     * @param <C>      type of the metadata.
     * @param <T>      type of the module.
     */
    <C extends TimerDrivenModuleContext, T extends TimerDrivenModule<C>> void registerModuleAndContext(T module, C context);

    /**
     * Start scheduling tasks / delegating work to registered modules.
     */
    void start();

    /**
     * Stop scheduling tasks. Perform cleanup. No other methods should be called afterwards as this object will be useless.
     */
    void stop();
}
