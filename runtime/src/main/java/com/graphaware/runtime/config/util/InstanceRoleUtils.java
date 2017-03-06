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

package com.graphaware.runtime.config.util;

import org.neo4j.causalclustering.core.consensus.RaftMachine;
import org.neo4j.causalclustering.core.consensus.roles.Role;
import org.neo4j.graphdb.DependencyResolver;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.kernel.ha.cluster.member.ClusterMembers;
import org.neo4j.kernel.impl.factory.OperationalMode;
import org.neo4j.kernel.internal.GraphDatabaseAPI;
import org.neo4j.udc.UsageData;
import org.neo4j.udc.UsageDataKey;

/**
 * Utilities for the role of instance in the cluster
 */
public class InstanceRoleUtils {

//	private static final Log LOG = LoggerFactory.getLogger(InstanceRoleUtils.class);

	/**
	 * The database instance
	 */
	private final GraphDatabaseService database;

	/**
	 * Manage role on database instance
	 * 
	 * @param database
	 */
	public InstanceRoleUtils(GraphDatabaseService database) {
		super();
		this.database = database;
	}

	protected <T> T resolveDependency(Class<T> type) {
		DependencyResolver dependencyResolver = ((GraphDatabaseAPI) database).getDependencyResolver();
		return dependencyResolver.resolveDependency(type);
	}

	/**
	 * Get the cluster configuration mode
	 * 
	 * @return
	 */
	public OperationalMode getOperationalMode() {
		UsageData usageData = resolveDependency(UsageData.class);
		OperationalMode opMode = usageData.get(new UsageDataKey<OperationalMode>("neo4j.opMode", null));
		return opMode;
	}

	/**
	 * Get the role of the instance in the cluster 
	 * @return SINGLE if no cluster exists
	 */
	public InstanceRole getInstaceRole() {
		OperationalMode operationalMode = getOperationalMode();
		InstanceRole res = InstanceRole.SINGLE;
		switch (operationalMode) {
		case ha:
			res = getHARole();
			break;
		case core:
			res = getCoreRole();
			break;
		case read_replica:
			res = InstanceRole.READ_REPLICA;
			break;
		default:
			res = InstanceRole.SINGLE;
		}
		return res;
	}

	/**
	 * Role for causal cluster
	 * @return
	 */
	private InstanceRole getCoreRole() {
		Role role = resolveDependency(RaftMachine.class).currentRole();
		switch (role) {
		case LEADER:
			return InstanceRole.LEADER;
		case FOLLOWER:
			return InstanceRole.FOLLOWER;
		case CANDIDATE:
			return InstanceRole.CANDIDATE;
		default:
			return InstanceRole.SINGLE;
		}
	}

	/**
	 * Role for HA cluster 
	 * @return
	 */
	private InstanceRole getHARole() {
		String role = resolveDependency(ClusterMembers.class).getCurrentMemberRole();

		if ("master".equalsIgnoreCase(role)) {
			return InstanceRole.MASTER;
		}
		
		return InstanceRole.SLAVE;
	}

	/**
	 * Check if the instance has write permission
	 * 
	 * @return true if the instance cannot write into the database
	 */
	public boolean isReadOnly() {
		InstanceRole role = getInstaceRole();
		return !(role == InstanceRole.MASTER || role == InstanceRole.SINGLE || role == InstanceRole.LEADER);
	}
}
