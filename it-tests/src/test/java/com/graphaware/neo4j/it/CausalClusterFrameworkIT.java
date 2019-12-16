package com.graphaware.neo4j.it;

import org.junit.Test;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;

public class CausalClusterFrameworkIT {


    @Test
    public void shouldStartInCluster() {
        ContainerizedCausalCluster cluster = new ContainerizedCausalCluster("target/", container -> {
            container
                    .withEnv("NEO4J_dbms_unmanaged__extension__classes", "com.graphaware.server=/graphaware")
                    .withEnv("NEO4J_com_graphaware_runtime_enabled", "true")
            ;
        });

        cluster.startCluster();

        assertThat(cluster.logs())
                .filteredOn(log -> log.contains("Mounting GraphAware Framework at /graphaware"))
                .hasSize(1);

        cluster.shutdownCluster();
    }

    @Test
    public void shouldStartAfterLeaderChange() {

    }
}
