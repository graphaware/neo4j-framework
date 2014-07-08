package com.graphaware.example.plugin;

import com.graphaware.test.integration.DatabaseIntegrationTest;
import com.graphaware.test.unit.GraphUnit;
import org.junit.Test;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;

import static org.junit.Assert.assertEquals;

/**
 * {@link DatabaseIntegrationTest} for {@link HelloWorldServerPlugin}.
 *
 * Tests the logic, but not the API.
 */
public class HelloWorldServerPluginTest extends DatabaseIntegrationTest {

    @Test
    public void shouldCreateAndReturnNode() {
        Node node = new HelloWorldServerPlugin().createHelloWorldNode((getDatabase()));

        try (Transaction tx = getDatabase().beginTx()) {
            assertEquals("world", node.getProperty("hello"));
            tx.success();
        }

        GraphUnit.assertSameGraph(getDatabase(), "CREATE (:HelloWorld {hello:'world'})");
    }
}
