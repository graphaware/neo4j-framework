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

package com.graphaware.example.plugin;

import com.graphaware.example.component.HelloWorldNodeCreator;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.server.plugins.*;

/**
 * Managed server extension ({@link ServerPlugin}) that creates and returns a hello world node.
 */
@Description("An extension to the Neo4j Server for creating a hello world node")
public class HelloWorldServerPlugin extends ServerPlugin {

    @Name("hello_world_node")
    @Description("Create and return a hello world node")
    @PluginTarget(GraphDatabaseService.class)
    public Node createHelloWorldNode(@Source GraphDatabaseService database) {
        return new HelloWorldNodeCreator(database).createHelloWorldNode();
    }
}
