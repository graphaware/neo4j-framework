package com.graphaware.runtime.metadata;

import com.graphaware.runtime.config.RuntimeConfiguration;
import com.graphaware.tx.event.batch.api.TransactionSimulatingBatchInserter;
import com.graphaware.tx.event.batch.propertycontainer.inserter.BatchInserterNode;
import org.apache.log4j.Logger;
import org.neo4j.graphdb.Node;

import java.util.HashMap;

import static com.graphaware.runtime.config.RuntimeConfiguration.GA_METADATA;

/**
 * {@link SingleNodeMetadataRepository} backed by a {@link TransactionSimulatingBatchInserter}.
 */
public class BatchSingleNodeMetadataRepository extends SingleNodeMetadataRepository {

    private static final Logger LOG = Logger.getLogger(BatchSingleNodeMetadataRepository.class);

    private final TransactionSimulatingBatchInserter batchInserter;

    /**
     * Create a new repository.
     *
     * @param batchInserter to back the repository.
     * @param configuration of the runtime.
     */
    public BatchSingleNodeMetadataRepository(TransactionSimulatingBatchInserter batchInserter, RuntimeConfiguration configuration) {
        super(configuration);
        this.batchInserter = batchInserter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Node getOrMetadataNode() {
        Node root = null;

        for (long candidate : batchInserter.getAllNodes()) {
            if (batchInserter.nodeHasLabel(candidate, GA_METADATA)) {
                if (root == null) {
                    root = new BatchInserterNode(candidate, batchInserter);
                } else {
                    LOG.fatal("There is more than 1 runtime metadata node! Cannot start GraphAware Runtime.");
                    throw new IllegalStateException("There is more than 1 runtime metadata node! Cannot start GraphAware Runtime.");
                }
            }
        }

        if (root != null) {
            return root;
        }

        return new BatchInserterNode(batchInserter.createNode(new HashMap<String, Object>(), GA_METADATA), batchInserter);
    }
}
