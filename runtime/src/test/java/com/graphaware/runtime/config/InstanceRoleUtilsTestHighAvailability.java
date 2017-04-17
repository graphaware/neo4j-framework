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

package com.graphaware.runtime.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.neo4j.kernel.impl.factory.OperationalMode;

import com.graphaware.common.policy.role.InstanceRole;
import com.graphaware.runtime.config.util.InstanceRoleUtils;
import com.graphaware.test.integration.cluster.HighAvailabilityClusterDatabasesIntegrationTest;

/**
 * Test for @InstanceRoleUtils in HA (1 master, 1 slave) cluster
 */
public class InstanceRoleUtilsTestHighAvailability extends HighAvailabilityClusterDatabasesIntegrationTest {

	private InstanceRoleUtils utilsMaster;
	private InstanceRoleUtils utilsSlave;
	
	
	@Before
	public void setUp() throws Exception {
		super.setUp();
		utilsMaster = new InstanceRoleUtils(getMasterDatabase());
		utilsSlave = new InstanceRoleUtils(getOneSlaveDatabase());
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
		InstanceRole roleMaster = utilsMaster.getInstanceRole();
		assertEquals(InstanceRole.MASTER, roleMaster);
		
		InstanceRole roleSlave = utilsSlave.getInstanceRole();
		assertEquals(InstanceRole.SLAVE, roleSlave);
	}

	@Test
	public void testIsReadOnly() {
		assertFalse(utilsMaster.getInstanceRole().isReadOnly());
		assertTrue(utilsSlave.getInstanceRole().isReadOnly());
	}

}
