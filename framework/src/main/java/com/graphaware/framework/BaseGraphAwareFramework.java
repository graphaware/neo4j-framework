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

package com.graphaware.framework;

import com.graphaware.common.strategy.InclusionStrategy;
import com.graphaware.framework.config.DefaultFrameworkConfiguration;
import com.graphaware.framework.config.FrameworkConfiguration;
import com.graphaware.framework.config.FrameworkConfigured;
import com.graphaware.tx.event.improved.api.FilteredTransactionData;
import com.graphaware.tx.event.improved.api.LazyTransactionData;
import com.graphaware.common.util.PropertyContainerUtils;
import org.apache.log4j.Logger;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.NotFoundException;
import org.neo4j.graphdb.event.ErrorState;
import org.neo4j.graphdb.event.KernelEventHandler;
import org.neo4j.graphdb.event.TransactionData;
import org.neo4j.graphdb.event.TransactionEventHandler;

import java.util.*;


/**
 * Framework that delegates to registered {@link GraphAwareModule}s to perform useful work.
 * There must be exactly one instance of this framework for a single {@link org.neo4j.graphdb.GraphDatabaseService}.
 * <p/>
 * The framework registers itself as a Neo4j {@link org.neo4j.graphdb.event.TransactionEventHandler},
 * translates {@link org.neo4j.graphdb.event.TransactionData} into {@link com.graphaware.tx.event.improved.api.ImprovedTransactionData}
 * and lets registered {@link GraphAwareModule}s deal with the data before each transaction
 * commits, in the order the modules were registered.
 * <p/>
 * After all desired modules have been registered, {@link #start()} must be called before anything gets written into the
 * database. No more modules can be registered thereafter.
 * <p/>
 * Every new {@link GraphAwareModule} and every {@link GraphAwareModule}
 * whose configuration has changed since the last run will be forced to (re-)initialize, which can lead to very long
 * startup times, as (re-)initialization could be a global graph operation. Re-initialization will also be automatically
 * performed for all modules, for which it has been detected that something is out-of-sync
 * (module threw a {@link NeedsInitializationException}).
 * <p/>
 * The root node (node with ID = 0) needs to be present in the database in order for this framework to work. It does not
 * need to be used by the application, nor does it need to be connected to any other node, but it needs to be present
 * in the database.
 */
public abstract class BaseGraphAwareFramework implements TransactionEventHandler<Void>, KernelEventHandler {
    static final String FORCE_INITIALIZATION = "FORCE_INIT:";
    static final String CONFIG = "CONFIG:";

    public static final String CORE = "CORE";

    private static final Logger LOG = Logger.getLogger(BaseGraphAwareFramework.class);

    private final FrameworkConfiguration configuration;
    private final List<GraphAwareModule> modules = new LinkedList<>();

    private volatile boolean started;

    /**
     * Create a new instance of the framework with {@link DefaultFrameworkConfiguration}.
     */
    public BaseGraphAwareFramework() {
        this(DefaultFrameworkConfiguration.getInstance());
    }

    /**
     * Create a new instance of the framework.
     *
     * @param configuration of the framework.
     */
    public BaseGraphAwareFramework(FrameworkConfiguration configuration) {
        this.configuration = configuration;
    }

    /**
     * Register a {@link GraphAwareModule}. Note that modules are delegated to in the order
     * they are registered.
     *
     * @param module to register.
     */
    public final synchronized void registerModule(GraphAwareModule module) {
        registerModule(module, false);
    }

