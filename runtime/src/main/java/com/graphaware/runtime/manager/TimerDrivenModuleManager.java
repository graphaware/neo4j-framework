package com.graphaware.runtime.manager;

import com.graphaware.runtime.module.TimerDrivenModule;

/**
 * {@link ModuleManager} for {@link TimerDrivenModule}s.
 */
public interface TimerDrivenModuleManager<T extends TimerDrivenModule> extends ModuleManager<T> {

    /**
     * Perform work needed to make modules start doing their job. Called exactly once each time the database is started.
     */
    void startModules();
}
