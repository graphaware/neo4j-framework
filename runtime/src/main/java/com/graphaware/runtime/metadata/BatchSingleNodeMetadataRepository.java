package com.graphaware.runtime.metadata;

import com.graphaware.runtime.config.RuntimeConfiguration;
import com.graphaware.tx.event.batch.api.TransactionSimulatingBatchInserter;
import com.graphaware.tx.event.batch.propertycontainer.inserter.BatchInserterNode;
import org.neo4j.graphdb.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

import static com.graphaware.runtime.config.RuntimeConfiguration.GA_METADATA;

/**
 * {@link SingleNodeMetadataRepository} backed by a {@link TransactionSimulatingBatchInserter}.
 */
public class BatchSingleNodeMetadataRepository extends SingleNodeMetadataRepository {

    private static final Logger LOG = LoggerFactory.getLogger(BatchSingleNodeMetadataRepository.class);

    private final TransactionSimulatingBatchInserter batchInserter;

    /**
     * Create a new repository.
     *
     * @param batchInserter  to back the repository.
     * @param configuration  of the runtime.
     * @param propertyPrefix String with which the property keys of properties written by this repository will be prefixed.
     */
    public BatchSingleNodeMetadataRepository(TransactionSimulatingBatchInserter batchInserter, RuntimeConfiguration configuration, String propertyPrefix) {
        super(configuration, propertyPrefix);
        this.batchInserter = batchInserter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Node getMetadataNode() {
        Node metadataNode = null;

        for (long candidate : batchInserter.getAllNodes()) {
            if (batchInserter.nodeHasLabel(candidate, GA_METADATA)) {
                if (metadataNode == null) {
                    metadataNode = new BatchInserterNode(candidate, batchInserter);
                } else {
                    LOG.error("There is more than 1 runtime metadata node! Cannot start/use GraphAware Runtime.");
                    throw new IllegalStateException("There is more than 1 runtime metadata node! Cannot start/use GraphAware Runtime.");
                }
            }
        }

        return metadataNode;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Node getOrCreateMetadataNode() {
        Node metadataNode = getMetadataNode();

        if (metadataNode != null) {
            return metadataNode;
        }

        return new BatchInserterNode(batchInserter.createNode(new HashMap<String, Object>(), GA_METADATA), batchInserter);
    }
}
