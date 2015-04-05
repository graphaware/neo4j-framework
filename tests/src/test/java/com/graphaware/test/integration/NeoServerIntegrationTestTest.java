/*
 * Copyright (c) 2015 GraphAware
 *
 * This file is part of GraphAware.
 *
 * GraphAware is free software: you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received a copy of
 * the GNU General Public License along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 */

package com.graphaware.test.integration;

import org.junit.Ignore;
import org.junit.Test;

import static org.apache.http.HttpStatus.SC_OK;

public class NeoServerIntegrationTestTest extends NeoServerIntegrationTest {

    @Test
    public void shouldLoadBrowser() {
        httpClient.get(baseUrl() + "/browser", SC_OK);
    }

    @Test
    public void shouldLoadWebAdmin() {
        httpClient.get(baseUrl() + "/webadmin", SC_OK);
    }

    @Test
    @Ignore //this will not work - would have to have graphaware server as a dependency, but that would introduce a cycle
    public void shouldLoadAPIs() {
        httpClient.get(baseUrl() + "/graphaware/greeting", SC_OK);
    }
}
