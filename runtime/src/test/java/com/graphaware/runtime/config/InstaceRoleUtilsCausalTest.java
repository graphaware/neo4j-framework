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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.kernel.impl.factory.OperationalMode;

import com.graphaware.runtime.config.util.InstanceRole;
import com.graphaware.runtime.config.util.InstanceRoleUtils;

/**
 * Test for @InstanceRoleUtils in causal cluster mode (1 leader, 2 followers, 1 read_replica)
 */
public class InstaceRoleUtilsCausalTest {

	private static CoreCausalClusterIntegrationTestUtils clusterUtils = new CoreCausalClusterIntegrationTestUtils();
	private static ReplicaCausalClusterIntegrationTestUtils clusterReplicaUtils = new ReplicaCausalClusterIntegrationTestUtils();
	
	private static InstanceRoleUtils utilsLeader;
	private static InstanceRoleUtils utilsFollower1;
	private static InstanceRoleUtils utilsFollower2;
	private static InstanceRoleUtils utilsReplica;

	@BeforeClass
	public static void setUp() throws Exception {
		clusterUtils.setUpDatabases(3);
		clusterReplicaUtils.setUpDatabases(1);
		
		utilsLeader = new InstanceRoleUtils(clusterUtils.getMainDatabase());
		
		utilsFollower1 = new InstanceRoleUtils(clusterUtils.getSecondaryDatabases().get(0));
		utilsFollower2 = new InstanceRoleUtils(clusterUtils.getSecondaryDatabases().get(1));
		
		utilsReplica = new InstanceRoleUtils(clusterReplicaUtils.getMainDatabase());
	}

	@AfterClass
	public static void tearDown() throws Exception {
		clusterReplicaUtils.shutdownDatabases();
		clusterUtils.shutdownDatabases();
	}

	@Test
	public void testGetOperationalMode() {
		assertEquals(OperationalMode.core, utilsLeader.getOperationalMode());
		assertEquals(OperationalMode.core, utilsFollower1.getOperationalMode());
		assertEquals(OperationalMode.core, utilsFollower2.getOperationalMode());
		
		assertEquals(OperationalMode.read_replica, utilsReplica.getOperationalMode());
	}

	@Test
	public void testGetInstaceRole() {
		assertEquals(InstanceRole.LEADER, utilsLeader.getInstaceRole());
		assertEquals(InstanceRole.FOLLOWER, utilsFollower1.getInstaceRole());
		assertEquals(InstanceRole.FOLLOWER, utilsFollower2.getInstaceRole());
		
		assertEquals(InstanceRole.READ_REPLICA, utilsReplica.getInstaceRole());
	}

	@Test
	public void testIsReadOnly() {
		assertFalse(utilsLeader.isReadOnly());
		assertTrue(utilsFollower1.isReadOnly());
		assertTrue(utilsFollower2.isReadOnly());
		
		assertTrue(utilsReplica.isReadOnly());
	}

}
