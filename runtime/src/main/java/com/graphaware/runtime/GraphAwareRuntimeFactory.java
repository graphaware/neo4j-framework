package com.graphaware.runtime;

import static com.graphaware.runtime.config.RuntimeConfiguration.TIMER_MODULES_PROPERTY_PREFIX;
import static com.graphaware.runtime.config.RuntimeConfiguration.TX_MODULES_PROPERTY_PREFIX;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.kernel.GraphDatabaseAPI;

import com.graphaware.runtime.config.DefaultRuntimeConfiguration;
import com.graphaware.runtime.config.RuntimeConfiguration;
import com.graphaware.runtime.manager.BatchModuleManager;
import com.graphaware.runtime.manager.ProductionTimerDrivenModuleManager;
import com.graphaware.runtime.manager.ProductionTxDrivenModuleManager;
import com.graphaware.runtime.manager.TimerDrivenModuleManager;
import com.graphaware.runtime.manager.TxDrivenModuleManager;
import com.graphaware.runtime.metadata.BatchSingleNodeMetadataRepository;
import com.graphaware.runtime.metadata.ModuleMetadataRepository;
import com.graphaware.runtime.metadata.ProductionSingleNodeMetadataRepository;
import com.graphaware.runtime.module.BatchSupportingTxDrivenModule;
import com.graphaware.runtime.module.TxDrivenModule;
import com.graphaware.runtime.schedule.AdaptiveTimingStrategy;
import com.graphaware.tx.event.batch.api.TransactionSimulatingBatchInserter;

/**
 * Factory producing {@link GraphAwareRuntime}. This should be the only way a runtime is created.
 */
public final class GraphAwareRuntimeFactory {

    /**
     * Create a runtime backed by a database using default runtime configuration.
     * <p/>
     * The runtime only supports {@link com.graphaware.runtime.module.TimerDrivenModule}s if the database is a real transactional
     * (rather than batch) database, i.e., that it implements {@link GraphDatabaseAPI}.
     *
     * @param database backing the runtime.
     * @return runtime.
     */
    public static GraphAwareRuntime createRuntime(GraphDatabaseService database) {
        return createRuntime(database, DefaultRuntimeConfiguration.getInstance());
    }

    /**
     * Create a runtime backed by a database using specific runtime configuration.
     * <p/>
     * The runtime only supports {@link com.graphaware.runtime.module.TimerDrivenModule}s if the database is a real transactional
     * (rather than batch) database, i.e., that it implements {@link GraphDatabaseAPI}.
     *
     * @param database      backing the runtime.
     * @param configuration custom configuration.
     * @return runtime.
     */
    public static GraphAwareRuntime createRuntime(GraphDatabaseService database, RuntimeConfiguration configuration) {
        if (database instanceof GraphDatabaseAPI) {
            return createProductionRuntime(database, configuration);
        }

        return createBatchRuntime(database, configuration);
    }

    /**
     * Create a runtime backed by a {@link TransactionSimulatingBatchInserter} using default runtime configuration.
     *
     * @param batchInserter backing the runtime.
     * @return runtime.
     */
    public static GraphAwareRuntime createRuntime(TransactionSimulatingBatchInserter batchInserter) {
        return createRuntime(batchInserter, DefaultRuntimeConfiguration.getInstance());
    }

    /**
     * Create a runtime backed by a {@link TransactionSimulatingBatchInserter} using specific runtime configuration.
     *
     * @param batchInserter backing the runtime.
     * @param configuration custom configuration.
     * @return runtime.
     */
    public static GraphAwareRuntime createRuntime(TransactionSimulatingBatchInserter batchInserter, RuntimeConfiguration configuration) {
        ModuleMetadataRepository metadataRepository = new BatchSingleNodeMetadataRepository(batchInserter, configuration, TX_MODULES_PROPERTY_PREFIX);
        TxDrivenModuleManager<BatchSupportingTxDrivenModule> manager = new BatchModuleManager(batchInserter, metadataRepository);

        return new BatchInserterRuntime(batchInserter, manager);
    }

    private static GraphAwareRuntime createProductionRuntime(GraphDatabaseService database, RuntimeConfiguration configuration) {
        ModuleMetadataRepository timerRepo = new ProductionSingleNodeMetadataRepository(database, configuration, TIMER_MODULES_PROPERTY_PREFIX);
        ModuleMetadataRepository txRepo = new ProductionSingleNodeMetadataRepository(database, configuration, TX_MODULES_PROPERTY_PREFIX);

        TimerDrivenModuleManager timerDrivenModuleManager = new ProductionTimerDrivenModuleManager(database, timerRepo,
        		new AdaptiveTimingStrategy(database, 1000)); // TODO: make the default delay configurable, bearing in mind the tests
        TxDrivenModuleManager<TxDrivenModule> txDrivenModuleManager = new ProductionTxDrivenModuleManager(database, txRepo);

        return new ProductionRuntime(database, txDrivenModuleManager, timerDrivenModuleManager);
    }

    private static GraphAwareRuntime createBatchRuntime(GraphDatabaseService database, RuntimeConfiguration configuration) {
        ModuleMetadataRepository repository = new ProductionSingleNodeMetadataRepository(database, configuration, TX_MODULES_PROPERTY_PREFIX);
        TxDrivenModuleManager<TxDrivenModule> txDrivenModuleManager = new ProductionTxDrivenModuleManager(database, repository);

        return new DatabaseRuntime(database, txDrivenModuleManager);
    }

    private GraphAwareRuntimeFactory() {
    }
}
