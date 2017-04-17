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

package com.graphaware.runtime.config.util;

import com.graphaware.common.policy.role.InstanceRole;
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
	public InstanceRole getInstanceRole() {
		OperationalMode operationalMode = getOperationalMode();
		InstanceRole res;
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
		try{
			Role role = resolveDependency(RaftMachine.class).currentRole();
			//avoid "switch" statement because it creates a dependency with Role that doesn't exist in community edition
			if (role == Role.LEADER) {
				return InstanceRole.LEADER;
			} else if (role == Role.FOLLOWER) {
				return InstanceRole.FOLLOWER;
			} else if (role == Role.CANDIDATE) {
				return InstanceRole.CANDIDATE;
			} else {
				return InstanceRole.SINGLE;
			}
		}catch(NoClassDefFoundError e){
			return InstanceRole.SINGLE;
		}
	}

	/**
	 * Role for HA cluster 
	 * @return
	 */
	private InstanceRole getHARole() {
		try{
			String role = resolveDependency(ClusterMembers.class).getCurrentMemberRole();
			
			if ("master".equalsIgnoreCase(role)) {
				return InstanceRole.MASTER;
			}
			
			return InstanceRole.SLAVE;			
		}catch(NoClassDefFoundError e){
			return InstanceRole.SINGLE;
		}
	}


}
