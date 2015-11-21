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

package com.graphaware.server;

import com.graphaware.test.integration.EnterpriseNeoTestServer;
import com.graphaware.test.integration.NeoServerIntegrationTest;
import com.graphaware.test.integration.NeoTestServer;
import org.apache.http.HttpStatus;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Integration test for custom server that wires Spring components.
 */
public class EnterpriseNeoServerHttpLoggingIntegrationTest extends NeoServerIntegrationTest {

    @Override
    protected NeoTestServer neoTestServer(String neo4jConfigFile, String neo4jServerConfigFile) {
        return new EnterpriseNeoTestServer(neo4jConfigFile, neo4jServerConfigFile) {
            @Override
            protected String[] otherConfResources() {
                return new String[]{"neo4j-http-logging.xml"};
            }
        };
    }

    @Override
    protected String neo4jServerConfigFile() {
        return "neo4j-server-http-log.properties";
    }

    @Test
    public void componentsShouldBeWired() throws InterruptedException {
        assertTrue(httpClient.get(baseUrl() + "/graphaware/greeting", HttpStatus.SC_OK).startsWith("Hello"));
    }
}
