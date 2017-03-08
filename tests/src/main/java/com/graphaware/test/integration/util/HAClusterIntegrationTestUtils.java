/*
 * Copyright (c) 2013-2016 GraphAware
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
package com.graphaware.test.integration.util;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.neo4j.causalclustering.core.CausalClusteringSettings;
import org.neo4j.cluster.ClusterSettings;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.kernel.ha.HaSettings;
import org.neo4j.kernel.ha.HighlyAvailableGraphDatabase;
import org.neo4j.kernel.impl.factory.GraphDatabaseFacadeFactory.Dependencies;

/**
 * Utility for HA cluster
 */
public class HAClusterIntegrationTestUtils extends AbstractClusterIntegrationTestUtils {

	@Override
	protected GraphDatabaseService newDatabaseInstance(File storeDir, Map<String, String> params,
			Dependencies dependencies) {
		return new HighlyAvailableGraphDatabase(storeDir, params, dependencies);
	}

	@Override
	protected Map<String, String> addictionalParams(int i, int clusterSize) {
		Map<String, String> params = new HashMap<>();
		
		params.put(CausalClusteringSettings.expected_core_cluster_size.name(), String.valueOf(clusterSize));
		
		params.put(ClusterSettings.server_id.name(), String.valueOf(i));
		params.put(HaSettings.ha_server.name(), "localhost:600"+i);//"ha.host.data"
		params.put(ClusterSettings.cluster_server.name(), "localhost:500"+i);//"ha.host.coordination"
		params.put(ClusterSettings.initial_hosts.name(), buildDiscoveryAddresses(5000,clusterSize));//ha.initial_hosts
		
		return params;
	}

}
