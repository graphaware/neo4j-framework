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

import com.graphaware.runtime.module.ModuleBootstrapper;
import com.graphaware.runtime.module.Module;
import org.neo4j.configuration.SettingImpl;
import org.neo4j.configuration.SettingValueParsers;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.config.Setting;

import java.util.Map;

/**
 * {@link ModuleBootstrapper} for {@link TestModule}.
 */
public class TestModuleBootstrapper implements ModuleBootstrapper {

    public static final String MODULE_CONFIG;

    static {
        MODULE_CONFIG = "com.graphaware.module.test.1=" + TestModuleBootstrapper.class.getCanonicalName() + "," + "com.graphaware.module.test.configKey=configValue";
    }

    @Override
    public Module bootstrapModule(String moduleId, Map<String, String> config, GraphDatabaseService database) {
        return new TestModule(moduleId, config);
    }
}
