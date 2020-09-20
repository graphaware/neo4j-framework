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
import com.graphaware.common.util.Pair;
import com.graphaware.runtime.GraphAwareRuntime;
import com.graphaware.runtime.GraphAwareRuntimeFactory;
import com.graphaware.runtime.config.Neo4jConfigBasedRuntimeConfiguration;
import com.graphaware.runtime.module.Module;
import com.graphaware.runtime.module.ModuleBootstrapper;
import com.graphaware.runtime.settings.FrameworkSettingsDeclaration;
import org.apache.commons.configuration2.CompositeConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.SystemConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.neo4j.configuration.Config;
import org.neo4j.dbms.api.DatabaseManagementService;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.kernel.lifecycle.Lifecycle;
import org.neo4j.logging.Log;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Neo4j kernel extension that automatically creates a {@link GraphAwareRuntime} and registers
 * {@link Module}s with it.
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
 * @see Neo4jConfigBasedRuntimeConfiguration
 */
public class RuntimeKernelExtension implements Lifecycle {
    private static final Log LOG = LoggerFactory.getLogger(RuntimeKernelExtension.class);

    public static final String RUNTIME_ENABLED_CONFIG = "com.graphaware.runtime.enabled";
    public static final String MODULE_CONFIG_KEY = "com.graphaware.module"; //.ID.Order = fully qualified class name of bootstrapper
    private static final Pattern MODULE_ENABLED_KEY = Pattern.compile("([a-zA-Z0-9]{1,})\\.([0-9]{1,})");
    private static final int WAIT_MINUTES = 5;
    private static final int WAIT_MS = WAIT_MINUTES * 60 * 1000;

    protected final Config neo4jConfig;
    protected Configuration gaConfig;
    protected final DatabaseManagementService managementService;
    protected final GraphDatabaseService database;

    public RuntimeKernelExtension(Config neo4jConfig, DatabaseManagementService managementService, GraphDatabaseService database) {
        this.neo4jConfig = neo4jConfig;
        this.managementService = managementService;
        this.database = database;
        this.gaConfig = gaConfig();
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
        if ("system".equals(database.databaseName())) {
            LOG.info("GraphAware Runtime always disabled on system database.");
            return;
        }

        if (!gaConfig.containsKey(RUNTIME_ENABLED_CONFIG)) {
            LOG.info("GraphAware Runtime disabled.");
            return;
        }

        if (!gaConfig.getBoolean(RUNTIME_ENABLED_CONFIG)) {
            LOG.info("GraphAware Runtime disabled.");
            return;
        }

        LOG.info("GraphAware Runtime enabled, bootstrapping...");

        final GraphAwareRuntime runtime = createRuntime();

        registerModules(runtime);

        new Thread(() -> {
            if (databaseIsAvailable()) {
                runtime.start();
            } else {
                LOG.error("Could not start GraphAware Runtime because the database didn't get to a usable state within " + WAIT_MINUTES + " minutes.");
            }
        }, "GraphAware Starter").start();

        LOG.info("GraphAware Runtime bootstrapped.");
    }

    protected boolean databaseIsAvailable() {
        return database.isAvailable(WAIT_MS);
    }

    protected GraphAwareRuntime createRuntime() {
        return GraphAwareRuntimeFactory.createRuntime(managementService, database, new Neo4jConfigBasedRuntimeConfiguration(database, neo4jConfig));
    }

    private void registerModules(GraphAwareRuntime runtime) {
        List<Pair<Integer, Pair<String, String>>> orderedBootstrappers = findOrderedBootstrappers();

        int lastOrder = -1;
        for (Pair<Integer, Pair<String, String>> bootstrapperPair : orderedBootstrappers) {
            int order = bootstrapperPair.first();
            LOG.info("Bootstrapping module with order " + order + ", ID " + bootstrapperPair.second().first() + ", using " + bootstrapperPair.second().second());

            if (lastOrder == order) {
                LOG.warn("There is more than one module with order " + order + "! Will order clashing modules randomly");
            }

            lastOrder = order;

            try {
                ModuleBootstrapper bootstrapper = (ModuleBootstrapper) Class.forName(bootstrapperPair.second().second()).newInstance();
                runtime.registerModule(bootstrapper.bootstrapModule(bootstrapperPair.second().first(), findModuleConfig(bootstrapperPair.second().first()), database));
            } catch (Exception e) {
                LOG.error("Unable to bootstrap module " + bootstrapperPair.first(), e);
            }
        }
    }

    private List<Pair<Integer, Pair<String, String>>> findOrderedBootstrappers() {
        List<Pair<Integer, Pair<String, String>>> orderedBootstrappers = new ArrayList<>();

        Configuration subset = gaConfig.subset(MODULE_CONFIG_KEY);
        subset.getKeys().forEachRemaining(s -> {
            Matcher matcher = MODULE_ENABLED_KEY.matcher(s);
            if (matcher.find()) {
                String moduleId = matcher.group(1);
                Integer moduleOrder = Integer.valueOf(matcher.group(2));
                String bootstrapperClass = subset.getString(s, "UNKNOWN");
                orderedBootstrappers.add(Pair.of(moduleOrder, Pair.of(moduleId, bootstrapperClass)));
            }
        });

        orderedBootstrappers.sort(Comparator.comparingInt(Pair::first));

        return orderedBootstrappers;
    }

    private Map<String, String> findModuleConfig(String moduleId) {
        Map<String, String> moduleConfig = new HashMap<>();

        String moduleConfigKeyPrefix = MODULE_CONFIG_KEY + "." + moduleId;

        Configuration subset = gaConfig.subset(moduleConfigKeyPrefix);
        subset.getKeys().forEachRemaining(s -> {
            if (!MODULE_ENABLED_KEY.matcher(s).find()) {
                moduleConfig.put(s, subset.getString(s));
            }
        });

        return moduleConfig;
    }

    private Configuration gaConfig() {
        FileBasedConfigurationBuilder<PropertiesConfiguration> builder =
                new FileBasedConfigurationBuilder<>(PropertiesConfiguration.class)
                        .configure(new Parameters()
                                .fileBased()
                                .setURL(getClass().getClassLoader().getResource(neo4jConfig.get(FrameworkSettingsDeclaration.ga_config_file_name))));

        CompositeConfiguration cc = new CompositeConfiguration();

        try {
            cc.addConfiguration(builder.getConfiguration());
            cc.addConfiguration(new SystemConfiguration());
        } catch (ConfigurationException e) {
            throw new RuntimeException(e);
        }

        return cc;
    }

    protected boolean isOnEnterprise() {
        try {
            Class.forName("org.neo4j.kernel.impl.enterprise.EnterpriseEditionModule");
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
        //do nothing
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void shutdown() {
        //do nothing
    }
}
