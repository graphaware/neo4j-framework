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

package com.graphaware.server;

import com.graphaware.test.integration.GraphAwareIntegrationTest;
import org.apache.http.HttpStatus;
import org.junit.Test;
import org.neo4j.server.enterprise.helpers.EnterpriseServerBuilder;
import org.neo4j.server.helpers.CommunityServerBuilder;

/**
 * Integration test for custom server that wires Spring components.
 */
public class EnterpriseNeoServerIntegrationTest extends GraphAwareIntegrationTest {

    @Override
    protected CommunityServerBuilder createServerBuilder() {
        return EnterpriseServerBuilder.server();
    }

    @Test
    public void componentsShouldBeWired() {
        httpClient.get(baseUrl() + "/greeting", HttpStatus.SC_OK);
    }
}
