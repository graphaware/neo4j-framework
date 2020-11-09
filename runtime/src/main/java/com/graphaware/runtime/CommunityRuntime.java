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
import com.graphaware.runtime.module.DeliberateTransactionRollbackException;
import com.graphaware.runtime.module.Module;
import com.graphaware.tx.event.improved.api.FilteredTransactionData;
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

import java.util.HashMap;
import java.util.LinkedHashMap;
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
        DESTROYED,
        FAILED;
    }

    private volatile State state;

    private final GraphDatabaseService database;
    private final DatabaseManagementService databaseManagementService;
    private final Map<String, Module> modules = new LinkedHashMap<>();

    public CommunityRuntime(GraphDatabaseService database, DatabaseManagementService databaseManagementService) {
        this.state = State.FRESH;
        this.database = database;
        this.databaseManagementService = databaseManagementService;

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

        checkNotAlreadyRegistered(module);

        modules.put(module.getId(), module);
    }

    /**
     * Check that the given module isn't already registered with the manager.
     *
     * @param module to check.
     * @throws IllegalStateException in case the module is already registered.
     */
    private void checkNotAlreadyRegistered(Module module) {
        if (modules.containsValue(module)) {
            LOG.error("Module " + module.getId() + " cannot be registered more than once!");
            throw new IllegalStateException("Module " + module.getId() + " cannot be registered more than once!");
        }

        if (modules.containsKey(module.getId())) {
            LOG.error("Module " + module.getId() + " cannot be registered more than once!");
            throw new IllegalStateException("Module " + module.getId() + " cannot be registered more than once!");
        }
    }

    @Override
    public final synchronized void start() {
        if (State.STARTED.equals(state)) {
            LOG.warn("GraphAware Runtime already started for database " + database.databaseName() + ".");
            return;
        }

        if (State.STARTING.equals(state)) {
            state = State.FAILED;
            throw new IllegalStateException("Attempt to start GraphAware Runtime for database " + database.databaseName() + " from multiple different threads. This is a bug.");
        }

        if (State.STOPPING.equals(state)) {
            state = State.FAILED;
            throw new IllegalStateException("Attempt to start GraphAware Runtime for database " + database.databaseName() + " while it is stopping. This is a bug.");
        }

        if (State.DESTROYED.equals(state)) {
            state = State.FAILED;
            throw new IllegalStateException("Attempt to start GraphAware Runtime for database " + database.databaseName() + " after it has been destroyed. Please create a fresh Runtime.");
        }

        if (State.FAILED.equals(state)) {
            throw new IllegalStateException("Failed GraphAware Runtime state. Please restart Neo4j.");
        }

        if (!State.FRESH.equals(state)) {
            state = State.FAILED;
            throw new IllegalStateException("Illegal GraphAware Runtime state " + state + "! This is a bug");
        }

        STARTING.set(true);
        LOG.info("Starting GraphAware Runtime for database " + database.databaseName() + "...");
        state = State.STARTING;

        try {
            startModules();

            state = State.STARTED;
            LOG.info("Started GraphAware Runtime for database " + database.databaseName() + ".");
        } catch (Throwable throwable) {
            state = State.FAILED;
            LOG.error("Failed starting GraphAware Runtime for database " + database.databaseName() + ".", throwable);
        }

        STARTING.set(false);
    }

    protected void startModules() {
        if (modules.isEmpty()) {
            LOG.info("No GraphAware Runtime modules registered for database " + database.databaseName() + ".");
            return;
        }

        LOG.info("Starting GraphAware Runtime modules for database " + database.databaseName() + "...");
        for (Module<?> module : modules.values()) {
            LOG.info("Starting module " + module.getId() + " for database " + database.databaseName() + "...");
            module.start(this);
            LOG.info("Started module " + module.getId() + " for database " + database.databaseName() + ".");
        }
        LOG.info("GraphAware Runtime modules started for database " + database.databaseName() + ".");
    }

    @Override
    public void stop() {
        switch (state) {
            case FAILED:
                LOG.warn("GraphAware Runtime for database " + database.databaseName() + " has failed starting.");
                break;
            case FRESH:
                LOG.warn("GraphAware Runtime for database " + database.databaseName() + " hasn't even been started.");
                break;
            case DESTROYED:
                LOG.warn("GraphAware Runtime for database " + database.databaseName() + " has already been destroyed.");
                break;
            default:
                LOG.info("Stopping GraphAware Runtime for database " + database.databaseName() + "...");

                state = State.STOPPING;

                stopModules();

                LOG.info("Stopped GraphAware Runtime for database " + database.databaseName() + ".");

                state = State.DESTROYED;
        }

        databaseManagementService.unregisterTransactionEventListener(database.databaseName(), this);
        databaseManagementService.unregisterDatabaseEventListener(this);
    }

    protected void stopModules() {
        for (Module<?> module : modules.values()) {
            LOG.info("Stopping module " + module.getId() + " for database " + database.databaseName() + "...");
            module.shutdown();
            LOG.info("Stopped module " + module.getId() + " for database " + database.databaseName() + ".");
        }
    }

    @Override
    public Map<String, Object> beforeCommit(TransactionData data, Transaction transaction, GraphDatabaseService databaseService) {
        if (!isStarted()) {
            return null;
        }

        LazyTransactionData transactionData = new LazyTransactionData(data, transaction);

        Map<String, Object> result = new HashMap<>();

        for (Module<?> module : modules.values()) {
            FilteredTransactionData filteredTransactionData = new FilteredTransactionData(transactionData, transactionData.getTransaction(), module.getConfiguration().getInclusionPolicies());

            if (!filteredTransactionData.mutationsOccurred()) {
                continue;
            }

            Object state = null;

            try {
                state = module.beforeCommit(filteredTransactionData);
            } catch (DeliberateTransactionRollbackException e) {
                LOG.debug("Module " + module.getId() + " threw an exception indicating that the transaction should be rolled back.", e);
                return handleException(data, result, module, state, e);
            } catch (RuntimeException e) {
                LOG.warn("Module " + module.getId() + " threw an exception", e);
                return handleException(data, result, module, state, e);
            }

            result.put(module.getId(), state);
        }

        return result;
    }

    private Map<String, Object> handleException(TransactionData data, Map<String, Object> result, Module<?> module, Object state, RuntimeException e) {
        result.put(module.getId(), state);      //just so the module gets afterRollback called as well
        afterRollback(data, result, database); //remove this when https://github.com/neo4j/neo4j/issues/2660 is resolved (todo this is fixed in 3.3)
        throw e;               //will cause rollback
    }

    @Override
    public void afterCommit(TransactionData data, Map<String, Object> states, GraphDatabaseService databaseService) {
        if (states == null) {
            return;
        }

        for (Module module : modules.values()) {
            if (!states.containsKey(module.getId())) {
                return; //perhaps module wasn't interested, or threw RuntimeException
            }

            module.afterCommit(states.get(module.getId()));
        }
    }

    @Override
    public void afterRollback(TransactionData data, Map<String, Object> states, GraphDatabaseService databaseService) {
        if (states == null) {
            return;
        }

        for (Module module : modules.values()) {
            if (!states.containsKey(module.getId())) {
                return; //rollback happened before this module had a go
            }

            module.afterRollback(states.get(module.getId()));
        }
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

        if (State.FAILED.equals(state)) {
            throw new IllegalStateException("Runtime for database " + database.databaseName() + " has failed.");
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
        if (!modules.containsKey(moduleId)) {
            throw new NotFoundException("No module of type " + clazz.getName() + " with ID " + moduleId + " has been registered");
        }

        Module module = modules.get(moduleId);
        if (!clazz.isAssignableFrom(module.getClass())) {
            LOG.warn("Module " + moduleId + " is not a " + clazz.getName());
            throw new NotFoundException("No module of type " + clazz.getName() + " with ID " + moduleId + " has been registered");
        }

        return (M) module;
    }

    @Override
    public <M extends Module<?>> M getModule(Class<M> clazz) throws NotFoundException {
        M result = null;
        for (Module module : modules.values()) {
            if (clazz.isAssignableFrom(module.getClass())) {
                if (result != null) {
                    throw new IllegalStateException("More than one module of type " + clazz + " has been registered");
                }
                result = (M) module;
            }
        }

        if (result == null) {
            throw new NotFoundException("No module of type " + clazz.getName() + " has been registered");
        }

        return result;
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
