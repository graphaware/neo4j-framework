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

package com.graphaware.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.*;
import org.neo4j.test.TestGraphDatabaseFactory;

import static org.junit.Assert.assertEquals;

/**
 * Test for {@link JsonNode}, {@link JsonRelationship}, and {@link JsonInput}.
 */
public class JsonSerialisationTest {

    private GraphDatabaseService database;
    private ObjectMapper mapper = new ObjectMapper();

    @Before
    public void setUp() {
        database = new TestGraphDatabaseFactory().newImpermanentDatabase();
    }

    @After
    public void tearDown() {
        database.shutdown();
    }

    @Test
    public void shouldCorrectlySerialiseNodes() throws JsonProcessingException {
        try (Transaction tx = database.beginTx()) {
            Node node = database.createNode(DynamicLabel.label("L1"), DynamicLabel.label("L2"));
            node.setProperty("k1", "v1");
            node.setProperty("k2", 2);

            assertEquals("{\"id\":0,\"properties\":{\"k1\":\"v1\",\"k2\":2},\"labels\":[\"L1\",\"L2\"]}", mapper.writeValueAsString(new JsonNode(node, new JsonInput())));
            assertEquals("{\"id\":0,\"properties\":{\"k1\":\"v1\",\"k2\":2},\"labels\":[\"L1\",\"L2\"]}", mapper.writeValueAsString(new JsonNode(node)));

            JsonInput jsonInput = new JsonInput();
            jsonInput.setIncludeNodeLabels(false);
            assertEquals("{\"id\":0,\"properties\":{\"k1\":\"v1\",\"k2\":2}}", mapper.writeValueAsString(new JsonNode(node, jsonInput)));

            JsonInput jsonInput1 = new JsonInput();
            jsonInput1.setNodeProperties(null);
            assertEquals("{\"id\":0,\"properties\":{\"k1\":\"v1\",\"k2\":2},\"labels\":[\"L1\",\"L2\"]}", mapper.writeValueAsString(new JsonNode(node, jsonInput1)));

            JsonInput jsonInput2 = new JsonInput();
            jsonInput2.setNodeProperties(new String[]{"k1"});
            assertEquals("{\"id\":0,\"properties\":{\"k1\":\"v1\"},\"labels\":[\"L1\",\"L2\"]}", mapper.writeValueAsString(new JsonNode(node, jsonInput2)));

            JsonInput jsonInput3 = new JsonInput();
            jsonInput3.setNodeProperties(new String[]{"k3"});
            assertEquals("{\"id\":0,\"labels\":[\"L1\",\"L2\"]}", mapper.writeValueAsString(new JsonNode(node, jsonInput3)));

            JsonInput jsonInput4 = new JsonInput();
            jsonInput4.setNodeProperties(new String[0]);
            assertEquals("{\"id\":0,\"labels\":[\"L1\",\"L2\"]}", mapper.writeValueAsString(new JsonNode(node, jsonInput4)));
        }
    }

    @Test
    public void shouldCorrectlySerialiseRelationships() throws JsonProcessingException {
        try (Transaction tx = database.beginTx()) {
            Node node1 = database.createNode();
            Node node2 = database.createNode();
            Relationship r = node1.createRelationshipTo(node2, DynamicRelationshipType.withName("R"));
            r.setProperty("k1", "v1");
            r.setProperty("k2", 2);

            assertEquals("{\"id\":0,\"properties\":{\"k1\":\"v1\",\"k2\":2},\"type\":\"R\",\"direction\":\"OUTGOING\"}", mapper.writeValueAsString(new JsonRelationship(r, new JsonInput(), node1)));
            assertEquals("{\"id\":0,\"properties\":{\"k1\":\"v1\",\"k2\":2},\"type\":\"R\",\"direction\":\"INCOMING\"}", mapper.writeValueAsString(new JsonRelationship(r, new JsonInput(), node2)));
            assertEquals("{\"id\":0,\"properties\":{\"k1\":\"v1\",\"k2\":2},\"type\":\"R\",\"direction\":\"OUTGOING\"}", mapper.writeValueAsString(new JsonRelationship(r, node1)));
            assertEquals("{\"id\":0,\"properties\":{\"k1\":\"v1\",\"k2\":2},\"type\":\"R\",\"direction\":\"INCOMING\"}", mapper.writeValueAsString(new JsonRelationship(r, node2)));

            JsonInput jsonInput1 = new JsonInput();
            jsonInput1.setRelationshipProperties(null);
            assertEquals("{\"id\":0,\"properties\":{\"k1\":\"v1\",\"k2\":2},\"type\":\"R\",\"direction\":\"OUTGOING\"}", mapper.writeValueAsString(new JsonRelationship(r, jsonInput1, node1)));

            JsonInput jsonInput2 = new JsonInput();
            jsonInput2.setRelationshipProperties(new String[]{"k1"});
            assertEquals("{\"id\":0,\"properties\":{\"k1\":\"v1\"},\"type\":\"R\",\"direction\":\"OUTGOING\"}", mapper.writeValueAsString(new JsonRelationship(r, jsonInput2, node1)));

            JsonInput jsonInput3 = new JsonInput();
            jsonInput3.setRelationshipProperties(new String[]{"k3"});
            assertEquals("{\"id\":0,\"type\":\"R\",\"direction\":\"OUTGOING\"}", mapper.writeValueAsString(new JsonRelationship(r, jsonInput3, node1)));

            JsonInput jsonInput4 = new JsonInput();
            jsonInput4.setRelationshipProperties(new String[0]);
            assertEquals("{\"id\":0,\"type\":\"R\",\"direction\":\"OUTGOING\"}", mapper.writeValueAsString(new JsonRelationship(r, jsonInput4, node1)));
        }
    }

    @Test
    public void shouldCorrectlySerialiseArrayProps() throws JsonProcessingException {
        try (Transaction tx = database.beginTx()) {
            Node node = database.createNode(DynamicLabel.label("L1"), DynamicLabel.label("L2"));
            node.setProperty("k1", new String[]{"v1", "v2"});
            node.setProperty("k2", new int[]{2,3});

            assertEquals("{\"id\":0,\"properties\":{\"k1\":[\"v1\",\"v2\"],\"k2\":[2,3]},\"labels\":[\"L1\",\"L2\"]}", mapper.writeValueAsString(new JsonNode(node, new JsonInput())));
        }
    }
}
