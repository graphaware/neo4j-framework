package com.graphaware.test.integration;

import org.neo4j.server.Bootstrapper;
import org.neo4j.server.CommunityBootstrapper;

/**
 * Community {@link NeoTestServer}
 */
public class CommunityNeoTestServer extends NeoTestServer {

    public CommunityNeoTestServer() {
    }

    public CommunityNeoTestServer(String neo4jConfigFile, String neo4jServerConfigFile) {
        super(neo4jConfigFile, neo4jServerConfigFile);
    }

    @Override
    protected Bootstrapper createBootstrapper() {
        return new CommunityBootstrapper();
    }
}
