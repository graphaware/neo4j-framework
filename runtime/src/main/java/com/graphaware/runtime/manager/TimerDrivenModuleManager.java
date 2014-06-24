package com.graphaware.runtime.manager;

import com.graphaware.runtime.module.TimerDrivenRuntimeModule;

/**
 *
 */
public interface TimerDrivenModuleManager extends ModuleManager<TimerDrivenRuntimeModule> {

    /**
     * Perform work needed to make modules start doing their job. Called exactly once each time the database is started.
     */
    void startModules();


}
