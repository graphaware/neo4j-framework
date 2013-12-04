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

package com.graphaware.bootstrap;

import com.graphaware.runtime.GraphAwareRuntime;
import com.graphaware.runtime.GraphAwareRuntimeModuleBootstrapper;
import org.apache.log4j.Logger;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.config.Setting;
import org.neo4j.kernel.AvailabilityGuard;
import org.neo4j.kernel.InternalAbstractGraphDatabase;
import org.neo4j.kernel.configuration.Config;
import org.neo4j.kernel.lifecycle.Lifecycle;

import java.lang.reflect.Field;
import java.util.Map;

import static org.neo4j.helpers.Settings.*;

/**
 * Extension that initializes the {@link com.graphaware.runtime.GraphAwareRuntime}.
 */
public class RuntimeKernelExtension implements Lifecycle {
    private static final Logger LOG = Logger.getLogger(RuntimeKernelExtension.class);

    public static final Setting<Boolean> RUNTIME_ENABLED = setting("com.graphaware.runtime.enabled", BOOLEAN, "false");
    private static final String MODULE_ENABLED_KEY = "com\\.graphaware\\.module\\.[a-z]*\\.enabled";

    private final Config config;
    private final GraphDatabaseService database;

    private GraphAwareRuntime runtime;

    public RuntimeKernelExtension(Config config, GraphDatabaseService database) {
        this.config = config;
        this.database = database;
    }

    @Override
    public void init() throws Throwable {

    }

    @Override
    public void start() throws Throwable {
        Map<String, String> params = config.getParams();

        if (!config.get(RUNTIME_ENABLED)) {
            LOG.info("GraphAware Runtime disabled.");
            return;
        }

        LOG.info("GraphAware Runtime enabled, bootstrapping...");

        runtime = new GraphAwareRuntime(database);

        for (String paramKey : params.keySet()) {
            if (paramKey.matches(MODULE_ENABLED_KEY)) {
                String paramValue = config.get(setting(paramKey, STRING, MANDATORY));
                LOG.info("Bootstrapping module with key " + paramKey + ", using " + paramValue);

                try {
                    GraphAwareRuntimeModuleBootstrapper bootstrapper
                            = (GraphAwareRuntimeModuleBootstrapper) Class.forName(paramValue).newInstance();
                    bootstrapper.bootstrap(runtime, config);
                } catch (Exception e) {
                    LOG.error("Unable to bootstrap module " + paramKey, e);
                }
            }
        }

        LOG.info("GraphAware Runtime bootstrapped.");

        //Extremely ugly (for the lack of a better way of doing this):
        Field availabilityGuardField = InternalAbstractGraphDatabase.class.getDeclaredField("availabilityGuard");
        availabilityGuardField.setAccessible(true);
        AvailabilityGuard availabilityGuard = (AvailabilityGuard) availabilityGuardField.get(database);
        availabilityGuard.addListener(new AvailabilityGuard.AvailabilityListener() {
            @Override
            public void available() {
                LOG.info("Database available, starting GraphAware Runtime...");

                runtime.start();

                LOG.info("GraphAware Runtime started.");
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
}
