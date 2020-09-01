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

package com.graphaware.runtime.config;

import org.neo4j.configuration.Config;
import org.neo4j.graphdb.GraphDatabaseService;

/**
 * Implementation of {@link RuntimeConfiguration} that loads bespoke settings from Neo4j's configuration properties, falling
 * back to default values when overrides aren't available. Intended for internal framework use, mainly for server deployments.
 */
public final class Neo4jConfigBasedRuntimeConfiguration extends BaseRuntimeConfiguration {

    /**
     * Constructs a new {@link Neo4jConfigBasedRuntimeConfiguration} based on the given Neo4j {@link Config}.
     *
     * @param config The {@link Config} containing the settings used to configure the runtime
     */
    public Neo4jConfigBasedRuntimeConfiguration(GraphDatabaseService database, Config config) {
        super(config);
    }
}
