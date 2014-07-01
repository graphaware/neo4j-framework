package com.graphaware.server;

import com.graphaware.test.integration.NeoServerIntegrationTest;
import org.apache.http.HttpStatus;
import org.junit.Test;

import static com.graphaware.test.util.TestUtils.get;

/**
 * Integration test for custom server that wires Spring components.
 */
public class EnterpriseNeoServerIntegrationTest extends NeoServerIntegrationTest {

    @Test
    public void componentsShouldBeWired() {
        get(baseUrl() + "/graphaware/greeting", HttpStatus.SC_OK);
    }

    @Test
    public void jarFilesShouldBeWired() {
        get(baseUrl() + "/graphaware/timetree/now", HttpStatus.SC_OK);
    }
}
