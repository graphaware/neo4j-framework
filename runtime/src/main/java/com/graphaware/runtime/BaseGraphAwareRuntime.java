/*
 * Copyright (c) 2013 GraphAware
 *
 * This file is part of GraphAware.
 *
 * GraphAware is free software: you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received a copy of
 * the GNU General Public License along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 */

package com.graphaware.runtime;

import com.graphaware.common.ping.GoogleAnalyticsStatsCollector;
import com.graphaware.runtime.config.FluentRuntimeConfiguration;
import com.graphaware.runtime.config.RuntimeConfiguration;
import com.graphaware.runtime.config.RuntimeConfigured;
import com.graphaware.runtime.module.RuntimeModule;
import com.graphaware.tx.event.improved.api.ImprovedTransactionData;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.event.ErrorState;
import org.neo4j.graphdb.event.KernelEventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Abstract base-class for {@link GraphAwareRuntime} implementations. Handles lifecycle of the runtime and basic
 * module-related sanity checks.
 */
public abstract class BaseGraphAwareRuntime implements GraphAwareRuntime, KernelEventHandler {

    private static final Logger LOG = LoggerFactory.getLogger(BaseGraphAwareRuntime.class);

    private static final ThreadLocal<Boolean> startingThread = new ThreadLocal<Boolean>() {
        @Override
        protected Boolean initialValue() {
            return false;
        }
    };

    private final RuntimeConfiguration configuration;

    private volatile State state = State.NONE;

    private enum State {
        NONE,
        REGISTERED,
        STARTING,
        STARTED,
        SHUTDOWN
    }

