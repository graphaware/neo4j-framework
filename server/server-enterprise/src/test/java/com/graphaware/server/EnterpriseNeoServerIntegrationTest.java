package com.graphaware.server;

import com.graphaware.test.integration.NeoServerIntegrationTest;
import com.graphaware.test.util.TestUtils;
import org.apache.http.HttpStatus;
import org.junit.Test;

import static com.graphaware.test.util.TestUtils.*;

/**
 * Integration test for custom server that wires Spring components.
 */
public class EnterpriseNeoServerIntegrationTest extends NeoServerIntegrationTest {

    @Test
    public void componentsShouldBeWired() {
        get("http://localhost:7474/graphaware/greeting", HttpStatus.SC_OK);
    }

    @Test
    public void jarFilesShouldBeWired() {
        post("http://localhost:7474/graphaware/timetree/now", HttpStatus.SC_OK);
    }
}
