package com.graphaware.runtime;

import static com.graphaware.runtime.config.RuntimeConfiguration.TIMER_MODULES_PROPERTY_PREFIX;
import static com.graphaware.runtime.config.RuntimeConfiguration.TX_MODULES_PROPERTY_PREFIX;

import com.graphaware.runtime.config.FluentRuntimeConfiguration;
import com.graphaware.runtime.metadata.GraphPropertiesMetadataRepository;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.kernel.GraphDatabaseAPI;

import com.graphaware.runtime.config.RuntimeConfiguration;
import com.graphaware.runtime.manager.ProductionTimerDrivenModuleManager;
import com.graphaware.runtime.manager.ProductionTxDrivenModuleManager;
import com.graphaware.runtime.manager.TimerDrivenModuleManager;
import com.graphaware.runtime.manager.TxDrivenModuleManager;
import com.graphaware.runtime.metadata.ModuleMetadataRepository;
import com.graphaware.runtime.module.TxDrivenModule;

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
        return createRuntime(database, FluentRuntimeConfiguration.defaultConfiguration());
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
        ModuleMetadataRepository timerRepo = new GraphPropertiesMetadataRepository(database, configuration, TIMER_MODULES_PROPERTY_PREFIX);
        ModuleMetadataRepository txRepo = new GraphPropertiesMetadataRepository(database, configuration, TX_MODULES_PROPERTY_PREFIX);

        TimerDrivenModuleManager timerDrivenModuleManager = new ProductionTimerDrivenModuleManager(database, timerRepo, configuration.getTimingStrategy());
        TxDrivenModuleManager<TxDrivenModule> txDrivenModuleManager = new ProductionTxDrivenModuleManager(database, txRepo);

        return new ProductionRuntime(configuration, database, txDrivenModuleManager, timerDrivenModuleManager, configuration.getWritingConfig().produceWriter(database));
    }

    private GraphAwareRuntimeFactory() {
    }
}
