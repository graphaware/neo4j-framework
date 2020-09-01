/*
 * Copyright (c) 2013-2020 GraphAware
 *
 * This file is part of the GraphAware Framework.
 *
 * GraphAware Framework is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received a copy of
 * the GNU General Public License along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 */

package com.graphaware.common.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.graphaware.common.UnitTest;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.neo4j.graphdb.*;
import org.skyscreamer.jsonassert.JSONAssert;

public class JsonGraphTest extends UnitTest {

    private ObjectMapper mapper = new ObjectMapper();

    @Override
    protected void populate(Transaction tx) {
        Node node1 = tx.createNode(Label.label("L1"), Label.label("L2"));
        Node node2 = tx.createNode();

        node1.setProperty("k1", "v1");
        node1.setProperty("k2", 2);

        Relationship r = node1.createRelationshipTo(node2, RelationshipType.withName("R"));
        r.setProperty("k1", "v1");
        r.setProperty("k2", 2);

        Relationship r2 = node1.createRelationshipTo(node2, RelationshipType.withName("R2"));
        r2.setProperty("k1", "v2");
        r2.setProperty("k2", 4);
    }

    @Test
    public void shouldProduceGraph1() throws JsonProcessingException, JSONException {
        Graph g = new Graph();

        try (Transaction tx = database.beginTx()) {
            g.addNode(tx.getNodeById(0));
            g.addNode(tx.getNodeById(1));
            g.addRelationship(tx.getRelationshipById(0));
            g.addRelationship(tx.getRelationshipById(1));
            tx.commit();
        }

        JSONAssert.assertEquals("{\"nodes\":" +
                "[{\"properties\":{\"k1\":\"v1\",\"k2\":2},\"labels\":[\"L1\",\"L2\"],\"id\":0}," +
                "{\"properties\":{},\"labels\":[],\"id\":1}]," +
                "\"relationships\":" +
                "[{\"properties\":{\"k1\":\"v1\",\"k2\":2},\"type\":\"R\",\"id\":0,\"startNodeId\":0,\"endNodeId\":1}," +
                "{\"properties\":{\"k1\":\"v2\",\"k2\":4},\"type\":\"R2\",\"id\":1,\"startNodeId\":0,\"endNodeId\":1}]}", mapper.writeValueAsString(g), false);

        System.out.println(mapper.writeValueAsString(g));
    }

    @Test
    public void shouldProduceGraph2() throws JsonProcessingException, JSONException {
        Graph g = new Graph();

        try (Transaction tx = database.beginTx()) {
            g.addRelationship(tx.getRelationshipById(1));
            g.addRelationship(tx.getRelationshipById(0));
            g.addNode(tx.getNodeById(0));
            g.addNode(tx.getNodeById(1));
            tx.commit();
        }

        JSONAssert.assertEquals("{\"nodes\":" +
                "[{\"properties\":{\"k1\":\"v1\",\"k2\":2},\"labels\":[\"L1\",\"L2\"],\"id\":0}," +
                "{\"properties\":{},\"labels\":[],\"id\":1}]," +
                "\"relationships\":" +
                "[{\"properties\":{\"k1\":\"v1\",\"k2\":2},\"type\":\"R\",\"id\":0,\"startNodeId\":0,\"endNodeId\":1}," +
                "{\"properties\":{\"k1\":\"v2\",\"k2\":4},\"type\":\"R2\",\"id\":1,\"startNodeId\":0,\"endNodeId\":1}]}", mapper.writeValueAsString(g), false);
    }

    private class Graph extends JsonGraph<Graph> {

        @Override
        protected Graph self() {
            return this;
        }
    }
}