    /**
     * Register a {@link GraphAwareModule} and optionally force its (re-)initialization.
     * <p/>
     * Forcing re-initialization should only be necessary in exceptional circumstances, such as that the database has
     * been written to without the module being registered / framework running. Re-initialization can be a very
     * expensive, graph-global operation, should only be run once, database stopped and started again without forcing
     * re-initialization.
     * <p/>
     * New modules and modules with changed configuration will be (re-)initialized automatically; there is no need to use
     * this method for that purpose.
     * <p/>
     * Note that modules are delegated to in the order they are registered.
     *
     * @param module              to register.
     * @param forceInitialization true to force (re-)initialization.
     */
    public final synchronized void registerModule(GraphAwareModule module, boolean forceInitialization) {
        if (started) {
            LOG.error("Modules must be registered before GraphAware is started!");
            throw new IllegalStateException("Modules must be registered before GraphAware is started!");
        }

        LOG.info("Registering module " + module.getId() + " with GraphAware.");
        checkNotAlreadyRegistered(module);
        modules.add(module);

        if (module instanceof FrameworkConfigured) {
            ((FrameworkConfigured) module).configurationChanged(configuration);
        }

        if (forceInitialization) {
            LOG.info("Forcing module " + module.getId() + " to be initialized.");
            forceInitialization(module);
        }
    }

    /**
     * Check that the given module isn't already registered with the framework.
     *
     * @param module to check.
     * @throws IllegalStateException in case the module is already registered.
     */
    private void checkNotAlreadyRegistered(GraphAwareModule module) {
        if (modules.contains(module)) {
            throw new IllegalStateException("Module " + module.getId() + " cannot be registered more than once!");
        }

        for (GraphAwareModule existing : modules) {
            if (existing.getId().equals(module.getId())) {
                throw new IllegalStateException("Module " + module.getId() + " cannot be registered more than once!");
            }
        }
    }

    /**
     * Start the framework. Must be called before anything gets written into the database.
     */
    public final synchronized void start() {
        start(false);
    }

    /**
     * Start the framework, optionally skipping the initialization phase. It is not recommended to skip initialization;
     * un-initialized modules might not behave correctly.
     *
     * @param skipInitialization true for skipping initialization.
     */
    public final synchronized void start(boolean skipInitialization) {
        if (started) {
            LOG.error("GraphAware already started!");
            throw new IllegalStateException("GraphAware already started!");
        }

        LOG.info("Starting GraphAware...");

        if (skipInitialization) {
            LOG.info("Initialization skipped.");
        } else {
            LOG.info("Initializing modules...");
            initializeModules();
            LOG.info("Modules initialized.");
        }

        registerSelfAsHandler();

        started = true;

        LOG.info("GraphAware started.");
    }

    /**
     * Register itself as transaction (and kernel) event handler.
     */
    protected abstract void registerSelfAsHandler();

