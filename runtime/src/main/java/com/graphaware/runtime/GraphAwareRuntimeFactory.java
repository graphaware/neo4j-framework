package com.graphaware.runtime;

import com.graphaware.runtime.config.DefaultRuntimeConfiguration;
import com.graphaware.runtime.config.RuntimeConfiguration;
import com.graphaware.runtime.manager.*;
import com.graphaware.runtime.metadata.BatchSingleNodeModuleMetadataRepository;
import com.graphaware.runtime.metadata.ModuleMetadataRepository;
import com.graphaware.runtime.metadata.ProductionSingleNodeModuleMetadataRepository;
import com.graphaware.runtime.module.TransactionDrivenRuntimeModule;
import com.graphaware.runtime.strategy.BatchSupportingTransactionDrivenRuntimeModule;
import com.graphaware.tx.event.batch.api.TransactionSimulatingBatchInserter;
import com.graphaware.tx.event.batch.api.TransactionSimulatingBatchInserterImpl;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.unsafe.batchinsert.BatchInserter;

/**
 *
 */
public final class GraphAwareRuntimeFactory {

    public static GraphAwareRuntime productionRuntime(GraphDatabaseService database) {
        return productionRuntime(database, DefaultRuntimeConfiguration.getInstance());
    }

    public static GraphAwareRuntime productionRuntime(GraphDatabaseService database, RuntimeConfiguration configuration) {
        ModuleMetadataRepository repository = new ProductionSingleNodeModuleMetadataRepository(database, configuration);
        TimerDrivenModuleManager timerDrivenModuleManager = new TimerDrivenModuleManagerImpl(repository, database);
        TransactionDrivenModuleManager<TransactionDrivenRuntimeModule> transactionDrivenModuleManager = new ProductionTransactionDrivenModuleManager(repository, database);

        return new TimerDrivenModuleSupportingRuntime(database, transactionDrivenModuleManager, timerDrivenModuleManager);
    }

    public static GraphAwareRuntime productionRuntime(TransactionSimulatingBatchInserter batchInserter) {
        return productionRuntime(batchInserter, DefaultRuntimeConfiguration.getInstance());
    }

    public static GraphAwareRuntime productionRuntime(TransactionSimulatingBatchInserter batchInserter, RuntimeConfiguration configuration) {
        ModuleMetadataRepository metadataRepository = new BatchSingleNodeModuleMetadataRepository(batchInserter, configuration);
        TransactionDrivenModuleManager<BatchSupportingTransactionDrivenRuntimeModule> manager = new BatchModuleManager(metadataRepository, batchInserter);

        return new BatchInserterBackedRuntime(batchInserter, configuration, manager);
    }

    private GraphAwareRuntimeFactory() {
    }
}
