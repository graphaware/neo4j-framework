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

package com.graphaware.runtime.manager;

import com.graphaware.common.ping.StatsCollector;
import com.graphaware.runtime.metadata.DefaultTimerDrivenModuleMetadata;
import com.graphaware.runtime.metadata.ModuleMetadataRepository;
import com.graphaware.runtime.metadata.TimerDrivenModuleMetadata;
import com.graphaware.runtime.module.TimerDrivenModule;
import com.graphaware.runtime.schedule.RotatingTaskScheduler;
import com.graphaware.runtime.schedule.TaskScheduler;
import com.graphaware.runtime.schedule.TimingStrategy;
import org.neo4j.graphdb.GraphDatabaseService;

/**
 * Production implementation of {@link TimerDrivenModuleManager}. Must be backed by a {@link GraphDatabaseService},
 * as there is no support for using {@link TimerDrivenModule}s in batch mode (i.e. with {@link org.neo4j.unsafe.batchinsert.BatchInserter}s).
 */
public class ProductionTimerDrivenModuleManager extends BaseModuleManager<TimerDrivenModuleMetadata, TimerDrivenModule> implements TimerDrivenModuleManager {

    private final GraphDatabaseService database;
    private final TaskScheduler taskScheduler;

    /**
     * Constructs a new {@link ProductionTimerDrivenModuleManager} based on the given arguments.
     *
     * @param database           storing graph data.
     * @param metadataRepository for storing module metadata.
     * @param timingStrategy     the {@link TimingStrategy} to use for scheduling the timer-driven modules.
     */
    public ProductionTimerDrivenModuleManager(GraphDatabaseService database, ModuleMetadataRepository metadataRepository, TimingStrategy timingStrategy, StatsCollector statsCollector) {
        super(metadataRepository, statsCollector);
        this.database = database;
        taskScheduler = new RotatingTaskScheduler(database, metadataRepository, timingStrategy);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected TimerDrivenModuleMetadata createFreshMetadata(TimerDrivenModule module) {
        return new DefaultTimerDrivenModuleMetadata(module.createInitialContext(database));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected TimerDrivenModuleMetadata acknowledgeMetadata(TimerDrivenModule module, TimerDrivenModuleMetadata metadata) {
        taskScheduler.registerModuleAndContext(module, metadata.lastContext());
        return metadata;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void startModules() {
        super.startModules();

        taskScheduler.start();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void shutdownModules() {
        super.shutdownModules();
        taskScheduler.stop();
    }
}