    /**
     * {@inheritDoc}
     */
    @Override
    public final Void beforeCommit(TransactionData data) throws Exception {
        if (!started) {
            return null;
        }

        if (data.isDeleted(getRoot())) {
            throw new IllegalStateException("Deleting node with ID=0 is not allowed by GraphAware!");
        }

        LazyTransactionData lazyTransactionData = new LazyTransactionData(data);

        for (GraphAwareModule module : modules) {
            FilteredTransactionData filteredTransactionData = new FilteredTransactionData(lazyTransactionData, module.getInclusionStrategies());

            if (!filteredTransactionData.mutationsOccurred()) {
                continue;
            }

            try {
                module.beforeCommit(filteredTransactionData);
            } catch (NeedsInitializationException e) {
                LOG.warn("Module " + module.getId() + " seems to have a problem and will be re-initialized next time the database is started. ");
                if (!findRootOrThrowException().getProperty(moduleKey(module)).toString().startsWith(FORCE_INITIALIZATION)) {
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
     * Initialize modules if needed.
     * <p/>
     * Metadata about modules is stored as properties on the root node (node with ID = 0) in the form of
     * {@link com.graphaware.framework.config.FrameworkConfiguration#GA_PREFIX}{@link #CORE}_{@link GraphAwareModule#getId()}
     * as key and one of the following as value:
     * - {@link #CONFIG} + {@link GraphAwareModule#asString()} capturing the last configuration
     * the module has been run with
     * - {@link #FORCE_INITIALIZATION} + timestamp indicating the module should be re-initialized.
     */
    private void initializeModules() {
        final Node root = findRootOrThrowException();
        final Map<String, Object> moduleMetadata = getInternalProperties(root);
        final Collection<String> unusedModules = new HashSet<>(moduleMetadata.keySet());

        for (final GraphAwareModule module : modules) {
            final String key = moduleKey(module);
            unusedModules.remove(key);

            if (!moduleMetadata.containsKey(key)) {
                LOG.info("Module " + module.getId() + " seems to have been registered for the first time, will initialize...");
                initializeModule(module);
                continue;

            }

            String value = (String) moduleMetadata.get(key);

            if (value.startsWith(CONFIG)) {
                if (!value.replaceFirst(CONFIG, "").equals(module.asString())) {
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
    }

    /**
     * Initialize a module and capture that fact on the as a root node's property.
     *
     * @param module to initialize.
     */
    private void initializeModule(final GraphAwareModule module) {
        doInitialize(module);
        recordInitialization(module);
    }

    /**
     * Initialize module.
     *
     * @param module to initialize.
     */
    protected abstract void doInitialize(GraphAwareModule module);

    /**
     * Re-initialize a module and capture that fact on the as a root node's property.
     *
     * @param module to initialize.
     */
    private void reinitializeModule(final GraphAwareModule module) {
        doReinitialize(module);
        recordInitialization(module);
    }

    /**
     * Re-initialize a module.
     *
     * @param module to initialize.
     */
    protected abstract void doReinitialize(GraphAwareModule module);

    /**
     * Capture the fact the a module has been (re-)initialized as a root node's property.
     *
     * @param module that has been initialized.
     */
    private void recordInitialization(final GraphAwareModule module) {
        final String key = moduleKey(module);
        doRecordInitialization(module, key);
    }

    /**
     * Capture the fact the a module has been (re-)initialized as a root node's property.
     *
     * @param module that has been initialized.
     * @param key    of the property to set.
     */
    protected abstract void doRecordInitialization(final GraphAwareModule module, final String key);

    /**
     * Remove unused modules.
     *
     * @param unusedModules to remove from the root node's properties.
     */
    protected abstract void removeUnusedModules(final Collection<String> unusedModules);

    /**
     * Get properties starting with {@link com.graphaware.framework.config.FrameworkConfiguration#GA_PREFIX} + {@link #CORE} from a node.
     *
     * @param node to get properties from.
     * @return map of properties (key-value).
     */
    private Map<String, Object> getInternalProperties(Node node) {
        return PropertyContainerUtils.propertiesToObjectMap(node, new InclusionStrategy<String>() {
            @Override
            public boolean include(String s) {
                return s.startsWith(configuration.createPrefix(CORE));
            }
        });
    }

    /**
     * Build a module key to use as a property on the root node for storing metadata.
     *
     * @param module to build a key for.
     * @return module key.
     */
    protected final String moduleKey(GraphAwareModule module) {
        return configuration.createPrefix(CORE) + module.getId();
    }

    /**
     * Force a module to be (re-)initialized next time the database (and framework) are started.
     *
     * @param module to be (re-)initialized next time.
     */
    protected abstract void forceInitialization(final GraphAwareModule module);

    /**
     * Find node with ID = 0, or throw an exception.
     *
     * @return root node.
     * @throws IllegalStateException if the node doesn't exist.
     */
    protected final Node findRootOrThrowException() {
        try {
            return getRoot();
        } catch (NotFoundException e) {
            throw new IllegalStateException("GraphAware Framework needs the root node (ID=0) for its operation. Please" +
                    " re-create the database and do not delete the root node. There is no need for it to be used in" +
                    " the application, but it must be present in the database.");
        }
    }

    /**
     * Get the root node.
     *
     * @return root node.
     * @throws org.neo4j.graphdb.NotFoundException
     *          if root not found.
     */
    protected abstract Node getRoot();

    /**
     * {@inheritDoc}
     */
    @Override
    public final void beforeShutdown() {
        LOG.info("Shutting down GraphAware... ");
        for (GraphAwareModule module : modules) {
            module.shutdown();
        }
        LOG.info("GraphAware shut down.");
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