    /**
     * Create a new instance.
     *
     * @param configuration config.
     */
    protected BaseGraphAwareRuntime(RuntimeConfiguration configuration) {
        this.configuration = configuration;

        if (!State.NONE.equals(state)) {
            throw new IllegalStateException("Only one instance of the GraphAware Runtime should ever be instantiated and started.");
        }

        state = State.REGISTERED;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RuntimeConfiguration getConfiguration() {
        return configuration;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final synchronized void registerModule(RuntimeModule module) {
        if (!State.REGISTERED.equals(state)) {
            LOG.error("Modules must be registered before GraphAware Runtime is started!");
            throw new IllegalStateException("Modules must be registered before GraphAware Runtime is started!");
        }

        LOG.info("Registering module " + module.getId() + " with GraphAware Runtime.");

        checkNotAlreadyRegistered(module);
        doRegisterModule(module);

        if (module instanceof RuntimeConfigured) {
            ((RuntimeConfigured) module).configurationChanged(configuration);
        }

        GoogleAnalyticsStatsCollector.getInstance().moduleStart(module.getClass().getCanonicalName());
    }

    /**
     * Check that the given module isn't already registered with the runtime.
     *
     * @param module to check.
     * @throws IllegalStateException in case the module is already registered.
     */
    protected abstract void checkNotAlreadyRegistered(RuntimeModule module);

    /**
     * Perform the actual module registration after sanity checks have passed.
     *
     * @param module to register.
     */
    protected abstract void doRegisterModule(RuntimeModule module);

    /**
     * {@inheritDoc}
     */
    @Override
    public final synchronized void start() {
        start(false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final synchronized void start(boolean skipLoadingMetadata) {
        if (State.STARTED.equals(state)) {
            LOG.debug("GraphAware already started");
            return;
        }

        if (State.STARTING.equals(state)) {
            throw new IllegalStateException("Attempt to start GraphAware from multiple different threads. This is a bug");
        }

        if (!State.REGISTERED.equals(state)) {
            throw new IllegalStateException("Illegal Runtime state " + state + "! This is a bug");
        }

        startingThread.set(true);
        LOG.info("Starting GraphAware...");
        state = State.STARTING;

        GoogleAnalyticsStatsCollector.getInstance().runtimeStart();

        doStart(skipLoadingMetadata);

        state = State.STARTED;
        LOG.info("GraphAware started.");
        startingThread.set(false);
    }

    /**
     * Perform the actual start of the runtime, being certain that it is the right time to do so.
     *
     * @param skipLoadingMetadata true for skipping the metadata loading phase.
     */
    protected void doStart(boolean skipLoadingMetadata) {
        if (skipLoadingMetadata) {
            LOG.info("Metadata loading skipped.");
        } else {
            LOG.info("Loading module metadata...");
            try (Transaction tx = startTransaction()) {
                Set<String> moduleIds = loadMetadata();
                cleanupMetadata(moduleIds);
                tx.success();
            }
            LOG.info("Module metadata loaded.");
        }
    }

    /**
     * Start a database transaction.
     *
     * @return tx.
     */
    protected abstract Transaction startTransaction();

    /**
     * Load module metadata.
     *
     * @return IDs of all modules registered with the runtime, no matter whether they previously had some metadata in
     *         the graph or not.
     */
    protected abstract Set<String> loadMetadata();

    /**
     * Perform cleanup of metadata potentially written to the graph by modules that aren't used any more.
     *
     * @param usedModules IDs of all the used modules (should be the same as returned by {@link #loadMetadata()}.
     */
    protected abstract void cleanupMetadata(Set<String> usedModules);

    /**
     * {@inheritDoc}
     */
    @Override
    public final void waitUntilStarted() {
        if (!isStarted(null)) {
            throw new IllegalStateException("It appears that the thread starting the runtime called waitUntilStarted() before it's finished its job. This is a bug");
        }
    }

    /**
     * Checks to see if this {@link GraphAwareRuntime} is started. Blocks until it is started, unless one of the following
     * conditions is met:
     * <ul>
     * <li>it's already started, in which case the method returns <code>true</code></li>
     * <li>hasn't even started starting for more than 1s, in which case an exception is thrown</li>
     * <li>hasn't been started yet, but the transaction triggering the call of this method isn't mutating, in which case it returns <code>false</code></li>
     * <li>it's starting but the caller is the thread that starts the runtime itself, in which case it returns <code>false</code></li>
     * </ul>
     *
     * @return <code>true</code> iff the runtime is started.
     *         <code>false</code> iff the runtime isn't started but it is safe to proceed.
     * @throws IllegalStateException in case the runtime hasn't been started at all.
     */
    protected final boolean isStarted(ImprovedTransactionData transactionData) {
        if (State.NONE.equals(state)) {
            throw new IllegalStateException("Runtime has not been registered! This is a bug.");
        }

        if (State.SHUTDOWN.equals(state)) {
            throw new IllegalStateException("Runtime is being / has been shut down.");
        }

        int attempts = 0;

        while (!State.STARTED.equals(state)) {
            //workaround for https://github.com/neo4j/neo4j/issues/2804
            if (transactionData != null && !transactionData.mutationsOccurred()) {
                return false;
            }

            if (State.STARTING.equals(state) && startingThread.get()) {
                return false;
            }

            try {
                attempts++;
                if (attempts > 100 && State.REGISTERED.equals(state)) {
                    throw new IllegalStateException("Runtime has not been started!");
                }
                TimeUnit.MILLISECONDS.sleep(10);
            } catch (InterruptedException e) {
                //just continue
            }
        }

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void beforeShutdown() {
        LOG.info("Shutting down GraphAware Runtime... ");
        state = State.SHUTDOWN;
        shutdownModules();
        afterShutdown();
        LOG.info("GraphAware Runtime shut down.");
    }

    /**
     * React to shutdown.
     */
    protected void afterShutdown() {
        //for subclasses
    }

    /**
     * Shutdown all modules.
     */
    protected abstract void shutdownModules();

    /**
     * {@inheritDoc}
     */
    @Override
    public final void kernelPanic(ErrorState error) {
        //do nothing
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Object getResource() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final ExecutionOrder orderComparedTo(KernelEventHandler other) {
        return ExecutionOrder.DOESNT_MATTER;
    }
}
