package com.graphaware.example.component;

import com.graphaware.test.integration.DatabaseIntegrationTest;
import com.graphaware.test.unit.GraphUnit;
import org.junit.Test;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;

import static org.junit.Assert.assertEquals;

/**
 * {@link DatabaseIntegrationTest} for {@link com.graphaware.example.component.HelloWorldNodeCreator}.
 */
public class HelloWorldNodeCreatorTest extends DatabaseIntegrationTest {

    @Test
    public void shouldCreateAndReturnNode() {
        Node node = new HelloWorldNodeCreator(getDatabase()).createHelloWorldNode();

        try (Transaction tx = getDatabase().beginTx()) {
            assertEquals("world", node.getProperty("hello"));
            tx.success();
        }

        GraphUnit.assertSameGraph(getDatabase(), "CREATE (:HelloWorld {hello:'world'})");
    }
}
