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
import com.graphaware.test.integration.util.HAClusterIntegrationTestUtils;

/**
 * Test for @InstanceRoleUtils in HA (1 master, 1 slave) cluster
 */
public class InstanceRoleUtilsHATest {

	private static HAClusterIntegrationTestUtils clusterUtils = new HAClusterIntegrationTestUtils();
	
	private static InstanceRoleUtils utilsMaster;
	private static InstanceRoleUtils utilsSlave;
	
	
	@BeforeClass
	public static void setUp() throws Exception {
		clusterUtils.setUpDatabases(2);
		
		utilsMaster = new InstanceRoleUtils(clusterUtils.getMainDatabase());
		utilsSlave = new InstanceRoleUtils(clusterUtils.getSecondaryDatabases().get(0));
	}

	@AfterClass
	public static void tearDown() throws Exception {
		clusterUtils.shutdownDatabases();
	}

	@Test
	public void testGetOperationalMode() {
		OperationalMode modeM = utilsMaster.getOperationalMode();
		assertEquals(OperationalMode.ha, modeM);
		
		OperationalMode modeS = utilsSlave.getOperationalMode();
		assertEquals(OperationalMode.ha, modeS);
	}

	@Test
	public void testGetInstaceRole() {
		InstanceRole roleMaster = utilsMaster.getInstaceRole();
		assertEquals(InstanceRole.MASTER, roleMaster);
		
		InstanceRole roleSlave = utilsSlave.getInstaceRole();
		assertEquals(InstanceRole.SLAVE, roleSlave);
	}

	@Test
	public void testIsReadOnly() {
		assertFalse(utilsMaster.isReadOnly());
		assertTrue(utilsSlave.isReadOnly());
	}

}
