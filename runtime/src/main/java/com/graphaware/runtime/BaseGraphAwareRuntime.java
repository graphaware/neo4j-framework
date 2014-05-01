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

import com.graphaware.common.serialize.Serializer;
import com.graphaware.common.strategy.InclusionStrategy;
import com.graphaware.common.util.PropertyContainerUtils;
import com.graphaware.runtime.config.DefaultRuntimeConfiguration;
import com.graphaware.runtime.config.RuntimeConfiguration;
import com.graphaware.runtime.config.RuntimeConfigured;
import com.graphaware.tx.event.improved.api.FilteredTransactionData;
import com.graphaware.tx.event.improved.api.LazyTransactionData;
import org.apache.log4j.Logger;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.event.ErrorState;
import org.neo4j.graphdb.event.KernelEventHandler;
import org.neo4j.graphdb.event.TransactionData;

import java.util.*;

/**
 * Abstract base-class for {@link GraphAwareRuntime} implementations.
 */
public abstract class BaseGraphAwareRuntime implements GraphAwareRuntime {
    static final String FORCE_INITIALIZATION = "FORCE_INIT:";
    public static final String CONFIG = "CONFIG:";

    public static final String RUNTIME = "RUNTIME";

    private static final Logger LOG = Logger.getLogger(BaseGraphAwareRuntime.class);

    private final RuntimeConfiguration configuration;
    private final List<GraphAwareRuntimeModule> modules = new LinkedList<>();
    private final List<GraphAwareRuntimeModule> modulesToForce = new LinkedList<>();

    private State state = State.NONE;

    private enum State {
        NONE,
        STARTING,
        STARTED
    }

    /**
     * Create a new instance of the runtime with {@link com.graphaware.runtime.config.DefaultRuntimeConfiguration}.
     */
    public BaseGraphAwareRuntime() {
        this(DefaultRuntimeConfiguration.getInstance());
    }

