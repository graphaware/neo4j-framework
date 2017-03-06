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
package com.graphaware.runtime.config;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.neo4j.causalclustering.core.CausalClusteringSettings;
import org.neo4j.causalclustering.readreplica.ReadReplicaGraphDatabase;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.kernel.impl.factory.GraphDatabaseFacadeFactory.Dependencies;

/**
 * Utility for reaa_replica in a (another) causal cluster 
 */
public class ReplicaCausalClusterIntegrationTestUtils extends AbstractClusterIntegrationTestUtils {

	@Override
	protected GraphDatabaseService newDatabaseInstance(File storeDir, Map<String, String> params,
			Dependencies dependencies) {
		return new  ReadReplicaGraphDatabase(storeDir, params, dependencies);
	}

	@Override
	protected Map<String, String> addictionalParams(int i, int clusterSize) {
		Map<String, String> params = new HashMap<>();
		
		params.put(CausalClusteringSettings.initial_discovery_members.name(), buildDiscoveryAddresses(5000,clusterSize));
		
		params.put("dbms.connector.bolt.enabled", "true");
		params.put("dbms.connector.bolt.listen_address", "localhost:"+ String.valueOf( 8000+i ));

		return params;
	}

}
