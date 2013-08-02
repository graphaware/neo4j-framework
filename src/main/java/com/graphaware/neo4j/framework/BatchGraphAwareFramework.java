package com.graphaware.neo4j.framework;

import com.graphaware.neo4j.framework.config.DefaultFrameworkConfiguration;
import com.graphaware.neo4j.framework.config.FrameworkConfiguration;
import com.graphaware.neo4j.tx.batch.api.TransactionSimulatingBatchInserter;
import com.graphaware.neo4j.tx.batch.propertycontainer.inserter.BatchInserterNode;
import com.graphaware.neo4j.tx.single.SimpleTransactionExecutor;
import com.graphaware.neo4j.tx.single.TransactionCallback;
import org.apache.log4j.Logger;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.NotFoundException;
import org.neo4j.kernel.impl.nioneo.store.FileSystemAbstraction;
import org.neo4j.unsafe.batchinsert.BatchInserter;
import org.neo4j.unsafe.batchinsert.TransactionSimulatingBatchGraphDatabase;
import org.neo4j.unsafe.batchinsert.TransactionSimulatingBatchInserterImpl;

import java.util.Collection;
import java.util.Map;


/**
 * {@link com.graphaware.neo4j.framework.BaseGraphAwareFramework} that operates on a {@link org.neo4j.unsafe.batchinsert.BatchInserter},
 * or more precisely {@link TransactionSimulatingBatchInserter}.
 *
 * @see com.graphaware.neo4j.framework.BaseGraphAwareFramework
 * @see org.neo4j.unsafe.batchinsert.BatchInserter - same limitations apply.
 */
public class BatchGraphAwareFramework extends BaseGraphAwareFramework {
    private static final Logger LOG = Logger.getLogger(BatchGraphAwareFramework.class);

    private final TransactionSimulatingBatchInserter batchInserter;

    /**
     * Create a new instance of the framework.
     *
     * @param batchInserter that the framework should use.
     */
    public BatchGraphAwareFramework(TransactionSimulatingBatchInserter batchInserter) {
        this.batchInserter = batchInserter;
        findRootOrThrowException();
    }

    /**
     * Create a new instance of the framework.
     *
     * @param batchInserter that the framework should use.
     * @param configuration of the framework.
     */
    public BatchGraphAwareFramework(TransactionSimulatingBatchInserter batchInserter, FrameworkConfiguration configuration) {
        super(configuration);
        this.batchInserter = batchInserter;
        findRootOrThrowException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void registerSelfAsHandler() {
        batchInserter.registerTransactionEventHandler(this);
        batchInserter.registerKernelEventHandler(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doInitialize(GraphAwareModule module) {
        module.initialize(batchInserter);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doReinitialize(GraphAwareModule module) {
        module.reinitialize(batchInserter);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doRecordInitialization(final GraphAwareModule module, final String key) {
        findRootOrThrowException().setProperty(key, HASH_CODE + module.hashCode());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void removeUnusedModules(final Collection<String> unusedModules) {
        for (String toRemove : unusedModules) {
            LOG.info("Removing unused module " + toRemove + ".");
            findRootOrThrowException().removeProperty(toRemove);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void forceInitialization(final GraphAwareModule module) {
        findRootOrThrowException().setProperty(moduleKey(module), FORCE_INITIALIZATION + System.currentTimeMillis());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Node getRoot() {
        if (!batchInserter.nodeExists(0)) {
            throw new NotFoundException();
        }
        return new BatchInserterNode(0, batchInserter);
    }
}
