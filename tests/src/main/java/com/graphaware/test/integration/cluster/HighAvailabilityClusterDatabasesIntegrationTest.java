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
package com.graphaware.test.integration.cluster;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.neo4j.cluster.ClusterSettings;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.kernel.ha.HaSettings;
import org.neo4j.kernel.ha.HighlyAvailableGraphDatabase;

/**
 *	Superclass for *HighAvailabilityTest
 */
public abstract class HighAvailabilityClusterDatabasesIntegrationTest extends ClusterDatabasesIntegrationTest{

	@Override
	protected List<Class<?>> getTopology() {
		// One master and two slaves
		return Arrays.asList(HighlyAvailableGraphDatabase.class, 
				HighlyAvailableGraphDatabase.class,
				HighlyAvailableGraphDatabase.class);
	}

	@Override
	protected Map<String, String> addictionalParams(int i, Class<?> instanceClass) {
		//instanceClass is always HighlyAvailableGraphDatabase
		Map<String, String> params = new HashMap<>();

		params.put(ClusterSettings.server_id.name(), String.valueOf(i));
		params.put(HaSettings.ha_server.name(), "localhost:600" + i);// "ha.host.data"
		params.put(ClusterSettings.cluster_server.name(), "localhost:510" + i);// "ha.host.coordination"
		params.put(ClusterSettings.initial_hosts.name(), buildDiscoveryAddresses(5100, getTopology().size()));// ha.initial_hosts

		return params;
	}

	/**
	 * The master instance
	 * @return
	 */
	protected GraphDatabaseService getMasterDatabase(){
		return getMainDatabase();
	}
	
	/**
	 * One of two slaves
	 * @return
	 */
	protected GraphDatabaseService getOneSlaveDatabase(){
		return getDatabases().get(1);
	}
	
	/**
	 * The two slave instances
	 * @return
	 */
	protected List<GraphDatabaseService> getSlaveDatabases(){
		return getDatabases().subList(1, getDatabases().size());
	}
}
