/*
 * Copyright (c) 2013-2020 GraphAware
 *
 * This file is part of the GraphAware Framework.
 *
 * GraphAware Framework is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received a copy of
 * the GNU General Public License along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 */

package com.graphaware.runtime.bootstrap;

import com.graphaware.common.log.LoggerFactory;
import com.graphaware.runtime.GraphAwareRuntime;
import com.graphaware.runtime.GraphAwareRuntimeFactory;
import com.graphaware.runtime.config.Neo4jConfigurationReader;
import com.graphaware.runtime.config.CommunityRuntimeConfiguration;
import com.graphaware.runtime.config.RuntimeConfiguration;
import com.graphaware.runtime.module.Module;
import com.graphaware.runtime.module.ModuleBootstrapper;
import org.neo4j.configuration.Config;
import org.neo4j.dbms.api.DatabaseManagementService;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.kernel.lifecycle.Lifecycle;
import org.neo4j.logging.Log;

import java.util.Map;

/**
 * Neo4j kernel extension that automatically creates a {@link GraphAwareRuntime} for the Neo4j database
 * and registers {@link Module}s with it.
 */
public class RuntimeKernelExtension implements Lifecycle {
    private static final Log LOG = LoggerFactory.getLogger(RuntimeKernelExtension.class);

    protected final DatabaseManagementService managementService;
    protected final GraphDatabaseService database;
    protected final RuntimeConfiguration runtimeConfiguration;
    protected GraphAwareRuntime runtime;

    public RuntimeKernelExtension(Config neo4jConfig, DatabaseManagementService managementService, GraphDatabaseService database) {
        this.managementService = managementService;
        this.database = database;
        this.runtimeConfiguration = constructRuntimeConfiguration(neo4jConfig, database);
    }

    protected RuntimeConfiguration constructRuntimeConfiguration(Config neo4jConfig, GraphDatabaseService database) {
        return new CommunityRuntimeConfiguration(database, new Neo4jConfigurationReader(neo4jConfig));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void init() {
        if (isOnEnterprise() && !hasEnterpriseFramework()) {
            throw new RuntimeException("GraphAware Framework Community Edition is not supported on Neo4j Enterprise. Please email info@graphaware.com to get access to GraphAware Framework Enterprise Edition instead.");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void start() {
        if (!runtimeConfiguration.runtimeEnabled()) {
            LOG.info("GraphAware Runtime disabled for database " + database.databaseName() + ".");
            return;
        }

        LOG.info("GraphAware Runtime enabled for database " + database.databaseName() + ", bootstrapping...");

        runtime = createRuntime();

        registerModules(runtime);

        LOG.info("GraphAware Runtime bootstrapped for database " + database.databaseName() + ".");
    }

    protected GraphAwareRuntime createRuntime() {
        return GraphAwareRuntimeFactory.createRuntime(managementService, database);
    }

    private void registerModules(GraphAwareRuntime runtime) {
        runtimeConfiguration.loadConfig().entrySet().stream().sorted(Map.Entry.comparingByValue()).forEach(entry -> {
            LOG.info("Bootstrapping module with order " + entry.getValue().getOrder() + ", ID " + entry.getValue().getId() + ", using " + entry.getValue().getBootstrapper() + " for database " + database.databaseName());

            try {
                ModuleBootstrapper bootstrapper = (ModuleBootstrapper) Class.forName(entry.getValue().getBootstrapper()).getDeclaredConstructor().newInstance();
                runtime.registerModule(bootstrapper.bootstrapModule(entry.getValue().getId(), entry.getValue().getConfig(), database, runtime));
            } catch (Exception e) {
                LOG.error("Unable to bootstrap module " + entry.getKey() + " for database " + database.databaseName(), e);
            }
        });
    }

    protected boolean isOnEnterprise() {
        try {
            Class.forName("com.neo4j.dbms.database.EnterpriseMultiDatabaseManager");
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean hasEnterpriseFramework() {
        try {
            Class.forName("com.graphaware.runtime.bootstrap.EnterpriseRuntimeKernelExtension");
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void stop() {
        runtime = null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void shutdown() {
        //do nothing
    }
}
