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

package com.graphaware.runtime.module;

import org.apache.commons.configuration2.Configuration;
import org.neo4j.graphdb.GraphDatabaseService;

import java.util.Map;

/**
 * Component that automatically bootstraps a {@link Module} based on config parameters passed to Neo4j.
 * <p>
 * Implementations can expect that if there is the following entry in neo4j.conf
 * <p>
 * com.graphaware.module.x.y = z
 * <p>
 * where x is the ID of the module, y is the order in which the module will be registered with respect to other modules,
 * and z is the fully qualified class name of the bootstrapper implementation, then x will be passed to the {@link #bootstrapModule(String, java.util.Map, org.neo4j.graphdb.GraphDatabaseService)}
 * method of an instance of z as the first parameter (moduleId). Moreover, from all other entries of the form
 * <p>
 * com.graphaware.module.x.a = b
 * <p>
 * a map with a's as keys and b's as values will be passed as the second parameter (config) to the {@link #bootstrapModule(String, java.util.Map, org.neo4j.graphdb.GraphDatabaseService)}
 * method. {@link ModuleBootstrapper} implementations should document, which key-value configurations
 * they expect.
 *
 * @see com.graphaware.runtime.bootstrap.RuntimeKernelExtension
 */
public interface ModuleBootstrapper {

    /**
     * Create a new instance of a module.
     *
     * @param moduleId ID of the module.
     * @param config   for this module.
     * @param database which the module will run on.
     * @return fully configured runtime module.
     */
    Module<?> bootstrapModule(String moduleId, Configuration config, GraphDatabaseService database);
}
