/*
 * Copyright (c) 2014 GraphAware
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

package com.graphaware.example.graphaware;

import com.graphaware.test.integration.GraphAwareApiTest;
import com.graphaware.test.integration.WrappingServerIntegrationTest;
import com.graphaware.test.unit.GraphUnit;
import com.graphaware.test.util.TestUtils;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * {@link GraphAwareApiTest} for {@link HelloWorldController}.
 *
 * Tests the logic as well as the API.
 */
public class HelloWorldControllerApiTest extends GraphAwareApiTest {

    @Test
    public void shouldCreateAndReturnNode() {
        assertEquals("0", TestUtils.post(baseUrl() + "/helloworld/create", 200));

        GraphUnit.assertSameGraph(getDatabase(), "CREATE (:HelloWorld {hello:'world'})");
    }
}
