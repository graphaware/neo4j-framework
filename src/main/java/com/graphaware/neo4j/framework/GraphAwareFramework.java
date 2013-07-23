package com.graphaware.neo4j.framework;

import com.graphaware.neo4j.framework.config.DefaultFrameworkConfiguration;
import com.graphaware.neo4j.framework.config.FrameworkConfiguration;
import com.graphaware.neo4j.framework.config.FrameworkConfigured;
import com.graphaware.neo4j.strategy.InclusionStrategy;
import com.graphaware.neo4j.tx.event.api.FilteredTransactionData;
import com.graphaware.neo4j.tx.event.api.LazyTransactionData;
import com.graphaware.neo4j.tx.single.SimpleTransactionExecutor;
import com.graphaware.neo4j.tx.single.TransactionCallback;
import com.graphaware.neo4j.utils.PropertyContainerUtils;
import org.apache.log4j.Logger;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.NotFoundException;
import org.neo4j.graphdb.event.TransactionData;
import org.neo4j.graphdb.event.TransactionEventHandler;

import java.util.*;


/**
 * Framework that delegates to registered {@link GraphAwareModule}s to perform useful work. There must be exactly one
 * instance of this framework for a single {@link GraphDatabaseService}.
 * <p/>
 * The framework registers itself as a Neo4j {@link TransactionEventHandler}, translates {@link TransactionData} into
 * {@link com.graphaware.neo4j.tx.event.api.ImprovedTransactionData} and lets registered {@link GraphAwareModule}s
 * deal with the data before each transaction commits, in the order the modules were registered.
 * <p/>
 * After all desired modules have been registered, {@link #start()} must be called before anything gets written into the
 * database. No more modules can be registered thereafter.
 * <p/>
 * Every new {@link GraphAwareModule} and every {@link GraphAwareModule} whose configuration has changed since the last
 * run will be forced to re-initialize, which can lead to very long initialization startup time, as (re-)initialization
 * could be a global graph operation. Re-initialization will also be automatically performed for all modules, for which
 * it has been detected that something is out-of-sync (module threw a {@link NeedsInitializationException}).
 * <p/>
 * The root node (node with ID = 0) needs to be present in the database in order for this framework to work. It does not
 * need to be used by the application, nor does it need to be connected to any other node, but it needs to be present
 * in the database.
 */
public final class GraphAwareFramework implements TransactionEventHandler<Void> {
    static final String FORCE_INITIALIZATION = "FORCE_INIT:";
    static final String HASH_CODE = "HASH_CODE:";

    private static final Logger LOG = Logger.getLogger(GraphAwareFramework.class);

    private final GraphDatabaseService database;
    private final FrameworkConfiguration configuration;
    private final List<GraphAwareModule> modules = new LinkedList<>();

    private volatile boolean started;

    /**
     * Create a new instance of the framework.
     *
     * @param database on which the framework should operate.
     */
    public GraphAwareFramework(GraphDatabaseService database) {
        this(database, DefaultFrameworkConfiguration.getInstance());
    }

    /**
     * Create a new instance of the framework.
     *
     * @param database      on which the framework should operate.
     * @param configuration of the framework.
     */
    public GraphAwareFramework(GraphDatabaseService database, FrameworkConfiguration configuration) {
        this.database = database;
        this.configuration = configuration;
        findRootOrThrowException();
    }

