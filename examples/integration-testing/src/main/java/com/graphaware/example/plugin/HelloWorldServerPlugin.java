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
