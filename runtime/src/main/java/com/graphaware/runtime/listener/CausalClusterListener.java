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
import org.neo4j.causalclustering.discovery.CoreTopology;
import org.neo4j.causalclustering.discovery.CoreTopologyService;
import org.neo4j.graphdb.DependencyResolver;
import org.neo4j.logging.Log;

public class CausalClusterListener implements CoreTopologyService.Listener, TopologyListener {

    private final Log LOG = LoggerFactory.getLogger(CausalClusterListener.class);

    private final DependencyResolver dependencyResolver;

    private final TopologyListenerAdapter adapter;

    public CausalClusterListener(DependencyResolver dependencyResolver, TopologyListenerAdapter adapter) {
        this.dependencyResolver = dependencyResolver;
        this.adapter = adapter;
    }

    @Override
    public void register() {
        dependencyResolver.resolveDependency(CoreTopologyService.class).addCoreTopologyListener(this);
    }

    @Override
    public void unregister() {
        // There is no removeListener for Causal Cluster
        // DO NOTHING
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

    // ----- CAUSAL CLUSTER EVENT -----

    @Override
    public void onCoreTopologyChange(CoreTopology coreTopology) {
        LOG.info(String.format("onCoreTopologyChange %s", coreTopology));
        adapter.fireEvent(topologyChangeEventFromCausalCluster(coreTopology));
    }
}
