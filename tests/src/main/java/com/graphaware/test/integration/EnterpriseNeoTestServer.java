package com.graphaware.test.integration;

import org.neo4j.server.Bootstrapper;
import org.neo4j.server.CommunityBootstrapper;
import org.neo4j.server.enterprise.EnterpriseBootstrapper;

/**
 * Enterprise {@link NeoTestServer}
 */
public class EnterpriseNeoTestServer extends NeoTestServer {

    public EnterpriseNeoTestServer() {
    }

    public EnterpriseNeoTestServer(String neo4jConfigFile, String neo4jServerConfigFile) {
        super(neo4jConfigFile, neo4jServerConfigFile);
    }

    @Override
    protected Bootstrapper createBootstrapper() {
        return new EnterpriseBootstrapper();
    }
}
