package com.graphaware.runtime.schedule;

import com.graphaware.runtime.metadata.TimerDrivenModuleMetadata;
import com.graphaware.runtime.module.TimerDrivenModule;

/**
 *                       // We will start the scheduling here.
 /*
 * I reckon we want to have something here that says "start the scheduler" and then it's off and running.
 *
 * The objects that are actually scheduled need to provide the environment in which the modules will operate, namely:
 * - start and manage a database transaction
 *   (although our diagram said the tx boundary was just below GraphAwareRuntime, that was only for the init process)
 * - ensure that exceptions thrown from the modules don't affect the module manager adversely
 * - ensure persistence of state of module and/or graph walk
 *   (i.e., make sure that if the database is shut down then we can reinitialise)
 */

public interface TaskScheduler {

    <M extends TimerDrivenModuleMetadata, T extends TimerDrivenModule<M>>  void registerMetadata(T module, M metadata);

    void start();

    void stop();
}
