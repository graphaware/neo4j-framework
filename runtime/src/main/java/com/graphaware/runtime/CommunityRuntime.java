/*
 * Copyright (c) 2013-2020 GraphAware
 *
 * This file is part of the GraphAware Framework.
 *
 * GraphAware Framework is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received a copy of
 * the GNU General Public License along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 */

package com.graphaware.runtime;

import com.graphaware.common.log.LoggerFactory;
import com.graphaware.runtime.config.RuntimeConfiguration;
import com.graphaware.runtime.manager.ModuleManager;
import com.graphaware.runtime.module.Module;
import com.graphaware.tx.event.improved.api.ImprovedTransactionData;
import com.graphaware.tx.event.improved.api.LazyTransactionData;
import org.neo4j.dbms.api.DatabaseManagementService;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.NotFoundException;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.event.DatabaseEventContext;
import org.neo4j.graphdb.event.DatabaseEventListener;
import org.neo4j.graphdb.event.TransactionData;
import org.neo4j.graphdb.event.TransactionEventListener;
import org.neo4j.logging.Log;

import java.util.Map;
import java.util.concurrent.TimeUnit;


/**
 * {@link GraphAwareRuntime} intended for Community production use.
 * <p>
 * Supports {@link Module} {@link Module}s.
 * <p>
 * To use this {@link GraphAwareRuntime}, construct it using {@link GraphAwareRuntimeFactory}.
 */
public class CommunityRuntime implements TransactionEventListener<Map<String, Object>>, GraphAwareRuntime, DatabaseEventListener {

    private static final Log LOG = LoggerFactory.getLogger(CommunityRuntime.class);

    private static final ThreadLocal<Boolean> STARTING = ThreadLocal.withInitial(() -> false);

    private enum State {
        NONE,
        REGISTERED,
        STARTING,
        STARTED,
        SHUTDOWN;
    }

    private volatile State state = State.NONE;

    private final RuntimeConfiguration configuration;
    private final GraphDatabaseService database;
    private final DatabaseManagementService databaseManagementService;
    private final ModuleManager runtimeModuleManager;

    /**
     * Construct a new runtime. Protected, please use {@link GraphAwareRuntimeFactory}.
     *
     * @param configuration config.
     * @param database      on which the runtime operates.
     * @param moduleManager manager for transaction-driven modules.
     */
    protected CommunityRuntime(RuntimeConfiguration configuration, GraphDatabaseService database, DatabaseManagementService databaseManagementService, ModuleManager moduleManager) {
        if (!State.NONE.equals(state)) {
            throw new IllegalStateException("Only one instance of the GraphAware Runtime should ever be instantiated and started.");
        }

        if (RuntimeRegistry.getRuntime(database.databaseName()) != null) {
            throw new IllegalStateException("It is not possible to create multiple runtimes for a single database!");
        }

        this.state = State.REGISTERED;
        this.configuration = configuration;
        this.database = database;
        this.databaseManagementService = databaseManagementService;
        this.runtimeModuleManager = moduleManager;

        databaseManagementService.registerTransactionEventListener(database.databaseName(),this);
        databaseManagementService.registerDatabaseEventListener(this);

        RuntimeRegistry.registerRuntime(database.databaseName(), this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeSelf() {
        stop();

        databaseManagementService.unregisterTransactionEventListener(database.databaseName(),this);
        databaseManagementService.unregisterDatabaseEventListener(this);

        RuntimeRegistry.unregisterRuntime(database.databaseName(), this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void registerModule(Module module) {
        if (!State.REGISTERED.equals(state)) {
            LOG.error("Modules must be registered before GraphAware Runtime is started!");
            throw new IllegalStateException("Modules must be registered before GraphAware Runtime is started!");
        }

        LOG.info("Registering module " + module.getId() + " with GraphAware Runtime.");

        runtimeModuleManager.checkNotAlreadyRegistered(module);

        runtimeModuleManager.registerModule(module);
    }

    @Override
    public void databaseStart(DatabaseEventContext eventContext) {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final synchronized void start() {
        if (State.STARTED.equals(state)) {
            LOG.debug("GraphAware Runtime already started");
            return;
        }

        if (State.STARTING.equals(state)) {
            throw new IllegalStateException("Attempt to start GraphAware Runtime from multiple different threads. This is a bug");
        }

        if (!State.REGISTERED.equals(state)) {
            throw new IllegalStateException("Illegal GraphAware Runtime state " + state + "! This is a bug");
        }

        STARTING.set(true);
        LOG.info("Starting GraphAware Runtime...");
        state = State.STARTING;

        beforeStart();

        startModules();

        state = State.STARTED;
        LOG.info("GraphAware Runtime started.");
        STARTING.set(false);
    }

    /**
     * Start the modules.
     */
    private void startModules() {
        runtimeModuleManager.startModules();
    }

    /**
     * Start whatever subclasses want to start.
     */
    protected void beforeStart() {

    }

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
     * {@inheritDoc}
     */
    @Override
    public void stop() {
        if (State.SHUTDOWN.equals(state)) {
            return;
        }

        LOG.info("Stopping GraphAware Runtime... ");

        state = State.SHUTDOWN;

        doStop();

        LOG.info("GraphAware Runtime stopped.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Object> beforeCommit(TransactionData data, Transaction transaction, GraphDatabaseService databaseService) throws Exception {
        LazyTransactionData transactionData = new LazyTransactionData(data, transaction);

        if (!isStarted(transactionData)) {
            return null;
        }

        return runtimeModuleManager.beforeCommit(transactionData);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void afterCommit(TransactionData data, Map<String, Object> state, GraphDatabaseService databaseService) {
        if (state == null) {
            return;
        }

        runtimeModuleManager.afterCommit(state);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void afterRollback(TransactionData data, Map<String, Object> state, GraphDatabaseService databaseService) {
        if (state == null) {
            return;
        }

        runtimeModuleManager.afterRollback(state);
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
     * <code>false</code> iff the runtime isn't started but it is safe to proceed.
     * @throws IllegalStateException in case the runtime hasn't been started at all.
     */
    private boolean isStarted(ImprovedTransactionData transactionData) {
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

            if (State.STARTING.equals(state) && STARTING.get()) {
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
    public void databaseShutdown(DatabaseEventContext eventContext) {
        LOG.info("Shutting down GraphAware Runtime... ");

        stop();

        RuntimeRegistry.removeRuntime(eventContext.getDatabaseName());

        LOG.info("GraphAware Runtime shut down.");
    }

    protected void doStop() {
        runtimeModuleManager.shutdownModules();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <M extends Module> M getModule(String moduleId, Class<M> clazz) throws NotFoundException {
        M module = runtimeModuleManager.getModule(moduleId, clazz);

        if (module != null) {
            return module;
        }

        throw new NotFoundException("No module of type " + clazz.getName() + " with ID " + moduleId + " has been registered");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <M extends Module> M getModule(Class<M> clazz) throws NotFoundException {
        M txResult = runtimeModuleManager.getModule(clazz);

        if (txResult == null) {
            throw new NotFoundException("No module of type " + clazz.getName() + " has been registered");
        }

        return txResult;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void databasePanic(DatabaseEventContext eventContext) {
        //do nothing
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RuntimeConfiguration getConfiguration() {
        return configuration;
    }
}
