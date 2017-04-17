/*
 * Copyright (c) 2013-2017 GraphAware
 *
 * This file is part of the GraphAware Framework.
 *
 * GraphAware Framework is free software: you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation, either
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
import com.graphaware.runtime.config.util.InstanceRoleUtils;
import org.neo4j.causalclustering.discovery.CoreTopology;
import org.neo4j.causalclustering.discovery.CoreTopologyService;
import org.neo4j.cluster.InstanceId;
import org.neo4j.cluster.client.ClusterClient;
import org.neo4j.cluster.protocol.cluster.ClusterConfiguration;
import org.neo4j.cluster.protocol.cluster.ClusterListener;
import org.neo4j.graphdb.DependencyResolver;
import org.neo4j.kernel.ha.cluster.member.ClusterMember;
import org.neo4j.kernel.ha.cluster.member.ClusterMembers;
import org.neo4j.kernel.impl.factory.OperationalMode;
import org.neo4j.kernel.internal.GraphDatabaseAPI;
import org.neo4j.logging.Log;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This class is an adapter listening to topology changes both for HA clusters and Causal ones
 */
public final class TopologyListenerAdapter implements ClusterListener, CoreTopologyService.Listener {

    protected final Log LOG = LoggerFactory.getLogger(TopologyListenerAdapter.class);

    List<TopologyChangeEventListener> topologyChangeEventListeners = new ArrayList<>();

    private DependencyResolver dependencyResolver;

    private OperationalMode operationalMode;

    public TopologyListenerAdapter(final GraphDatabaseAPI api) {
        operationalMode = new InstanceRoleUtils(api).getOperationalMode();
        dependencyResolver = api.getDependencyResolver();

        // HA cluster
        if (operationalMode.equals(OperationalMode.ha)) {
            dependencyResolver.resolveDependency(ClusterClient.class).addClusterListener(this);
        }
        // Core
        if (operationalMode.equals(OperationalMode.core)) {
            dependencyResolver.resolveDependency(CoreTopologyService.class).addCoreTopologyListener(this);
        }
    }

    /**
     * Register a listener for topology change events
     *
     * @param topologyChangeEventListener
     */
    public void registerListener(TopologyChangeEventListener topologyChangeEventListener) {
        this.topologyChangeEventListeners.add(topologyChangeEventListener);
    }

    /**
     * Remove a listener
     *
     * @param topologyChangeEventListener
     */
    public void removeListener(TopologyChangeEventListener topologyChangeEventListener) {
        this.topologyChangeEventListeners.remove(topologyChangeEventListener);
    }

    /**
     * Un-register this adapter
     */
    public void unregister() {
        this.topologyChangeEventListeners.clear();

        if (operationalMode.equals(OperationalMode.ha)) {
            dependencyResolver.resolveDependency(ClusterClient.class).removeClusterListener(this);
        }

        // There is no removeListener for Causal Cluster
    }

    /**
     * For each (registered) listener fire the event
     *
     * @param topologyChangeEvent
     */
    private void fireEvent(TopologyChangeEvent topologyChangeEvent) {
        this.topologyChangeEventListeners.forEach(listener -> listener.onTopologyChange(topologyChangeEvent));
    }

    /**
     * Creates a {@link TopologyChangeEvent} from a {@link CoreTopology} object
     * This is used only in case of Causal Cluster mode
     *
     * @param coreTopology
     * @return
     */
    private TopologyChangeEvent topologyChangeEventFromCausalCluster(CoreTopology coreTopology) {
        // TODO: convert coreTopology into TopologyChangeEvent
        return null;
    }

    /**
     * Creates a {@link TopologyChangeEvent} from a {@link InstanceId} object
     * This is used only in case of HA mode
     *
     * @param instanceId
     * @return
     */
    private TopologyChangeEvent topologyChangeEventFromHighAvailability(final InstanceId instanceId, final TopologyChangeEvent.EventType eventType) {
        // This member, "this" instance receiving the event
        final ClusterMember thisMember = getHAClusterMembers().getCurrentMember();

        return new TopologyChangeEventImpl(instanceId.toString(),
                thisMember.getInstanceId().toString(),
                haInstanceRoleFromString(thisMember.getHARole()),
                eventType);
    }

