package com.graphaware.example.plugin;

import com.graphaware.test.integration.WrappingServerIntegrationTest;
import com.graphaware.test.unit.GraphUnit;
import com.graphaware.test.util.TestUtils;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * {@link com.graphaware.test.integration.DatabaseIntegrationTest} for {@link HelloWorldServerPlugin}.
 *
 * Tests the logic as well as the API.
 */
public class HelloWorldServerPluginApiTest extends WrappingServerIntegrationTest {

    @Test
    public void shouldCreateAndReturnNode() {
        TestUtils.get(baseNeoUrl() + "/db/data/ext/HelloWorldServerPlugin/graphdb/hello_world_node", 200);
        String result = TestUtils.post(baseNeoUrl() + "/db/data/ext/HelloWorldServerPlugin/graphdb/hello_world_node", 200);

        assertTrue(result.contains(" \"hello\" : \"world\""));
        GraphUnit.assertSameGraph(getDatabase(), "CREATE (:HelloWorld {hello:'world'})");
    }
}
