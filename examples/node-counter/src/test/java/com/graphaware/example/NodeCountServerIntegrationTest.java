package com.graphaware.example;

import com.graphaware.test.integration.NeoServerIntegrationTest;
import org.apache.http.HttpStatus;
import org.junit.Test;

import static com.graphaware.test.util.TestUtils.get;
import static org.junit.Assert.assertEquals;

/**
 * {@link com.graphaware.test.integration.NeoServerIntegrationTest} for {@link NodeCountApi}.
 */
public class NodeCountServerIntegrationTest extends NeoServerIntegrationTest {

    @Test
    public void apiShouldBeMounted() {
        assertEquals("0", get("http://localhost:7474/graphaware/count", HttpStatus.SC_OK));
    }
}
