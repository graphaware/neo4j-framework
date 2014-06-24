package com.graphaware.runtime.metadata;

import com.graphaware.runtime.config.RuntimeConfiguration;
import com.graphaware.tx.event.batch.api.TransactionSimulatingBatchInserter;
import com.graphaware.tx.event.batch.propertycontainer.inserter.BatchInserterNode;
import org.apache.log4j.Logger;
import org.neo4j.graphdb.Node;

import java.util.HashMap;

import static com.graphaware.runtime.config.RuntimeConfiguration.GA_METADATA;

/**
 *
 */
public class BatchSingleNodeModuleMetadataRepository extends SingleNodeModuleMetadataRepository {

    private static final Logger LOG = Logger.getLogger(BatchSingleNodeModuleMetadataRepository.class);

    private final TransactionSimulatingBatchInserter batchInserter;

    public BatchSingleNodeModuleMetadataRepository(TransactionSimulatingBatchInserter batchInserter, RuntimeConfiguration configuration) {
        super(configuration);
        this.batchInserter = batchInserter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Node getOrCreateRoot() {
        for (long candidate : batchInserter.getAllNodes()) {
            if (batchInserter.nodeHasLabel(candidate, GA_METADATA)) {
                return new BatchInserterNode(candidate, batchInserter);
            }
        }

        return new BatchInserterNode(batchInserter.createNode(new HashMap<String, Object>(), GA_METADATA), batchInserter);
    }
}
