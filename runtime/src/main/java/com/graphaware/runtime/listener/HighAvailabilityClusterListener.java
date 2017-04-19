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
import org.neo4j.cluster.InstanceId;
import org.neo4j.cluster.client.ClusterClient;
import org.neo4j.cluster.protocol.cluster.ClusterConfiguration;
import org.neo4j.cluster.protocol.cluster.ClusterListener;
import org.neo4j.graphdb.DependencyResolver;
import org.neo4j.kernel.ha.cluster.member.ClusterMember;
import org.neo4j.kernel.ha.cluster.member.ClusterMembers;
import org.neo4j.logging.Log;

import java.net.URI;

public class HighAvailabilityClusterListener implements ClusterListener, TopologyListener {

    protected final Log LOG = LoggerFactory.getLogger(HighAvailabilityClusterListener.class);

    private final DependencyResolver dependencyResolver;

    private final TopologyListenerAdapter adapter;

    public HighAvailabilityClusterListener(DependencyResolver dependencyResolver, TopologyListenerAdapter adapter) {
        this.dependencyResolver = dependencyResolver;
        this.adapter = adapter;
    }

    @Override
    public void register() {
        dependencyResolver.resolveDependency(ClusterClient.class).addClusterListener(this);
    }

    @Override
    public void unregister() {
        dependencyResolver.resolveDependency(ClusterClient.class).removeClusterListener(this);
    }

    private ClusterMembers getHAClusterMembers() {
        return dependencyResolver.resolveDependency(ClusterMembers.class);
    }

    // TODO use InstanceRoleUtils instead of this method?
    private InstanceRole haInstanceRoleFromString(String haInstanceRole) {
        return haInstanceRole.equals("master") ? InstanceRole.MASTER : InstanceRole.SLAVE;
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
        adapter.fireEvent(topologyChangeEventFromHighAvailability(instanceId, TopologyChangeEvent.EventType.CLUSTER_JOIN));
    }

    @Override
    public void leftCluster(InstanceId instanceId, URI uri) {
        adapter.fireEvent(topologyChangeEventFromHighAvailability(instanceId, TopologyChangeEvent.EventType.CLUSTER_LEAVE));
    }

    @Override
    public void elected(String s, InstanceId instanceId, URI uri) {
        adapter.fireEvent(topologyChangeEventFromHighAvailabilityElection(instanceId));
    }

    @Override
    public void unelected(String s, InstanceId instanceId, URI uri) {
        // DO NOTHING
    }
}
