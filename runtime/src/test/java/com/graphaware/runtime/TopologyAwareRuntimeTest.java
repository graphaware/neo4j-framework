package com.graphaware.runtime;

import com.graphaware.common.policy.role.InstanceRole;
import com.graphaware.runtime.listener.TopologyChangeEvent;
import com.graphaware.runtime.listener.TopologyChangeEventListener;
import com.graphaware.runtime.module.RuntimeModule;
import com.graphaware.test.integration.cluster.HighAvailabilityClusterDatabasesIntegrationTest;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.cluster.InstanceId;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.kernel.ha.cluster.member.ClusterMembers;
import org.neo4j.kernel.internal.GraphDatabaseAPI;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class TopologyAwareRuntimeTest extends HighAvailabilityClusterDatabasesIntegrationTest {

    private static ExecutorService executorService = Executors.newFixedThreadPool(10);

    private static List<GraphDatabaseService> databases;

    private static AtomicInteger clusterLeaveEvents = new AtomicInteger();

    private static AtomicInteger clusterElectionEvents = new AtomicInteger();

    private static AtomicInteger finalNumberOfMasters = new AtomicInteger();

    @AfterClass
    public static void tearDown() throws Exception {
        executorService.submit(() -> {
            try {
                System.out.println("wait before shutting down all the instances");
                Thread.sleep(5000);
                List<Map<InstanceId, String>> members = databases.stream()
                        .map(db -> ((GraphDatabaseAPI) db).getDependencyResolver().resolveDependency(ClusterMembers.class).getCurrentMember())
                        .map(currentMember -> {
                            if (currentMember != null) {
                                return Collections.singletonMap(currentMember.getInstanceId(), currentMember.getHARole());
                            }
                            return new HashMap<InstanceId, String>();
                        })
                        .collect(Collectors.toList());

                System.out.println("shutting down all the instances: " + members);
                databases.forEach(GraphDatabaseService::shutdown);

            } catch (Exception e) {

            }
        }).get();
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();

        databases = getDatabases();
    }

    @Override
    public boolean shouldRegisterModules() {
        return true;
    }

    @Override
    public void registerModules(GraphDatabaseService db) throws Exception {
        GraphAwareRuntime runtime = GraphAwareRuntimeFactory.createRuntime(db);
        runtime.registerModule(new TopologyAwareModule());
    }

    @Test
    public void testMasterShutdown() throws Exception {
        Future<?> future = executorService.submit(() -> {

            ClusterMembers clusterMembers = ((GraphDatabaseAPI) getMasterDatabase())
                    .getDependencyResolver()
                    .resolveDependency(ClusterMembers.class);
            LOG.info(String.format("shutdown instance %s", clusterMembers.getCurrentMember()));
            getMasterDatabase().shutdown();
        });
        future.get();

        Thread.sleep(2000);

        // We should have 2 "cluster leave" events: two for each remaining instance
        assertEquals(2, clusterLeaveEvents.intValue());
        // We should have 2 "cluster election" events: two for each remaining instance
        assertEquals(2, clusterElectionEvents.intValue());

        LOG.info("Cluster leave events: " + clusterLeaveEvents.intValue());
        LOG.info("Cluster election events: " + clusterElectionEvents.intValue());

        // Of course, we should have only one master at the end of the new election
        assertEquals(1, finalNumberOfMasters.intValue());
        LOG.info("Number of final master instances: " + finalNumberOfMasters.intValue());
    }

    class TopologyAwareModule implements RuntimeModule, TopologyChangeEventListener {

        @Override
        public String getId() {
            return "TOPOLOGY_AWARE";
        }

        @Override
        public void shutdown() {

        }

        public void onTopologyChange(TopologyChangeEvent topologyChangeEvent) {
            LOG.info("onTopologyChange: " + topologyChangeEvent);
            assertNotNull(topologyChangeEvent.getInstanceId());
            assertNotNull(topologyChangeEvent.getOwnInstanceId());
            assertNotNull(topologyChangeEvent.getOwnInstanceRole());

            if (topologyChangeEvent.getEventType().equals(TopologyChangeEvent.EventType.CLUSTER_LEAVE)) {
                clusterLeaveEvents.incrementAndGet();
            }

            if (topologyChangeEvent.getEventType().equals(TopologyChangeEvent.EventType.ELECTION)) {
                clusterElectionEvents.incrementAndGet();
            }

            if (topologyChangeEvent.getOwnInstanceRole().equals(InstanceRole.MASTER)) {
                finalNumberOfMasters.incrementAndGet();
            }
        }
    }
}
