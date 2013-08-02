package com.graphaware.neo4j.framework;

import com.graphaware.neo4j.framework.config.DefaultFrameworkConfiguration;
import com.graphaware.neo4j.framework.config.FrameworkConfiguration;
import com.graphaware.neo4j.tx.single.SimpleTransactionExecutor;
import com.graphaware.neo4j.tx.single.VoidReturningCallback;
import org.apache.log4j.Logger;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;

import java.util.Collection;


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
    public GraphAwareFramework(GraphDatabaseService database, FrameworkConfiguration configuration) {
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
        new SimpleTransactionExecutor(database).executeInTransaction(new VoidReturningCallback() {
            @Override
            protected void doInTx(GraphDatabaseService database) {
                findRootOrThrowException().setProperty(key, HASH_CODE + module.hashCode());
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void removeUnusedModules(final Collection<String> unusedModules) {
        new SimpleTransactionExecutor(database).executeInTransaction(new VoidReturningCallback() {
            @Override
            protected void doInTx(GraphDatabaseService database) {
                for (String toRemove : unusedModules) {
                    LOG.info("Removing unused module " + toRemove + ".");
                    findRootOrThrowException().removeProperty(toRemove);
                }
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void forceInitialization(final GraphAwareModule module) {
        new SimpleTransactionExecutor(database).executeInTransaction(new VoidReturningCallback() {
            @Override
            protected void doInTx(GraphDatabaseService database) {
                findRootOrThrowException().setProperty(moduleKey(module), FORCE_INITIALIZATION + System.currentTimeMillis());
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