    /**
     * Create a new instance of the runtime.
     *
     * @param configuration of the runtime.
     */
    public BaseGraphAwareRuntime(RuntimeConfiguration configuration) {
        this.configuration = configuration;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final synchronized void registerModule(GraphAwareRuntimeModule module) {
        registerModule(module, false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final synchronized void registerModule(GraphAwareRuntimeModule module, boolean forceInitialization) {
        if (!State.NONE.equals(state)) {
            LOG.error("Modules must be registered before GraphAware Runtime is started!");
            throw new IllegalStateException("Modules must be registered before GraphAware Runtime is started!");
        }

        LOG.info("Registering module " + module.getId() + " with GraphAware Runtime.");
        checkNotAlreadyRegistered(module);
        modules.add(module);

        if (module instanceof RuntimeConfigured) {
            ((RuntimeConfigured) module).configurationChanged(configuration);
        }

        if (forceInitialization) {
            LOG.info("Forcing module " + module.getId() + " to be initialized.");
            modulesToForce.add(module);
        }
    }

    /**
     * Check that the given module isn't already registered with the runtime.
     *
     * @param module to check.
     * @throws IllegalStateException in case the module is already registered.
     */
    private void checkNotAlreadyRegistered(GraphAwareRuntimeModule module) {
        if (modules.contains(module)) {
            throw new IllegalStateException("Module " + module.getId() + " cannot be registered more than once!");
        }

        for (GraphAwareRuntimeModule existing : modules) {
            if (existing.getId().equals(module.getId())) {
                throw new IllegalStateException("Module " + module.getId() + " cannot be registered more than once!");
            }
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
            initializeModules();
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
        if (!databaseAvailable()) {
            return null;
        }

        synchronized (this) {
            switch (state) {
                case NONE:
                    start();
                    break;
                case STARTING:
                    return null;
                case STARTED:
                    break;
                default:
                    throw new IllegalStateException("Unknown GraphAware Runtime state. This is a bug.");
            }
        }

        if (data.isDeleted(getOrCreateRoot())) {
            throw new IllegalStateException("Attempted to delete GraphAware Runtime root node!");
        }

        LazyTransactionData lazyTransactionData = new LazyTransactionData(data);

        for (GraphAwareRuntimeModule module : modules) {
            FilteredTransactionData filteredTransactionData = new FilteredTransactionData(lazyTransactionData, module.getConfiguration().getInclusionStrategies());

            if (!filteredTransactionData.mutationsOccurred()) {
                continue;
            }

            try {
                module.beforeCommit(filteredTransactionData);
            } catch (NeedsInitializationException e) {
                LOG.warn("Module " + module.getId() + " seems to have a problem and will be re-initialized next time the database is started. ");
                if (!getOrCreateRoot().getProperty(moduleKey(module)).toString().startsWith(FORCE_INITIALIZATION)) {
                    forceInitialization(module);
                }
            } catch (RuntimeException e) {
                LOG.warn("Module " + module.getId() + " threw an exception!", e);
            }
        }

        return null;
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
     * {@link com.graphaware.runtime.config.RuntimeConfiguration#GA_PREFIX}{@link #RUNTIME}_{@link GraphAwareRuntimeModule#getId()}
     * as key and one of the following as value:
     * - {@link #CONFIG} + {@link com.graphaware.runtime.GraphAwareRuntimeModule#getConfiguration()} (serialized) capturing the last configuration
     * the module has been run with
     * - {@link #FORCE_INITIALIZATION} + timestamp indicating the module should be re-initialized.
     */
    private void initializeModules() {
        try (Transaction tx = startTransaction()) {
            for (final GraphAwareRuntimeModule module : modulesToForce) {
                forceInitialization(module);
            }

            final Node root = getOrCreateRoot();
            final Map<String, Object> moduleMetadata = getInternalProperties(root);
            final Collection<String> unusedModules = new HashSet<>(moduleMetadata.keySet());

            for (final GraphAwareRuntimeModule module : modules) {
                final String key = moduleKey(module);
                unusedModules.remove(key);

                Serializer.register(module.getConfiguration().getClass());

                if (!moduleMetadata.containsKey(key)) {
                    LOG.info("Module " + module.getId() + " seems to have been registered for the first time, will initialize...");
                    initializeModule(module);
                    continue;

                }

                String value = (String) moduleMetadata.get(key);

                if (value.startsWith(CONFIG)) {
                    if (!value.equals(Serializer.toString(module.getConfiguration(), CONFIG))) {
                        LOG.info("Module " + module.getId() + " seems to have changed configuration since last run, will re-initialize...");
                        reinitializeModule(module);
                    } else {
                        LOG.info("Module " + module.getId() + " has not changed configuration since last run, already initialized.");
                    }
                    continue;
                }

                if (value.startsWith(FORCE_INITIALIZATION)) {
                    LOG.info("Module " + module.getId() + " has been marked for re-initialization on "
                            + new Date(Long.valueOf(value.replace(FORCE_INITIALIZATION, ""))).toString() + ". Will re-initialize...");
                    reinitializeModule(module);
                    continue;

                }

                LOG.fatal("Corrupted module info: " + value + " is not a valid value!");
                throw new IllegalStateException("Corrupted module info: " + value + " is not a valid value");
            }

            removeUnusedModules(unusedModules);

            tx.success();
        }
    }

    /**
     * Initialize a module and capture that fact on the as a root node's property.
     *
     * @param module to initialize.
     */
    private void initializeModule(final GraphAwareRuntimeModule module) {
        doInitialize(module);
        recordInitialization(module);
    }

    /**
     * Initialize module.
     *
     * @param module to initialize.
     */
    protected abstract void doInitialize(GraphAwareRuntimeModule module);

    /**
     * Re-initialize a module and capture that fact on the as a root node's property.
     *
     * @param module to initialize.
     */
    private void reinitializeModule(final GraphAwareRuntimeModule module) {
        doReinitialize(module);
        recordInitialization(module);
    }

    /**
     * Re-initialize a module.
     *
     * @param module to initialize.
     */
    protected abstract void doReinitialize(GraphAwareRuntimeModule module);

    /**
     * Capture the fact the a module has been (re-)initialized as a root node's property.
     *
     * @param module that has been initialized.
     */
    private void recordInitialization(final GraphAwareRuntimeModule module) {
        final String key = moduleKey(module);
        doRecordInitialization(module, key);
    }

    /**
     * Capture the fact the a module has been (re-)initialized as a root node's property.
     *
     * @param module that has been initialized.
     * @param key    of the property to set.
     */
    protected abstract void doRecordInitialization(final GraphAwareRuntimeModule module, final String key);

    /**
     * Remove unused modules.
     *
     * @param unusedModules to remove from the root node's properties.
     */
    protected abstract void removeUnusedModules(final Collection<String> unusedModules);

    /**
     * Get properties starting with {@link com.graphaware.runtime.config.RuntimeConfiguration#GA_PREFIX} + {@link #RUNTIME} from a node.
     *
     * @param node to get properties from.
     * @return map of properties (key-value).
     */
    private Map<String, Object> getInternalProperties(Node node) {
        return PropertyContainerUtils.propertiesToMap(node, new InclusionStrategy<String>() {
            @Override
            public boolean include(String s) {
                return s.startsWith(configuration.createPrefix(RUNTIME));
            }
        });
    }

    /**
     * Build a module key to use as a property on the root node for storing metadata.
     *
     * @param module to build a key for.
     * @return module key.
     */
    protected final String moduleKey(GraphAwareRuntimeModule module) {
        return configuration.createPrefix(RUNTIME) + module.getId();
    }

    /**
     * Force a module to be (re-)initialized next time the database (and runtime) are started.
     *
     * @param module to be (re-)initialized next time.
     */
    protected abstract void forceInitialization(final GraphAwareRuntimeModule module);

    /**
     * Get the root node.
     *
     * @return root node.
     * @throws org.neo4j.graphdb.NotFoundException
     *          if root not found.
     */
    protected abstract Node getOrCreateRoot();

    /**
     * {@inheritDoc}
     */
    @Override
    public final void beforeShutdown() {
        LOG.info("Shutting down GraphAware Runtime... ");
        for (GraphAwareRuntimeModule module : modules) {
            module.shutdown();
        }
        LOG.info("GraphAware Runtime shut down.");
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
