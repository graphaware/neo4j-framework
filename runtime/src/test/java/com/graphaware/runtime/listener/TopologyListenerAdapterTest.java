/*
 * Copyright (c) 2013-2018 GraphAware
 *
 * This file is part of the GraphAware Framework.
 *
 * GraphAware Framework is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received a copy of
 * the GNU General Public License along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 */

package com.graphaware.runtime.listener;

import com.graphaware.common.log.LoggerFactory;
import com.graphaware.common.policy.role.InstanceRole;
import com.graphaware.test.integration.cluster.HighAvailabilityClusterDatabasesIntegrationTest;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.cluster.InstanceId;
import org.neo4j.kernel.configuration.Config;
import org.neo4j.kernel.ha.HighlyAvailableGraphDatabase;
import org.neo4j.kernel.internal.GraphDatabaseAPI;
import org.neo4j.logging.Log;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;


public class TopologyListenerAdapterTest extends HighAvailabilityClusterDatabasesIntegrationTest {

    private static final InstanceId instanceId = new InstanceId(0);

    protected final Log LOG = LoggerFactory.getLogger(TopologyListenerAdapterTest.class);

    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    protected List<Class<?>> getTopology() {
        // One master
        return Arrays.asList(HighlyAvailableGraphDatabase.class);
    }

    @Test
    public void testRegisterListener() {
        TopologyListenerAdapter adapter = new TopologyListenerAdapter((GraphDatabaseAPI) getMasterDatabase(), Config.defaults());
        adapter.registerListener((TopologyChangeEvent topologyChangeEvent) -> {
        });

        assertEquals(1, adapter.topologyChangeEventListeners.size());
    }

    @Test
    public void testUnregisterListener() {
        TopologyListenerAdapter adapter = new TopologyListenerAdapter((GraphDatabaseAPI) getMasterDatabase(), Config.defaults());
        TopologyChangeEventListener listener = (TopologyChangeEvent topologyChangeEvent) -> {
        };
        adapter.registerListener(listener);

        assertEquals(1, adapter.topologyChangeEventListeners.size());

        adapter.removeListener(listener);
        assertEquals(0, adapter.topologyChangeEventListeners.size());
    }

    @Test
    public void testIfUnregisterShouldNotFireEvents() {
        TopologyListenerAdapter adapter = new TopologyListenerAdapter((GraphDatabaseAPI) getMasterDatabase(), Config.defaults());
        TopologyChangeEventListener listener = (TopologyChangeEvent topologyChangeEvent) -> {
            LOG.info("This should never fail");
            fail();
        };

        adapter.registerListener(listener);
        assertEquals(1, adapter.topologyChangeEventListeners.size());

        adapter.removeListener(listener);
        assertEquals(0, adapter.topologyChangeEventListeners.size());

        adapter.fireEvent(new TopologyListener.TopologyChangeEventImpl(
                instanceId.toString(),
                instanceId.toString(),
                InstanceRole.MASTER,
                TopologyChangeEvent.EventType.CLUSTER_JOIN));
    }

    @Test
    public void testJoinClusterEvent() {
        TopologyListenerAdapter adapter = new TopologyListenerAdapter((GraphDatabaseAPI) getMasterDatabase(), Config.defaults());
        adapter.registerListener((TopologyChangeEvent topologyChangeEvent) -> {
            assertEquals(instanceId.toString(), topologyChangeEvent.getInstanceId());
            assertEquals(TopologyChangeEvent.EventType.CLUSTER_JOIN, topologyChangeEvent.getEventType());
        });

        // fire fake event
        adapter.fireEvent(new TopologyListener.TopologyChangeEventImpl(
                instanceId.toString(),
                instanceId.toString(),
                InstanceRole.MASTER,
                TopologyChangeEvent.EventType.CLUSTER_JOIN));
    }

    @Test
    public void testLeaveClusterEvent() {
        TopologyListenerAdapter adapter = new TopologyListenerAdapter((GraphDatabaseAPI) getMasterDatabase(), Config.defaults());
        adapter.registerListener((TopologyChangeEvent topologyChangeEvent) -> {
            assertEquals(instanceId.toString(), topologyChangeEvent.getInstanceId());
            assertEquals(TopologyChangeEvent.EventType.CLUSTER_LEAVE, topologyChangeEvent.getEventType());
        });

        // fire fake event
        adapter.fireEvent(new TopologyListener.TopologyChangeEventImpl(
                instanceId.toString(),
                instanceId.toString(),
                InstanceRole.SLAVE,
                TopologyChangeEvent.EventType.CLUSTER_LEAVE));
    }

    @Test
    public void testElectionEvent() {
        TopologyListenerAdapter adapter = new TopologyListenerAdapter((GraphDatabaseAPI) getMasterDatabase(), Config.defaults());
        adapter.registerListener((TopologyChangeEvent topologyChangeEvent) -> {
            assertEquals(instanceId.toString(), topologyChangeEvent.getInstanceId());
            assertEquals(TopologyChangeEvent.EventType.ELECTION, topologyChangeEvent.getEventType());
            assertEquals(InstanceRole.MASTER, topologyChangeEvent.getOwnInstanceRole());
        });

        // fire fake event
        adapter.fireEvent(new TopologyListener.TopologyChangeEventImpl(
                instanceId.toString(),
                instanceId.toString(),
                InstanceRole.MASTER,
                TopologyChangeEvent.EventType.ELECTION));
    }
}
