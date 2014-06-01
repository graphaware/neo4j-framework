package com.graphaware.example;

import com.graphaware.test.integration.ServerIntegrationTest;
import com.graphaware.test.integration.ServerIntegrationTest;
import org.apache.http.HttpStatus;
import org.junit.Test;

import static com.graphaware.test.util.TestUtils.get;
import static org.junit.Assert.assertEquals;

/**
 * {@link com.graphaware.test.integration.ServerIntegrationTest} for {@link NodeCountApi}.
 */
public class NodeCountServerIntegrationTest extends ServerIntegrationTest {

    @Test
    public void apiShouldBeMounted() {
        assertEquals("0", get("http://localhost:7474/graphaware/count", HttpStatus.SC_OK));
    }
}
