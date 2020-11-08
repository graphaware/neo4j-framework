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
import com.graphaware.runtime.manager.CommunityModuleManager;
import com.graphaware.runtime.manager.ModuleManager;
import com.graphaware.runtime.module.Module;
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
 */
public class CommunityRuntime implements TransactionEventListener<Map<String, Object>>, GraphAwareRuntime, DatabaseEventListener {

    private static final Log LOG = LoggerFactory.getLogger(CommunityRuntime.class);

    private static final ThreadLocal<Boolean> STARTING = ThreadLocal.withInitial(() -> false);

    private enum State {
        FRESH,
        STARTING,
        STARTED,
        STOPPING,
        DESTROYED;
    }

    private volatile State state;

    private final GraphDatabaseService database;
    private final DatabaseManagementService databaseManagementService;
    private final ModuleManager runtimeModuleManager;

    public CommunityRuntime(GraphDatabaseService database, DatabaseManagementService databaseManagementService) {
        this.state = State.FRESH;
        this.database = database;
        this.databaseManagementService = databaseManagementService;
        this.runtimeModuleManager = new CommunityModuleManager(database);

        databaseManagementService.registerTransactionEventListener(database.databaseName(), this);
        databaseManagementService.registerDatabaseEventListener(this);
    }

    @Override
    public GraphDatabaseService getDatabase() {
        return database;
    }

    @Override
    public synchronized void registerModule(Module<?> module) {
        if (!State.FRESH.equals(state)) {
            LOG.error("Modules must be registered before GraphAware Runtime is started!");
            throw new IllegalStateException("Modules must be registered before GraphAware Runtime is started!");
        }

        LOG.info("Registering module " + module.getId() + " with GraphAware Runtime for database " + database.databaseName() + ".");

        runtimeModuleManager.checkNotAlreadyRegistered(module);

        runtimeModuleManager.registerModule(module);
    }

    @Override
    public final synchronized void start() {
        if (State.STARTED.equals(state)) {
            LOG.warn("GraphAware Runtime already started for database " + database.databaseName() + ".");
            return;
        }

        if (State.STARTING.equals(state)) {
            throw new IllegalStateException("Attempt to start GraphAware Runtime for database " + database.databaseName() + " from multiple different threads. This is a bug.");
        }

        if (State.STOPPING.equals(state)) {
            throw new IllegalStateException("Attempt to start GraphAware Runtime for database " + database.databaseName() + " while it is stopping. This is a bug.");
        }

        if (State.DESTROYED.equals(state)) {
            throw new IllegalStateException("Attempt to start GraphAware Runtime for database " + database.databaseName() + " after it has been destroyed. Please create a fresh Runtime.");
        }

        if (!State.FRESH.equals(state)) {
            throw new IllegalStateException("Illegal GraphAware Runtime state " + state + "! This is a bug");
        }

        STARTING.set(true);
        LOG.info("Starting GraphAware Runtime for database " + database.databaseName() + "...");
        state = State.STARTING;

        runtimeModuleManager.startModules();

        state = State.STARTED;
        LOG.info("Started GraphAware Runtime for database " + database.databaseName() + ".");
        STARTING.set(false);
    }

    @Override
    public void stop() {
        switch (state) {
            case FRESH:
                LOG.warn("GraphAware Runtime for database " + database.databaseName() + " hasn't even been started.");
                break;
            case DESTROYED:
                LOG.warn("GraphAware Runtime for database " + database.databaseName() + " has already been destroyed.");
                break;
            default:
                LOG.info("Stopping GraphAware Runtime for database " + database.databaseName() + "...");

                state = State.STOPPING;

                runtimeModuleManager.stopModules();

                LOG.info("Stopped GraphAware Runtime for database " + database.databaseName() + ".");

                state = State.DESTROYED;
        }

        databaseManagementService.unregisterTransactionEventListener(database.databaseName(), this);
        databaseManagementService.unregisterDatabaseEventListener(this);
    }

    @Override
    public Map<String, Object> beforeCommit(TransactionData data, Transaction transaction, GraphDatabaseService databaseService) throws Exception {
        LazyTransactionData transactionData = new LazyTransactionData(data, transaction);

        if (!isStarted()) {
            return null;
        }

        return runtimeModuleManager.beforeCommit(transactionData);
    }

    @Override
    public void afterCommit(TransactionData data, Map<String, Object> state, GraphDatabaseService databaseService) {
        if (state == null) {
            return;
        }

        runtimeModuleManager.afterCommit(state);
    }

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
     * <li>it's starting but the caller is the thread that starts the runtime itself, in which case it returns <code>false</code></li>
     * </ul>
     *
     * @return <code>true</code> iff the runtime is started.
     * <code>false</code> iff the runtime isn't started but it is safe to proceed.
     * @throws IllegalStateException in case the runtime hasn't been started at all.
     */
    private boolean isStarted() {
        if (State.STOPPING.equals(state) || (State.DESTROYED.equals(state))) {
            throw new IllegalStateException("Runtime for database " + database.databaseName() + " is being / has been stopped.");
        }

        int attempts = 0;

        while (!State.STARTED.equals(state)) {
            if (State.STARTING.equals(state) && STARTING.get()) {
                return false;
            }

            try {
                attempts++;
                if (attempts > 100 && State.FRESH.equals(state)) {
                    throw new IllegalStateException("Runtime has not been started!");
                }
                TimeUnit.MILLISECONDS.sleep(10);
            } catch (InterruptedException e) {
                //just continue
            }
        }

        return true;
    }

    @Override
    public <M extends Module<?>> M getModule(String moduleId, Class<M> clazz) throws NotFoundException {
        M module = runtimeModuleManager.getModule(moduleId, clazz);

        if (module != null) {
            return module;
        }

        throw new NotFoundException("No module of type " + clazz.getName() + " with ID " + moduleId + " has been registered");
    }

    @Override
    public <M extends Module<?>> M getModule(Class<M> clazz) throws NotFoundException {
        M txResult = runtimeModuleManager.getModule(clazz);

        if (txResult == null) {
            throw new NotFoundException("No module of type " + clazz.getName() + " has been registered");
        }

        return txResult;
    }

    @Override
    public void databaseStart(DatabaseEventContext eventContext) {
        if (databaseApplies(eventContext)) {
            start();
        }
    }

    @Override
    public void databaseShutdown(DatabaseEventContext eventContext) {
        if (databaseApplies(eventContext)) {
            stop();
        }
    }

    private boolean databaseApplies(DatabaseEventContext eventContext) {
        return database.databaseName().equals(eventContext.getDatabaseName());
    }

    @Override
    public void databasePanic(DatabaseEventContext eventContext) {

    }
}
