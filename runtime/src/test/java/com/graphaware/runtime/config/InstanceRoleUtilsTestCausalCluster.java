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
import org.junit.Ignore;
import org.junit.Test;
import org.neo4j.kernel.impl.factory.OperationalMode;

import com.graphaware.common.policy.role.InstanceRole;
import com.graphaware.runtime.config.util.InstanceRoleUtils;
import com.graphaware.test.integration.cluster.CausalClusterDatabasesintegrationTest;

/**
 * Test for @InstanceRoleUtils in causal cluster mode
 */
public class InstanceRoleUtilsTestCausalCluster extends CausalClusterDatabasesintegrationTest {

    private InstanceRoleUtils utilsLeader;
    private InstanceRoleUtils utilsFollower1;
    private InstanceRoleUtils utilsFollower2;
    private InstanceRoleUtils utilsReplica;

    @Before
    public void setUp() throws Exception {
        super.setUp();

        utilsLeader = new InstanceRoleUtils(getLeaderDatabase());

        utilsFollower1 = new InstanceRoleUtils(getFollowers().get(0));
        utilsFollower2 = new InstanceRoleUtils(getFollowers().get(1));

        utilsReplica = new InstanceRoleUtils(getReplicas().get(0));
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
        assertEquals(InstanceRole.LEADER, utilsLeader.getInstanceRole());
        assertEquals(InstanceRole.FOLLOWER, utilsFollower1.getInstanceRole());
        assertEquals(InstanceRole.FOLLOWER, utilsFollower2.getInstanceRole());

        assertEquals(InstanceRole.READ_REPLICA, utilsReplica.getInstanceRole());
    }

    @Test
    public void testIsReadOnly() {
        assertFalse(utilsLeader.getInstanceRole().isReadOnly());
        assertTrue(utilsFollower1.getInstanceRole().isReadOnly());
        assertTrue(utilsFollower2.getInstanceRole().isReadOnly());

        assertTrue(utilsReplica.getInstanceRole().isReadOnly());
    }

}
