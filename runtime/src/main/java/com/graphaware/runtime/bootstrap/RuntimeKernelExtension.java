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
 * Neo4j kernel extension that automatically creates a {@link GraphAwareRuntime} and registers {@link Module}s with it.
 * <p/>
 * The mechanism of this extension works as follows. Of course, the GraphAware Framework .jar file must be present on
 * classpath (embedded mode), or in the "plugins" directory (server mode).
 * <p/>
 * The Runtime is only created when a setting called "com.graphaware.runtime.enabled" with value equal to "true" or "1"
 * is passed as a configuration to the database. This can be achieved by any of the standard mechanisms of passing
 * configuration to the database, for example programmaticaly using {@link org.neo4j.graphdb.factory.GraphDatabaseFactory}
 * (embedded mode), or declaratively using neo4j.conf (typically server mode).
 * <p/>
 * Modules are registered similarly. For each module that should be registered, there must be an entry in the configuration
 * passed to the database. The key of the entry should be "com.graphaware.module.X.Y", where X becomes the ID
 * of the module ({@link Module#getId()}) and Y becomes the order in which the
 * module gets registered with respect to other modules. The value of the configuration entry must be a fully qualified
 * class name of a {@link ModuleBootstrapper} present on the classpath or as a .jar
 * file in the "plugins" directory. Of course, third party modules can be registered as well.
 * <p/>
 * Custom configuration to the modules can be also passed via database configuration in the form of
 * "com.graphaware.module.X.A = B", where X is the module ID, A is the configuration key, and B is the configuration value.
 * <p/>
 * For instance, if you develop a {@link Module} that is bootstrapped by
 * <code>com.mycompany.mymodule.MyBootstrapper</code> and want to register it as the first module of the runtime with MyModuleID as
 * the module ID, with an extra configuration called "threshold" equal to 20, then there should be the two following
 * configuration entries passed to the database:
 * <p/>
 * <pre>
 * com.graphaware.runtime.enabled=true
 * com.graphaware.module.MyModuleID.1=com.mycompany.mymodule.MyBootstrapper
 * com.graphaware.module.MyModuleID.threshold=20
 * </pre>
 * <p/>
 *
 * @see ModuleBootstrapper
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
                ModuleBootstrapper bootstrapper = (ModuleBootstrapper) Class.forName(entry.getValue().getBootstrapper()).newInstance();
                runtime.registerModule(bootstrapper.bootstrapModule(entry.getValue().getId(), entry.getValue().getConfig(), database));
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
