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

package com.graphaware.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.graphaware.test.integration.GraphAwareIntegrationTest;
import org.apache.http.HttpStatus;
import org.junit.Test;
import org.neo4j.graphdb.*;

import java.io.IOException;
import java.util.Collection;

import static org.junit.Assert.assertEquals;

/**
 * {@link GraphAwareIntegrationTest} for {@link NodeStreamingApi}.
 */
public class NodeStreamingApiTest extends GraphAwareIntegrationTest {

    private static final Label TEST_NODE = Label.label("TestNode");
    private static final int NUMBER_OF_NODES = 1000;

    @Override
    protected void populateDatabase(GraphDatabaseService database) {
        try (Transaction tx = database.beginTx()) {
            for (int i = 0; i < NUMBER_OF_NODES; i++) {
                Node node = database.createNode(TEST_NODE);
                node.setProperty("name", "node" + i);
            }
            tx.success();
        }
    }

    @Test
    public void shouldStreamAllNodesV1() throws IOException {
        String result = httpClient.get(baseUrl() + "/stream/v1", HttpStatus.SC_OK);

        assertEquals(NUMBER_OF_NODES, new ObjectMapper().readValue(result, Collection.class).size());
    }

    @Test
    public void shouldStreamAllNodesV2() throws IOException {
        String result = httpClient.get(baseUrl() + "/stream/v2", HttpStatus.SC_OK);

        assertEquals(NUMBER_OF_NODES, new ObjectMapper().readValue(result, Collection.class).size());
    }
}
