package com.graphaware.test;

import com.graphaware.test.integration.CommunityNeoTestServer;
import com.graphaware.test.integration.NeoTestServer;

public class CommunityNeoServerIntegrationTestTest extends NeoServerIntegrationTestTest {

    @Override
    protected NeoTestServer neoTestServer(String neo4jConfigFile, String neo4jServerConfigFile) {
        return new CommunityNeoTestServer(neo4jConfigFile, neo4jServerConfigFile);
    }
}
