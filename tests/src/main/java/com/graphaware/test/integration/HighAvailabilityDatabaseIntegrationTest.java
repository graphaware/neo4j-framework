package com.graphaware.test.integration;

import org.neo4j.cluster.ClusterSettings;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.config.Setting;
import org.neo4j.graphdb.factory.GraphDatabaseBuilder;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.factory.HighlyAvailableGraphDatabaseFactory;
import org.neo4j.helpers.Settings;
import org.neo4j.kernel.ha.HaSettings;
import org.neo4j.server.configuration.Configurator;
import org.neo4j.server.web.ServerInternalSettings;
import org.neo4j.test.TestGraphDatabaseFactory;

import static com.graphaware.common.util.DatabaseUtils.registerShutdownHook;
import static org.neo4j.helpers.Settings.setting;
import static org.neo4j.helpers.Settings.STRING;

public class HighAvailabilityDatabaseIntegrationTest extends DatabaseIntegrationTest {

    private static final String SERVER_ID = "1";
    private static final String HA_SERVER = "localhost:6001";
    private static final String SLAVE_ONLY = Settings.FALSE;
    private static final String CLUSTER_SERVER = "localhost:5001";
    private static final String INITIAL_HOSTS = "localhost:5001";
    private static final String PATH = "target/graph-master";

    @Override
    protected GraphDatabaseService createDatabase() {
        //This is deprecated, but I didn't find another way how to do it properly.
        GraphDatabaseBuilder graphDatabaseBuilder = new HighlyAvailableGraphDatabaseFactory().newHighlyAvailableDatabaseBuilder(PATH);

        if (propertiesFile() != null) {
            graphDatabaseBuilder = graphDatabaseBuilder.loadPropertiesFromFile(propertiesFile());
        } else {
            setConfig(graphDatabaseBuilder);
        }

        GraphDatabaseService database = graphDatabaseBuilder.newGraphDatabase();
        registerShutdownHook(database);
        return database;
    }

    protected void setConfig(GraphDatabaseBuilder graphDatabaseBuilder) {
        graphDatabaseBuilder.setConfig(ClusterSettings.server_id, SERVER_ID);
        graphDatabaseBuilder.setConfig(HaSettings.ha_server, HA_SERVER);
        graphDatabaseBuilder.setConfig(HaSettings.slave_only, SLAVE_ONLY);
        graphDatabaseBuilder.setConfig(ClusterSettings.cluster_server, CLUSTER_SERVER);
        graphDatabaseBuilder.setConfig(ClusterSettings.initial_hosts, INITIAL_HOSTS);
    }
}
