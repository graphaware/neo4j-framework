/*
 * Copyright (c) 2013-2015 GraphAware
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

package com.graphaware.test;

import com.graphaware.test.integration.HighAvailabilityDatabaseIntegrationTest;
import org.junit.Test;
import org.neo4j.management.ClusterMemberInfo;
import org.neo4j.management.Neo4jManager;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;

public class HighAvailabilityDatabaseIntegrationTestTest extends HighAvailabilityDatabaseIntegrationTest {

    @Test
    public void shouldStartHighAvailabilityDatabase() {
        Neo4jManager manager = Neo4jManager.get();

        org.neo4j.management.HighAvailability highAvailabilityBean = manager.getHighAvailabilityBean();

        assertNotNull(highAvailabilityBean);

        final ClusterMemberInfo[] instancesInCluster = highAvailabilityBean.getInstancesInCluster();

        assertEquals(1, instancesInCluster.length);

        assertEquals("1", instancesInCluster[0].getInstanceId());
        assertEquals("master", instancesInCluster[0].getHaRole());
    }
}
