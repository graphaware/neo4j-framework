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

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.kernel.configuration.Config;
import org.neo4j.kernel.lifecycle.Lifecycle;

/**
 *
 */
public class TestExtension implements Lifecycle {

    private final Config config;
    private final GraphDatabaseService database;

    public TestExtension(Config config, GraphDatabaseService database) {
        this.config = config;
        this.database = database;
    }

    @Override
    public void init() throws Throwable {
        System.out.println(config.getParams().toString());
        System.out.println("INIT");
    }

    @Override
    public void start() throws Throwable {
        System.out.println("START");
    }

    @Override
    public void stop() throws Throwable {
        System.out.println("STOP");
    }

    @Override
    public void shutdown() throws Throwable {
        System.out.println("SHUTDOWN");
    }
}
