package com.graphaware.test.integration;

/**
 * Community {@link NeoServerIntegrationTest}.
 */
public class CommunityNeoServerIntegrationTest extends NeoServerIntegrationTest {

    /**
     * {@inheritDoc}
     */
    @Override
    protected NeoTestServer neoTestServer(String neo4jConfigFile, String neo4jServerConfigFile) {
        return new CommunityNeoTestServer(neo4jConfigFile, neo4jServerConfigFile);
    }
}
