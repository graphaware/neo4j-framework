/*
 * Copyright (c) 2013-2019 GraphAware
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
import com.graphaware.runtime.manager.TimerDrivenModuleManager;
import com.graphaware.runtime.manager.TxDrivenModuleManager;
import com.graphaware.runtime.module.RuntimeModule;
import com.graphaware.runtime.module.TimerDrivenModule;
import com.graphaware.runtime.module.TxDrivenModule;
import com.graphaware.tx.event.improved.api.ImprovedTransactionData;
import com.graphaware.tx.event.improved.api.LazyTransactionData;
import com.graphaware.writer.neo4j.Neo4jWriter;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.NotFoundException;
import org.neo4j.graphdb.event.ErrorState;
import org.neo4j.graphdb.event.KernelEventHandler;
import org.neo4j.graphdb.event.TransactionData;
import org.neo4j.graphdb.event.TransactionEventHandler;
import org.neo4j.logging.Log;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;


/**
 * {@link GraphAwareRuntime} intended for Community production use.
 * <p>
 * Supports both {@link TimerDrivenModule} and {@link TxDrivenModule} {@link RuntimeModule}s.
 * <p>
 * To use this {@link GraphAwareRuntime}, construct it using {@link GraphAwareRuntimeFactory}.
 */
public class CommunityRuntime implements TransactionEventHandler<Map<String, Object>>, GraphAwareRuntime, KernelEventHandler {

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
    private final TxDrivenModuleManager<TxDrivenModule> txDrivenModuleManager;
    private final TimerDrivenModuleManager timerDrivenModuleManager;
    private final Neo4jWriter writer;

    /**
     * Construct a new runtime. Protected, please use {@link GraphAwareRuntimeFactory}.
     *
     * @param configuration            config.
     * @param database                 on which the runtime operates.
     * @param txDrivenModuleManager    manager for transaction-driven modules.
     * @param timerDrivenModuleManager manager for timer-driven modules.
     * @param writer                   to use when writing to the database.
     */
    protected CommunityRuntime(RuntimeConfiguration configuration, GraphDatabaseService database, TxDrivenModuleManager<TxDrivenModule> txDrivenModuleManager, TimerDrivenModuleManager timerDrivenModuleManager, Neo4jWriter writer) {
        if (!State.NONE.equals(state)) {
            throw new IllegalStateException("Only one instance of the GraphAware Runtime should ever be instantiated and started.");
        }

        if (RuntimeRegistry.getRuntime(database) != null) {
            throw new IllegalStateException("It is not possible to create multiple runtimes for a single database!");
        }

        this.state = State.REGISTERED;
        this.configuration = configuration;
        this.database = database;
        this.txDrivenModuleManager = txDrivenModuleManager;
        this.timerDrivenModuleManager = timerDrivenModuleManager;
        this.writer = writer;

        database.registerTransactionEventHandler(this);
        database.registerKernelEventHandler(this);

//        // Register to topology change events
//        // In Community Edition this raises a ClassNotFoundException
//        try {
//            this.topologyListenerAdapter = new TopologyListenerAdapter((GraphDatabaseAPI) database, configuration.kernelConfig());
//        } catch (Exception exception) {
//            LOG.warn("Failed to register topology listener", exception);
//        }

        RuntimeRegistry.registerRuntime(database, this);
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

        txDrivenModuleManager.checkNotAlreadyRegistered(module);
        timerDrivenModuleManager.checkNotAlreadyRegistered(module);

        if (module instanceof TxDrivenModule) {
            txDrivenModuleManager.registerModule((TxDrivenModule) module);
        }

//        // If the module is a TopologyChangeEventListener then we should register this module as a listener
//        if (this.topologyListenerAdapter != null && module instanceof TopologyChangeEventListener) {
//            this.topologyListenerAdapter.registerListener((TopologyChangeEventListener) module);
//        }

        if (module instanceof TimerDrivenModule) {
            timerDrivenModuleManager.registerModule((TimerDrivenModule) module);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final synchronized void start() {
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

        STARTING.set(true);
        LOG.info("Starting GraphAware...");
        state = State.STARTING;

        startStatsCollector();
        startModules();
        startWriter();

        state = State.STARTED;
        LOG.info("GraphAware started.");
        STARTING.set(false);
    }

    /**
     * Start stats collector.
     */
    private void startStatsCollector() {
        configuration.getStatsCollector().runtimeStart();
    }

    /**
     * Perform the actual start of the runtime, being certain that it is the right time to do so.
     */
    private void startModules() {
        LOG.info("Loading module metadata...");

        Set<String> moduleIds = new HashSet<>();
        moduleIds.addAll(txDrivenModuleManager.loadMetadata());
        moduleIds.addAll(timerDrivenModuleManager.loadMetadata());

        txDrivenModuleManager.cleanupMetadata(moduleIds);
        timerDrivenModuleManager.cleanupMetadata(moduleIds);

        LOG.info("Module metadata loaded.");

        txDrivenModuleManager.startModules();
        timerDrivenModuleManager.startModules();
    }

    /**
     * Start the database writer.
     */
    private void startWriter() {
        getDatabaseWriter().start();
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
    public Map<String, Object> beforeCommit(TransactionData data) {
        LazyTransactionData transactionData = new LazyTransactionData(data);

        if (!isStarted(transactionData)) {
            return null;
        }

        return txDrivenModuleManager.beforeCommit(transactionData);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void afterCommit(TransactionData data, Map<String, Object> states) {
        if (states == null) {
            return;
        }

        txDrivenModuleManager.afterCommit(states);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void afterRollback(TransactionData data, Map<String, Object> states) {
        if (states == null) {
            return;
        }

        txDrivenModuleManager.afterRollback(states);
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
    public final void beforeShutdown() {
        LOG.info("Shutting down GraphAware Runtime... ");

        state = State.SHUTDOWN;

        txDrivenModuleManager.shutdownModules();
        timerDrivenModuleManager.shutdownModules();
        getDatabaseWriter().stop();

        RuntimeRegistry.removeRuntime(database);

//        // Remove all listeners and un-register adapter
//        if (this.topologyListenerAdapter != null) {
//            this.topologyListenerAdapter.unregister();
//            this.topologyListenerAdapter = null;
//        }

        LOG.info("GraphAware Runtime shut down.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <M extends RuntimeModule> M getModule(String moduleId, Class<M> clazz) throws NotFoundException {
        M module = txDrivenModuleManager.getModule(moduleId, clazz);

        if (module != null) {
            return module;
        }

        module = timerDrivenModuleManager.getModule(moduleId, clazz);

        if (module != null) {
            return module;
        }

        throw new NotFoundException("No module of type " + clazz.getName() + " with ID " + moduleId + " has been registered");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <M extends RuntimeModule> M getModule(Class<M> clazz) throws NotFoundException {
        M txResult = txDrivenModuleManager.getModule(clazz);
        M timerResult = timerDrivenModuleManager.getModule(clazz);

        if (txResult != null && timerResult != null && timerResult != txResult) {
            throw new IllegalStateException("More than one module of type " + clazz + " has been registered");
        }

        if (txResult == null && timerResult == null) {
            throw new NotFoundException("No module of type " + clazz.getName() + " has been registered");
        }

        return txResult == null ? timerResult : txResult;
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
    public Neo4jWriter getDatabaseWriter() {
        return writer;
    }
}
