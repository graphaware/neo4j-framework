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

package com.graphaware.runtime.bootstrap;

import com.graphaware.runtime.module.RuntimeModuleBootstrapper;
import com.graphaware.runtime.module.TxDrivenModule;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.config.Setting;

import java.util.Map;

import static org.neo4j.kernel.configuration.Settings.*;

/**
 * {@link com.graphaware.runtime.module.RuntimeModuleBootstrapper} for {@link TestRuntimeModule}.
 */
public class TestModuleBootstrapper implements RuntimeModuleBootstrapper {

    public static final Setting<String> MODULE_ENABLED = setting("com.graphaware.module.test.1", STRING, TestModuleBootstrapper.class.getCanonicalName());
    public static final Setting<String> MODULE_CONFIG = setting("com.graphaware.module.test.configKey", STRING, "configValue");

    @Override
    public TxDrivenModule bootstrapModule(String moduleId, Map<String, String> config, GraphDatabaseService database) {
        return new TestRuntimeModule(moduleId, config);
    }
}
