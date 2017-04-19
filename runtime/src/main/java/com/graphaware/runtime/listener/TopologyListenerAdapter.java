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
import com.graphaware.runtime.config.util.InstanceRoleUtils;
import org.neo4j.graphdb.DependencyResolver;
import org.neo4j.kernel.impl.factory.OperationalMode;
import org.neo4j.kernel.internal.GraphDatabaseAPI;
import org.neo4j.logging.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is an adapter listening to topology changes both for HA clusters and Causal ones
 */
public final class TopologyListenerAdapter {

    protected final Log LOG = LoggerFactory.getLogger(TopologyListenerAdapter.class);

    List<TopologyChangeEventListener> topologyChangeEventListeners = new ArrayList<>();

    private DependencyResolver dependencyResolver;

    private OperationalMode operationalMode;

    private TopologyListener topologyListener;

    public TopologyListenerAdapter(final GraphDatabaseAPI api) {
        operationalMode = new InstanceRoleUtils(api).getOperationalMode();
        dependencyResolver = api.getDependencyResolver();

        // HA
        if (operationalMode.equals(OperationalMode.ha)) {
            topologyListener = new HighAvailabilityClusterListener(dependencyResolver, this);
        }

        // Core
        if (operationalMode.equals(OperationalMode.core)) {
            topologyListener = new CausalClusterListener(dependencyResolver, this);
        }

        topologyListener.register();
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
        this.topologyListener.unregister();
    }

    /**
     * For each (registered) listener fire the event
     *
     * @param topologyChangeEvent
     */
    protected final void fireEvent(TopologyChangeEvent topologyChangeEvent) {
        this.topologyChangeEventListeners.forEach(listener -> listener.onTopologyChange(topologyChangeEvent));
    }
}