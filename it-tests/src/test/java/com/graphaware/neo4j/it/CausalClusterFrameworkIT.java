package com.graphaware.neo4j.it;

import org.junit.After;
import org.junit.Test;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.Session;

import static org.assertj.core.api.Assertions.assertThat;

public class CausalClusterFrameworkIT {

    private ContainerizedCausalCluster cluster;

    @After
    public void tearDown() throws Exception {
        if (cluster != null) {
            cluster.shutdownCluster();
            cluster = null;
        }
    }

    @Test
    public void shouldStartInCluster() {
        cluster = new ContainerizedCausalCluster("target/", container -> {
            container
                    .withEnv("NEO4J_dbms_unmanaged__extension__classes", "com.graphaware.server=/graphaware")
                    .withEnv("NEO4J_com_graphaware_runtime_enabled", "true")
            ;
        });

        cluster.startCluster();

        assertThat(cluster.logs())
                .filteredOn(log -> log.contains("Mounting GraphAware Framework at /graphaware"))
                .hasSize(1);

    }

    @Test
    public void shouldStartTxModuleOnLeader() {
        ContainerizedCausalCluster cluster = new ContainerizedCausalCluster("target/", container -> {
            container
                    .withEnv("NEO4J_dbms_unmanaged__extension__classes", "com.graphaware.server=/graphaware")
                    .withEnv("NEO4J_com_graphaware_runtime_enabled", "true")
                    .withEnv("NEO4J_com_graphaware_module_friendshipcounter_1", "com.graphaware.example.module.FriendshipStrengthModuleBootstrapper");
        });

        cluster.startCluster();

        try (Driver driver = GraphDatabase.driver(cluster.boltRoutingUrl())) {

            try (Session session = driver.session()) {
                session.run("CREATE (:Person {name:'Eddard Stark'})-[:FRIEND_OF {strength:42}]->(:Person {name:'Robert Baratheon'})")
                        .consume();
                long totalStrength = session.run("MATCH (n:FriendshipCounter) RETURN n.totalFriendshipStrength AS total")
                        .single().get("total").asLong();

                assertThat(totalStrength).isEqualTo(42);
            }
        }

    }

    @Test
    public void moduleShouldWorkOnChangedLeader() {
        ContainerizedCausalCluster cluster = new ContainerizedCausalCluster("target/", container -> {
            container
                    .withEnv("NEO4J_dbms_unmanaged__extension__classes", "com.graphaware.server=/graphaware")
                    .withEnv("NEO4J_com_graphaware_runtime_enabled", "true")
                    .withEnv("NEO4J_com_graphaware_module_friendshipcounter_1", "com.graphaware.example.module.FriendshipStrengthModuleBootstrapper");
        });

        cluster.startCluster();
        cluster.restartLeader();


        Driver driver = cluster.getDriver();
        try (Session session = driver.session()) {
            session.writeTransaction(tx -> tx.run("CREATE (:Person {name:'Eddard Stark'})-[:FRIEND_OF {strength:42}]->(:Person {name:'Robert Baratheon'})")
                    .consume());

            long totalStrength = session.readTransaction(tx -> tx.run("MATCH (n:FriendshipCounter) RETURN n.totalFriendshipStrength AS total")
                    .single().get("total").asLong());

            assertThat(totalStrength).isEqualTo(42);
        }

    }

    @Test
    public void shouldStartOnExistingData() throws Exception {
        ContainerizedCausalCluster cluster = new ContainerizedCausalCluster("target/", container -> {
            container
                    .withEnv("NEO4J_dbms_unmanaged__extension__classes", "com.graphaware.server=/graphaware")
                    .withEnv("NEO4J_com_graphaware_runtime_enabled", "true")
                    .withEnv("NEO4J_com_graphaware_module_friendshipcounter_1", "com.graphaware.example.module.FriendshipStrengthModuleBootstrapper");
        });

        cluster.startCluster();

        Thread.sleep(10_000);


        Driver driver = cluster.getDriver();
        try (Session session = driver.session()) {
            session.writeTransaction(tx -> tx.run("CREATE (:Person {name:'Eddard Stark'})-[:FRIEND_OF {strength:42}]->(:Person {name:'Robert Baratheon'})")
                    .consume());

            session.writeTransaction(tx -> tx.run("MATCH (n:FriendshipCounter) DETACH DELETE n")
                    .consume());
        }
        int leader = cluster.leader();
        cluster.restartCore(leader);

        try (Session session = driver.session()) {
            long totalStrength = session.readTransaction(tx -> tx.run("MATCH (n:FriendshipCounter) RETURN n.totalFriendshipStrength AS total")
                    .single().get("total").asLong());

            assertThat(totalStrength).isEqualTo(42);
        }

    }
}
