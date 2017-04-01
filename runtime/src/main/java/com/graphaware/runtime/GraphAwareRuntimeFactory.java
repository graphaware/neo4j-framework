/*
 * Copyright (c) 2013-2017 GraphAware
 *
 * This file is part of the GraphAware Framework.
 *
 * GraphAware Framework is free software: you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received a copy of
 * the GNU General Public License along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 */

package com.graphaware.runtime;

import com.graphaware.runtime.config.FluentRuntimeConfiguration;
import com.graphaware.runtime.config.RuntimeConfiguration;
import com.graphaware.runtime.manager.ProductionTimerDrivenModuleManager;
import com.graphaware.runtime.manager.ProductionTxDrivenModuleManager;
import com.graphaware.runtime.manager.TimerDrivenModuleManager;
import com.graphaware.runtime.manager.TxDrivenModuleManager;
import com.graphaware.runtime.metadata.GraphPropertiesMetadataRepository;
import com.graphaware.runtime.metadata.ModuleMetadataRepository;
import com.graphaware.runtime.module.TxDrivenModule;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.kernel.internal.GraphDatabaseAPI;

import static com.graphaware.runtime.config.RuntimeConfiguration.TIMER_MODULES_PROPERTY_PREFIX;
import static com.graphaware.runtime.config.RuntimeConfiguration.TX_MODULES_PROPERTY_PREFIX;

/**
 * Factory producing {@link GraphAwareRuntime}. This should be the only way a runtime is created.
 */
public final class GraphAwareRuntimeFactory {

    /**
     * Create a runtime backed by a database using default runtime configuration.
     * <p>
     * The runtime only supports {@link com.graphaware.runtime.module.TimerDrivenModule}s if the database is a real transactional
     * (rather than batch) database, i.e., that it implements {@link GraphDatabaseAPI}.
     *
     * @param database backing the runtime.
     * @return runtime.
     */
    public static GraphAwareRuntime createRuntime(GraphDatabaseService database) {
        return createRuntime(database, FluentRuntimeConfiguration.defaultConfiguration(database));
    }

    /**
     * Create a runtime backed by a database using specific runtime configuration.
     * <p>
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

        TimerDrivenModuleManager timerDrivenModuleManager = new ProductionTimerDrivenModuleManager(database, timerRepo, configuration.getTimingStrategy(), configuration.getStatsCollector());
        TxDrivenModuleManager<TxDrivenModule> txDrivenModuleManager = new ProductionTxDrivenModuleManager(database, txRepo, configuration.getStatsCollector());

        return new ProductionRuntime(configuration, database, txDrivenModuleManager, timerDrivenModuleManager, configuration.getWritingConfig().produceWriter(database));
    }

    private GraphAwareRuntimeFactory() {
    }
}
