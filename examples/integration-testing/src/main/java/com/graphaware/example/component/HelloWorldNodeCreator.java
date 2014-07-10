package com.graphaware.example.component;

import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.springframework.stereotype.Component;

/**
 * Very powerful class capable of creating a "Hello World" node. Intended for
 * demonstrating Neo4j integration testing with GraphAware Framework.
 */
public class HelloWorldNodeCreator {

    private final GraphDatabaseService database;

    public HelloWorldNodeCreator(GraphDatabaseService database) {
        this.database = database;
    }

    /**
     * Create a hello world node.
     *
     * @return created node.
     */
    public Node createHelloWorldNode() {
        Node node;

        try (Transaction tx = database.beginTx()) {
            node = database.createNode(DynamicLabel.label("HelloWorld"));
            node.setProperty("hello", "world");
            tx.success();
        }

        return node;
    }
}
