package com.graphaware.test;

import com.graphaware.test.integration.HighAvailabilityDatabaseIntegrationTest;
import org.junit.Test;
import org.neo4j.management.ClusterMemberInfo;
import org.neo4j.management.Neo4jManager;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;

public class HighAvailabilityDatabaseIntegrationTestTest extends HighAvailabilityDatabaseIntegrationTest {

    @Test
    public void shouldStartHighAvailabilityDatabase() {
        Neo4jManager manager = Neo4jManager.get();

        org.neo4j.management.HighAvailability highAvailabilityBean = manager.getHighAvailabilityBean();

        assertNotNull(highAvailabilityBean);

        final ClusterMemberInfo[] instancesInCluster = highAvailabilityBean.getInstancesInCluster();

        assertEquals(1, instancesInCluster.length);

        assertEquals("1", instancesInCluster[0].getInstanceId());
        assertEquals("master", instancesInCluster[0].getHaRole());
    }
}
