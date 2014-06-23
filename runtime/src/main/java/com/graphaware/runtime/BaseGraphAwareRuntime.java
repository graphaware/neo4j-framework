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

import com.graphaware.runtime.config.DefaultRuntimeConfiguration;
import com.graphaware.runtime.config.RuntimeConfiguration;
import com.graphaware.runtime.config.RuntimeConfigured;
import com.graphaware.runtime.manager.TransactionDrivenModuleManager;
import com.graphaware.runtime.module.RuntimeModule;
import com.graphaware.runtime.module.TransactionDrivenRuntimeModule;
import com.graphaware.tx.event.improved.api.LazyTransactionData;
import org.apache.log4j.Logger;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.event.ErrorState;
import org.neo4j.graphdb.event.KernelEventHandler;
import org.neo4j.graphdb.event.TransactionData;

/**
 * Abstract base-class for {@link GraphAwareRuntime} implementations.
 */
public abstract class BaseGraphAwareRuntime implements GraphAwareRuntime {
    private static final Logger LOG = Logger.getLogger(BaseGraphAwareRuntime.class);

    private final RuntimeConfiguration configuration;

    private final TransactionDrivenModuleManager transactionDrivenModuleManager;

    private State state = State.NONE;

    private enum State {
        NONE,
        STARTING,
        STARTED
    }

    /**
     * Create a new instance of the runtime with {@link com.graphaware.runtime.config.DefaultRuntimeConfiguration}.
     */
    public BaseGraphAwareRuntime(TransactionDrivenModuleManager transactionDrivenModuleManager) {
        this(DefaultRuntimeConfiguration.getInstance(), transactionDrivenModuleManager);
    }

    protected BaseGraphAwareRuntime(RuntimeConfiguration configuration, TransactionDrivenModuleManager transactionDrivenModuleManager) {
        this.configuration = configuration;
        this.transactionDrivenModuleManager = transactionDrivenModuleManager;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final synchronized void registerModule(RuntimeModule module) {
        if (!State.NONE.equals(state)) {
            LOG.error("Modules must be registered before GraphAware Runtime is started!");
            throw new IllegalStateException("Modules must be registered before GraphAware Runtime is started!");
        }

        LOG.info("Registering module " + module.getId() + " with GraphAware Runtime.");

        doRegisterModule(module);

        if (module instanceof RuntimeConfigured) {
            ((RuntimeConfigured) module).configurationChanged(configuration);
        }
    }

    protected void doRegisterModule(RuntimeModule module) {
        if (module instanceof TransactionDrivenRuntimeModule) {
            transactionDrivenModuleManager.registerModule((TransactionDrivenRuntimeModule) module);
        }
    }


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
    public final synchronized void start(boolean skipInitialization) {
        if (State.STARTED.equals(state)) {
            if (LOG.isTraceEnabled()) {
                LOG.trace("GraphAware already started");
            }
            return;
        }

        if (State.STARTING.equals(state)) {
            throw new IllegalStateException("Attempt to start GraphAware from multiple different threads. This is a bug");
        }

        LOG.info("Starting GraphAware...");
        state = State.STARTING;

        if (skipInitialization) {
            LOG.info("Initialization skipped.");
        } else {
            LOG.info("Initializing modules...");
            try (Transaction tx = startTransaction()) {
                initializeModules();
                tx.success();
            }
            LOG.info("Modules initialized.");
        }

        state = State.STARTED;
        LOG.info("GraphAware started.");
    }

    /**
     * Register itself as transaction (and kernel) event handler. Should be called in the constructor of implementations.
     */
    protected abstract void registerSelfAsHandler();

    /**
     * Find out whether the database is available and ready for use.
     *
     * @return true iff the database is ready.
     */
    protected abstract boolean databaseAvailable();

    /**
     * {@inheritDoc}
     */
    @Override
    public final Void beforeCommit(TransactionData data) throws Exception {
        if (runtimeHasNotYetInitialised()) {
            return null;
        }

        transactionDrivenModuleManager.check(data);

        LazyTransactionData lazyTransactionData = new LazyTransactionData(data);

        transactionDrivenModuleManager.beforeCommit(lazyTransactionData);

        return null;
    }

    /**
     * Checks to see if initialisation of this {@link GraphAwareRuntime} has <b>NOT</b> been completed, starting the initialisation
     * process if necessary.
     *
     * @return <code>true</code> if any of the prerequisites haven't been satisfied, <code>false</code> if it's alright to
     *         delegate onto modules
     */
    protected boolean runtimeHasNotYetInitialised() {
        if (!databaseAvailable()) {
            return true;
        }

        //todo: is this a bottleneck? all transactions arrive here!
        synchronized (this) {
            switch (state) {
                case NONE:
                    start();
                    break;
                case STARTING:
                    return true;
                case STARTED:
                    break;
                default:
                    throw new IllegalStateException("Unknown GraphAware Runtime state. This is a bug.");
            }
        }

        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void afterCommit(TransactionData data, Void state) {
        //do nothing for now
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void afterRollback(TransactionData data, Void state) {
        //do nothing for now
    }

    /**
     * Start a database transaction.
     *
     * @return tx.
     */
    protected abstract Transaction startTransaction();

    /**
     * Initialize modules if needed.
     * <p/>
     * Metadata about modules is stored as properties on the GraphAware Runtime Root Node in the form of
     * {@link com.graphaware.runtime.config.RuntimeConfiguration#GA_PREFIX}{@link #RUNTIME}_{@link TransactionDrivenRuntimeModule#getId()}
     * as key and one of the following as value:
     * - {@link #CONFIG} + {@link TransactionDrivenRuntimeModule#getConfiguration()} (serialized) capturing the last configuration
     * the module has been run with
     * - {@link #FORCE_INITIALIZATION} + timestamp indicating the module should be re-initialized.
     */
    protected void initializeModules() {
        transactionDrivenModuleManager.initializeModules();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void beforeShutdown() {
        LOG.info("Shutting down GraphAware Runtime... ");
        shutdownModules();
        shutdownRuntime();
        LOG.info("GraphAware Runtime shut down.");
    }

    protected void shutdownModules() {
        transactionDrivenModuleManager.shutdownModules();
    }

    /**
     * Perform any last-minute operations required in order to cleanly shut down this {@link GraphAwareRuntime}.
     */
    protected void shutdownRuntime() {
        // don't do anything by default
    }

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
