package com.graphaware.example.plugin;

import com.graphaware.test.integration.NeoServerIntegrationTest;
import com.graphaware.test.util.TestUtils;
import org.junit.Test;

/**
 * {@link NeoServerIntegrationTest} for {@link HelloWorldServerPlugin}.
 * <p/>
 * Only tests the actual deployment of the extension, not so much the logic.
 */
public class HelloWorldServerPluginDeploymentTest extends NeoServerIntegrationTest {

    @Test
    public void shouldCreateAndReturnNode() {
        TestUtils.get(baseUrl() + "/db/data/ext/HelloWorldServerPlugin/graphdb/hello_world_node", 200);
        TestUtils.post(baseUrl() + "/db/data/ext/HelloWorldServerPlugin/graphdb/hello_world_node", 200);
    }
}
