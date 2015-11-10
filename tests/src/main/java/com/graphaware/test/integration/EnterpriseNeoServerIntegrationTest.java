package com.graphaware.test.integration;

/**
 * Community {@link NeoServerIntegrationTest}.
 */
public class EnterpriseNeoServerIntegrationTest extends NeoServerIntegrationTest {

    /**
     * {@inheritDoc}
     */
    @Override
    protected NeoTestServer neoTestServer(String neo4jConfigFile, String neo4jServerConfigFile) {
        return new EnterpriseNeoTestServer(neo4jConfigFile, neo4jServerConfigFile);
    }
}
