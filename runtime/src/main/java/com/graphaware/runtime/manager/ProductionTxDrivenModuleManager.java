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
import com.graphaware.runtime.config.util.InstanceRoleUtils;
import com.graphaware.runtime.metadata.ModuleMetadataRepository;
import com.graphaware.runtime.metadata.TxDrivenModuleMetadata;
import com.graphaware.runtime.module.TxDrivenModule;
import org.neo4j.graphdb.GraphDatabaseService;

/**
 * {@link BaseTxDrivenModuleManager} backed by a {@link GraphDatabaseService}.
 */
public class ProductionTxDrivenModuleManager extends BaseTxDrivenModuleManager<TxDrivenModule> {

    private final GraphDatabaseService database;

    /**
     * Construct a new manager.
     *
     * @param database           storing graph data.
     * @param metadataRepository for storing module metadata.
     */
    public ProductionTxDrivenModuleManager(GraphDatabaseService database, ModuleMetadataRepository metadataRepository, StatsCollector statsCollector) {
        super(metadataRepository, statsCollector, new InstanceRoleUtils(database));
        this.database = database;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void start(TxDrivenModule module) {
        module.start(database);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize(TxDrivenModule module) {
        module.initialize(database);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void reinitialize(TxDrivenModule module, TxDrivenModuleMetadata oldMetadata) {
        module.reinitialize(database, oldMetadata);
    }
}
