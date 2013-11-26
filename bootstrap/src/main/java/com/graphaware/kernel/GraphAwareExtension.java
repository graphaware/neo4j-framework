/*
 * Copyright (c) 2013 GraphAware
 *
 * This file is part of GraphAware.
 *
 * GraphAware is free software: you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received a copy of
 * the GNU General Public License along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 */

package com.graphaware.kernel;

import com.graphaware.framework.GraphAwareFramework;
import com.graphaware.framework.GraphAwareModuleBootstrapper;
import com.graphaware.framework.config.DefaultFrameworkConfiguration;
import org.apache.log4j.Logger;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.kernel.AvailabilityGuard;
import org.neo4j.kernel.InternalAbstractGraphDatabase;
import org.neo4j.kernel.configuration.Config;
import org.neo4j.kernel.extension.KernelExtensionListener;
import org.neo4j.kernel.extension.KernelExtensions;
import org.neo4j.kernel.impl.core.NodeManager;
import org.neo4j.kernel.lifecycle.LifeSupport;
import org.neo4j.kernel.lifecycle.Lifecycle;
import org.neo4j.kernel.lifecycle.LifecycleListener;
import org.neo4j.kernel.lifecycle.LifecycleStatus;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.Executors;

/**
 * Extension that initializes the {@link com.graphaware.framework.GraphAwareFramework}.
 */
public class GraphAwareExtension implements Lifecycle {
    private static final Logger LOG = Logger.getLogger(GraphAwareExtension.class);

    private static final String FW_ENABLE_KEY = "com.graphaware.framework.enabled";
    private static final String MODULE_ENABLE_REGEX = "com\\.graphaware\\.module\\.[a-z]*\\.enabled";

    private final Config config;
    private final GraphDatabaseService database;

    private GraphAwareFramework framework;

    public GraphAwareExtension(Config config, GraphDatabaseService database) {
        this.config = config;
        this.database = database;
    }

    @Override
    public void init() throws Throwable {

    }

    @Override
    public void start() throws Throwable {
        Map<String, String> params = config.getParams();

        if (!params.containsKey(FW_ENABLE_KEY) || !representsTrue(params.get(FW_ENABLE_KEY))) {
            LOG.info("GraphAware Framework disabled.");
            return;
        }

        LOG.info("GraphAware Framework enabled, bootstrapping...");

        framework = new GraphAwareFramework(database, DefaultFrameworkConfiguration.getInstance());

        for (String paramKey : params.keySet()) {
            if (paramKey.matches(MODULE_ENABLE_REGEX)) {
                String paramValue = params.get(paramKey);
                LOG.info("Bootstrapping module with key " + paramKey + ", using " + paramValue);

                try {
                    GraphAwareModuleBootstrapper bootstrapper = (GraphAwareModuleBootstrapper) Class.forName(paramValue).newInstance();
                    bootstrapper.bootstrap(framework, config);
                } catch (Exception e) {
                    LOG.error("Unable to bootstrap module " + paramKey, e);
                }
            }
        }

        LOG.info("GraphAware Framework bootstrapped.");

        //Extremely ugly (for the lack of a better way of doing this):
        Field availabilityGuardField = InternalAbstractGraphDatabase.class.getDeclaredField("availabilityGuard");
        availabilityGuardField.setAccessible(true);
        AvailabilityGuard availabilityGuard = (AvailabilityGuard) availabilityGuardField.get(database);
        availabilityGuard.addListener(new AvailabilityGuard.AvailabilityListener() {
            @Override
            public void available() {
                LOG.info("Database available, starting GraphAware Framework...");

                framework.start();

                LOG.info("GraphAware Framework started.");
            }

            @Override
            public void unavailable() {
            }
        });
    }

    @Override
    public void stop() throws Throwable {
    }

    @Override
    public void shutdown() throws Throwable {
    }

    private boolean representsTrue(String string) {
        return "1".equals(string) || "true".equalsIgnoreCase(string);
    }
}
