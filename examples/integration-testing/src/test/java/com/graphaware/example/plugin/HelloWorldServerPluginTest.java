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

package com.graphaware.example.plugin;

import com.graphaware.test.integration.DatabaseIntegrationTest;
import com.graphaware.test.integration.EmbeddedDatabaseIntegrationTest;
import com.graphaware.test.unit.GraphUnit;
import org.junit.Test;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;

import static org.junit.Assert.assertEquals;

/**
 * {@link EmbeddedDatabaseIntegrationTest} for {@link HelloWorldServerPlugin}.
 *
 * Tests the logic, but not the API.
 */
public class HelloWorldServerPluginTest extends EmbeddedDatabaseIntegrationTest {

    @Test
    public void shouldCreateAndReturnNode() {
        Node node = new HelloWorldServerPlugin().createHelloWorldNode((getDatabase()));

        try (Transaction tx = getDatabase().beginTx()) {
            assertEquals("world", node.getProperty("hello"));
            tx.success();
        }

        GraphUnit.assertSameGraph(getDatabase(), "CREATE (:HelloWorld {hello:'world'})");
    }
}
