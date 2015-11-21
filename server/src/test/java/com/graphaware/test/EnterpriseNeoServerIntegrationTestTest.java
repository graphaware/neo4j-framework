package com.graphaware.test;

import com.graphaware.test.integration.EnterpriseNeoTestServer;
import com.graphaware.test.integration.NeoTestServer;

public class EnterpriseNeoServerIntegrationTestTest extends NeoServerIntegrationTestTest {

    @Override
    protected NeoTestServer neoTestServer(String neo4jConfigFile, String neo4jServerConfigFile) {
        return new EnterpriseNeoTestServer(neo4jConfigFile, neo4jServerConfigFile);
    }
}
