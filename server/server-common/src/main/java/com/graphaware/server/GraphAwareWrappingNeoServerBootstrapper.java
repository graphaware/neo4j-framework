/*
 * Copyright (c) 2015 GraphAware
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

package com.graphaware.server;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.kernel.GraphDatabaseAPI;
import org.neo4j.kernel.GraphDatabaseDependencies;
import org.neo4j.logging.LogProvider;
import org.neo4j.server.NeoServer;
import org.neo4j.server.WrappingNeoServerBootstrapper;
import org.neo4j.server.configuration.ConfigurationBuilder;
import org.neo4j.server.configuration.Configurator;
import org.neo4j.server.configuration.ServerConfigurator;

/**
 * {@link org.neo4j.server.WrappingNeoServerBootstrapper} that uses {@link com.graphaware.server.GraphAwareWrappingNeoServer}.
 */
public class GraphAwareWrappingNeoServerBootstrapper extends WrappingNeoServerBootstrapper {

    private final GraphDatabaseService database;
    private final Configurator configurator;

    public GraphAwareWrappingNeoServerBootstrapper(GraphDatabaseAPI db) {
        super(db);
        this.database = db;
        this.configurator = new ServerConfigurator(db);
    }

    public GraphAwareWrappingNeoServerBootstrapper(GraphDatabaseAPI db, Configurator configurator) {
        super(db, configurator);
        this.database = db;
        this.configurator = configurator;
    }

    @Override
    protected NeoServer createNeoServer(ConfigurationBuilder configurator, GraphDatabaseDependencies dependencies, LogProvider userLogProvider) {
        return new GraphAwareWrappingNeoServer((GraphDatabaseAPI) database, configurator);
    }
}
