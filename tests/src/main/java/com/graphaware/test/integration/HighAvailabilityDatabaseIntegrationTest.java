package com.graphaware.test.integration;

import org.neo4j.cluster.ClusterSettings;
import org.neo4j.graphdb.factory.GraphDatabaseBuilder;
import org.neo4j.graphdb.factory.HighlyAvailableGraphDatabaseFactory;
import org.neo4j.helpers.Settings;
import org.neo4j.kernel.ha.HaSettings;

public class HighAvailabilityDatabaseIntegrationTest extends DatabaseIntegrationTest {

    private static final String SERVER_ID = "1";
    private static final String HA_SERVER = "localhost:6001";
    private static final String SLAVE_ONLY = Settings.FALSE;
    private static final String CLUSTER_SERVER = "localhost:5001";
    private static final String INITIAL_HOSTS = "localhost:5001";
    private static final String PATH = "target/graph-master";

    @Override
    protected GraphDatabaseBuilder createGraphDatabaseBuilder() {
        return new HighlyAvailableGraphDatabaseFactory().newHighlyAvailableDatabaseBuilder(getPath());
    }

    @Override
    protected void populateConfig(GraphDatabaseBuilder builder) {
        super.populateConfig(builder);

        builder.setConfig(ClusterSettings.server_id, getServerId());
        builder.setConfig(HaSettings.ha_server, getHaServer());
        builder.setConfig(HaSettings.slave_only, getSlaveOnly());
        builder.setConfig(ClusterSettings.cluster_server, getClusterServer());
        builder.setConfig(ClusterSettings.initial_hosts, getInitialHosts());
    }

    protected String getServerId() {
        return SERVER_ID;
    }

    protected String getHaServer() {
        return HA_SERVER;
    }

    protected String getSlaveOnly() {
        return SLAVE_ONLY;
    }

    protected String getClusterServer() {
        return CLUSTER_SERVER;
    }

    protected String getInitialHosts() {
        return INITIAL_HOSTS;
    }

    protected String getPath() {
        return PATH;
    }
}