    /**
     * @param instanceId
     * @return
     */
    private TopologyChangeEvent topologyChangeEventFromHighAvailabilityElection(final InstanceId instanceId) {
        final ClusterMember thisMember = getHAClusterMembers().getCurrentMember();

        // TODO: give explanation to this logic
        InstanceRole role = InstanceRole.SLAVE;
        // FIXME
        if (instanceId.equals(thisMember.getInstanceId())) {
            role = InstanceRole.MASTER;
        }

        return new TopologyChangeEventImpl(instanceId.toString(),
                thisMember.getInstanceId().toString(),
                role,
                TopologyChangeEvent.EventType.ELECTION);
    }

    private ClusterMembers getHAClusterMembers() {
        return dependencyResolver.resolveDependency(ClusterMembers.class);
    }

    // TODO use InstanceRoleUtils instead of this method?
    private InstanceRole haInstanceRoleFromString(String haInstanceRole) {
        return haInstanceRole.equals("master") ? InstanceRole.MASTER : InstanceRole.SLAVE;
    }

    // ----- HIGH AVAILABILITY EVENTS -----

    @Override
    public void enteredCluster(ClusterConfiguration clusterConfiguration) {
        // DO NOTHING
    }

    @Override
    public void leftCluster() {
        // DO NOTHING
    }

    @Override
    public void joinedCluster(InstanceId instanceId, URI uri) {
        fireEvent(topologyChangeEventFromHighAvailability(instanceId, TopologyChangeEvent.EventType.CLUSTER_JOIN));
    }

    @Override
    public void leftCluster(InstanceId instanceId, URI uri) {
        fireEvent(topologyChangeEventFromHighAvailability(instanceId, TopologyChangeEvent.EventType.CLUSTER_LEAVE));
    }

    @Override
    public void elected(String s, InstanceId instanceId, URI uri) {
        fireEvent(topologyChangeEventFromHighAvailabilityElection(instanceId));
    }

    @Override
    public void unelected(String s, InstanceId instanceId, URI uri) {
        // DO NOTHING
    }

    // ----- CAUSAL CLUSTER EVENT -----

    @Override
    public void onCoreTopologyChange(CoreTopology coreTopology) {
        LOG.info(String.format("onCoreTopologyChange %s", coreTopology));
        fireEvent(topologyChangeEventFromCausalCluster(coreTopology));
    }

    // ----- Inner implementation for a TopologyChangeEvent ---- ///

    private class TopologyChangeEventImpl implements TopologyChangeEvent {

        /**
         * The instance id (joining or leaving the cluster so that generating the event)
         */
        private final String instanceId;

        /**
         * The instance id receiving the event
         */
        private final String ownInstanceId;

        /**
         * The event type
         */
        private final EventType eventType;

        /**
         * The instance role receiving the event
         */
        private final InstanceRole ownInstanceRole;

        public TopologyChangeEventImpl(String instanceId, String ownInstanceId, InstanceRole ownInstanceRole, EventType eventType) {
            this.instanceId = instanceId;
            this.ownInstanceId = ownInstanceId;
            this.ownInstanceRole = ownInstanceRole;
            this.eventType = eventType;
        }

        @Override
        public String getInstanceId() {
            return instanceId;
        }

        @Override
        public String getOwnInstanceId() {
            return ownInstanceId;
        }

        @Override
        public InstanceRole getOwnInstanceRole() {
            return ownInstanceRole;
        }

        public EventType getEventType() {
            return eventType;
        }

        @Override
        public String toString() {
            return Stream.of("[instanceId=", getInstanceId(),
                    ",ownInstanceId=", getOwnInstanceId(),
                    ",ownInstanceRole=", getOwnInstanceRole().toString(),
                    ",eventType=", getEventType().toString() + "]")
                    .collect(Collectors.joining());
        }
    }
}