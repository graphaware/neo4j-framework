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
import org.neo4j.graphdb.event.ErrorState;
import org.neo4j.graphdb.event.KernelEventHandler;
import org.neo4j.graphdb.event.TransactionData;
import org.neo4j.graphdb.event.TransactionEventHandler;

import java.util.*;


/**
 * {@link BaseGraphAwareFramework} that operates on a real {@link GraphDatabaseService}.
 *
 * @see BaseGraphAwareFramework
 */
public class GraphAwareFramework extends BaseGraphAwareFramework {
    private static final Logger LOG = Logger.getLogger(GraphAwareFramework.class);

    private final GraphDatabaseService database;

    /**
     * Create a new instance of the framework.
     *
     * @param database on which the framework should operate.
     */
    public GraphAwareFramework(GraphDatabaseService database) {
        super(DefaultFrameworkConfiguration.getInstance());
        this.database = database;
        findRootOrThrowException();
    }

    /**
     * Create a new instance of the framework.
     *
     * @param database      on which the framework should operate.
     * @param configuration of the framework.
     */
    public GraphAwareFramework(FrameworkConfiguration configuration, GraphDatabaseService database) {
        super(configuration);
        this.database = database;
        findRootOrThrowException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void registerSelfAsHandler() {
        database.registerTransactionEventHandler(this);
        database.registerKernelEventHandler(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doInitialize(GraphAwareModule module) {
        module.initialize(database);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doReinitialize(GraphAwareModule module) {
        module.reinitialize(database);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doRecordInitialization(final GraphAwareModule module, final String key) {
        new SimpleTransactionExecutor(database).executeInTransaction(new TransactionCallback<Void>() {
            @Override
            public Void doInTransaction(GraphDatabaseService database) {
                findRootOrThrowException().setProperty(key, HASH_CODE + module.hashCode());
                return null;
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void removeUnusedModules(final Collection<String> unusedModules) {
        new SimpleTransactionExecutor(database).executeInTransaction(new TransactionCallback<Void>() {
            @Override
            public Void doInTransaction(GraphDatabaseService database) {
                for (String toRemove : unusedModules) {
                    LOG.info("Removing unused module " + toRemove + ".");
                    findRootOrThrowException().removeProperty(toRemove);
                }
                return null;
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void forceInitialization(final GraphAwareModule module) {
        new SimpleTransactionExecutor(database).executeInTransaction(new TransactionCallback<Void>() {
            @Override
            public Void doInTransaction(GraphDatabaseService database) {
                findRootOrThrowException().setProperty(moduleKey(module), FORCE_INITIALIZATION + System.currentTimeMillis());
                return null;
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Node getRoot() {
        return database.getNodeById(0);
    }
}