    /**
     * Register a {@link GraphAwareModule}. Note that modules are delegated to in the order they are registered.
     *
     * @param module to register.
     */
    public synchronized void registerModule(GraphAwareModule module) {
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
     * New modules and modules with changed configuration will be re-initialized automatically; there is no need to use
     * this method for that purpose.
     * <p/>
     * Note that modules are delegated to in the order they are registered.
     *
     * @param module              to register.
     * @param forceInitialization true to force (re-)initialization.
     */
    public synchronized void registerModule(GraphAwareModule module, boolean forceInitialization) {
        if (started) {
            LOG.error("Modules must be registered before GraphAware is started!");
            throw new IllegalStateException("Modules must be registered before GraphAware is started!");
        }

        LOG.info("Registering module " + module.getId() + " with GraphAware.");
        checkNotAlreadyRegistered(module.getId());
        modules.add(module);

        if (module instanceof FrameworkConfigured) {
            ((FrameworkConfigured) module).configurationChanged(configuration);
        }

        if (forceInitialization) {
            LOG.info("Forcing module " + module.getId() + " to be initialized.");
            forceInitialization(module);
        }
    }

    private void checkNotAlreadyRegistered(String id) {
        for (GraphAwareModule module : modules) {
            if (id.equals(module.getId())) {
                throw new IllegalStateException("Module " + id + " cannot be registered more than once!");
            }
        }
    }

    /**
     * Start the framework. Must be called before anything gets written into the database.
     */
    public synchronized void start() {
        start(false);
    }

    /**
     * Start the framework, optionally skipping the initialization phase. It is not recommended to skip initialization;
     * un-initialized modules might not behave correctly.
     *
     * @param skipInitialization true for skipping initialization.
     */
    public synchronized void start(boolean skipInitialization) {
        if (started) {
            LOG.error("GraphAware already started!");
            throw new IllegalStateException("GraphAware already started!");
        }

        LOG.info("Starting GraphAware...");

        database.registerTransactionEventHandler(this);

        if (skipInitialization) {
            LOG.info("Initialization skipped.");
        } else {
            LOG.info("Initializing modules...");
            initializeModules();
            LOG.info("Modules initialized.");
        }

        started = true;

        LOG.info("GraphAware started.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Void beforeCommit(TransactionData data) throws Exception {
        if (!started) {
            return null;
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
                forceInitialization(module);
            }
        }

        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void afterCommit(TransactionData data, Void state) {
        //do nothing for now
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void afterRollback(TransactionData data, Void state) {
        //do nothing for now
    }

    /**
     * Initialize modules if needed.
     * <p/>
     * Metadata about modules is stored as properties on the root node (node with ID = 0) in the form of
     * {@link FrameworkConfiguration#GA_PREFIX} + {@link com.graphaware.neo4j.framework.GraphAwareModule#getId()} as key and one of the
     * following as value:
     * - {@link #HASH_CODE} + {@link com.graphaware.neo4j.framework.GraphAwareModule#hashCode()} capturing the last configuration
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

            if (value.startsWith(HASH_CODE)) {
                if (!value.replaceFirst(HASH_CODE, "").equals(Integer.toString(module.hashCode()))) {
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
        module.initialize(database);
        recordInitialization(module);
    }

    /**
     * Re-initialize a module and capture that fact on the as a root node's property.
     *
     * @param module to initialize.
     */
    private void reinitializeModule(final GraphAwareModule module) {
        module.reinitialize(database);
        recordInitialization(module);
    }

    /**
     * Capture the fact the a module has been (re-)initialized as a root node's property.
     *
     * @param module that has been initialized.
     */
    private void recordInitialization(final GraphAwareModule module) {
        final Node root = findRootOrThrowException();
        final String key = moduleKey(module);

        new SimpleTransactionExecutor(database).executeInTransaction(new TransactionCallback<Void>() {
            @Override
            public Void doInTransaction(GraphDatabaseService database) {
                root.setProperty(key, HASH_CODE + module.hashCode());
                return null;
            }
        });
    }

    /**
     * Remove unused modules.
     *
     * @param unusedModules to remove from the root node's properties.
     */
    private void removeUnusedModules(final Collection<String> unusedModules) {
        final Node root = findRootOrThrowException();

        new SimpleTransactionExecutor(database).executeInTransaction(new TransactionCallback<Void>() {
            @Override
            public Void doInTransaction(GraphDatabaseService database) {
                for (String toRemove : unusedModules) {
                    LOG.info("Removing unused module " + toRemove + ".");
                    root.removeProperty(toRemove);
                }
                return null;
            }
        });
    }

    /**
     * Get properties starting with {@link FrameworkConfiguration#GA_PREFIX} from a node.
     *
     * @param node to get properties from.
     * @return map of properties (key-value).
     */
    private Map<String, Object> getInternalProperties(Node node) {
        return PropertyContainerUtils.propertiesToObjectMap(node, new InclusionStrategy<String>() {
            @Override
            public boolean include(String s) {
                return s.startsWith(FrameworkConfiguration.GA_PREFIX);
            }
        });
    }

    /**
     * Build a module key to use as a property on the root node for storing metadata.
     *
     * @param module to build a key for.
     * @return module key.
     */
    private String moduleKey(GraphAwareModule module) {
        return FrameworkConfiguration.GA_PREFIX + module.getId();
    }

    /**
     * Force a module to be (re-)initialized next time the database (and framework) are started.
     *
     * @param module to be (re-)initialized next time.
     */
    private void forceInitialization(final GraphAwareModule module) {
        final Node root = findRootOrThrowException();

        new SimpleTransactionExecutor(database).executeInTransaction(new TransactionCallback<Void>() {
            @Override
            public Void doInTransaction(GraphDatabaseService database) {
                root.setProperty(moduleKey(module), FORCE_INITIALIZATION + System.currentTimeMillis());
                return null;
            }
        });
    }

    /**
     * Find node with ID = 0, or throw an exception.
     *
     * @return root node.
     * @throws IllegalStateException if the node doesn't exist.
     */
    private Node findRootOrThrowException() {
        try {
            return database.getNodeById(0);
        } catch (NotFoundException e) {
            throw new IllegalStateException("GraphAware Framework needs the root node (ID=0) for its operation. Please" +
                    " re-create the database and do not delete the root node. There is no need for it to be used in" +
                    " the application, but it must be present in the database.");
        }
    }
}
