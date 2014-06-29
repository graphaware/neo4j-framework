package com.graphaware.runtime.schedule;

import com.graphaware.runtime.metadata.TimerDrivenModuleMetadata;
import com.graphaware.runtime.module.TimerDrivenModule;

/**
 * A component delegating to registered {@link TimerDrivenModule}s on a scheduled basis.
 */
public interface TaskScheduler {

    /**
     * Register a module and its metadata. Registered modules will be delegated to and metadata managed by the implementation
     * of this interface after the first registration. Must be called before {@link #start()}.
     *
     * @param module   to register.
     * @param metadata of the module.
     * @param <M>      type of the metadata.
     * @param <T>      type of the module.
     */
    <M extends TimerDrivenModuleMetadata, T extends TimerDrivenModule<M>> void registerModuleAndMetadata(T module, M metadata);

    /**
     * Start scheduling tasks / delegating work to registered modules.
     */
    void start();

    /**
     * Stop scheduling tasks. Perform cleanup. No other methods should be called afterwards as this object will be useless.
     */
    void stop();
}
