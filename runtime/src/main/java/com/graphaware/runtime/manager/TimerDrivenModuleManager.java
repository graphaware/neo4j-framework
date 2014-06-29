package com.graphaware.runtime.manager;

import com.graphaware.runtime.metadata.TimerDrivenModuleMetadata;
import com.graphaware.runtime.module.TimerDrivenModule;

/**
 *
 */
public interface TimerDrivenModuleManager extends ModuleManager<TimerDrivenModule<? extends TimerDrivenModuleMetadata<?>>> {

    /**
     * Perform work needed to make modules start doing their job. Called exactly once each time the database is started.
     */
    void startModules();
}
