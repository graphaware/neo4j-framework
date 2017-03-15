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
package com.graphaware.test.integration.cluster;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.neo4j.causalclustering.core.CausalClusteringSettings;
import org.neo4j.causalclustering.core.CoreGraphDatabase;
import org.neo4j.causalclustering.readreplica.ReadReplicaGraphDatabase;
import org.neo4j.graphdb.GraphDatabaseService;

/**
 * Superclass for Causal Cluster integration testing
 */
public abstract class CausalClusterDatabasesintegrationTest extends ClusterDatabasesIntegrationTest {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.graphaware.test.integration.cluster.ClusterDatabasesIntegrationTest#
	 * getTopology()
	 */
	@Override
	protected List<Class<?>> getTopology() {
		return Arrays.asList(CoreGraphDatabase.class, CoreGraphDatabase.class, CoreGraphDatabase.class,
				ReadReplicaGraphDatabase.class, ReadReplicaGraphDatabase.class);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.graphaware.test.integration.cluster.ClusterDatabasesIntegrationTest#
	 * addictionalParams(int, java.lang.Class)
	 */
	@Override
	protected Map<String, String> addictionalParams(int i, Class<?> instanceClass) {
		Map<String, String> params = new HashMap<>();

		int coreClusterSize = 3;
		params.put(CausalClusteringSettings.initial_discovery_members.name(),
				buildDiscoveryAddresses(5000, coreClusterSize));

		params.put("dbms.connector.bolt.enabled", "true");
		params.put("dbms.connector.bolt.listen_address", "localhost:" + String.valueOf(7687 + i));

		if (instanceClass.equals(CoreGraphDatabase.class)) {
			params.put(CausalClusteringSettings.expected_core_cluster_size.name(), String.valueOf(coreClusterSize));

			params.put(CausalClusteringSettings.discovery_listen_address.name(), "localhost:500" + i);
			params.put(CausalClusteringSettings.transaction_listen_address.name(), "localhost:600" + i);
			params.put(CausalClusteringSettings.raft_listen_address.name(), "localhost:700" + i);
		}

		return params;
	}

	/**
	 * The master instance
	 * 
	 * @return
	 */
	protected GraphDatabaseService getLeaderDatabase() {
		return getMainDatabase();
	}

	/**
	 * One of two slaves
	 * 
	 * @return
	 */
	protected GraphDatabaseService getOneFollowerDatabase() {
		return getDatabases().get(1);
	}
	
	protected GraphDatabaseService getOneReplicaDatabase() {
		return getReplicas().get(0);
	}

	/**
	 * Get the followers nodes
	 * @return
	 */
	protected List<GraphDatabaseService> getFollowers() {
		List<GraphDatabaseService> followers = new ArrayList<>();
		
		List<Class<?>> topos = getTopology();
		int i = 0;
		for (Class<?> top : topos) {
			if(top.equals(CoreGraphDatabase.class)){
				followers.add(getDatabases().get(i));
			}
			i++;
		}
		
		return followers.subList(1, followers.size());
	}
	
	/**
	 * Get the replica nodes
	 * @return
	 */
	protected List<GraphDatabaseService> getReplicas() {
		List<GraphDatabaseService> replicas = new ArrayList<>();
		
		List<Class<?>> topos = getTopology();
		int i = 0;
		for (Class<?> top : topos) {
			if(top.equals(ReadReplicaGraphDatabase.class)){
				replicas.add(getDatabases().get(i));
			}
			i++;
		}
		
		return replicas;
	}
}
