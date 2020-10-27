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
import com.graphaware.common.junit.InjectNeo4j;
import com.graphaware.common.junit.Neo4jExtension;
import org.json.JSONException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.neo4j.graphdb.*;
import org.skyscreamer.jsonassert.JSONAssert;

@ExtendWith(Neo4jExtension.class)
public class JsonGraphTest {

    @InjectNeo4j
    private GraphDatabaseService database;

    private long a, b, r1, r2;
    private ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    protected void populate() {
        try (Transaction tx = database.beginTx()) {
            Node node1 = tx.createNode(Label.label("L1"), Label.label("L2"));
            Node node2 = tx.createNode();

            node1.setProperty("k1", "v1");
            node1.setProperty("k2", 2);

            Relationship r = node1.createRelationshipTo(node2, RelationshipType.withName("R"));
            r.setProperty("k1", "v1");
            r.setProperty("k2", 2);

            Relationship rel2 = node1.createRelationshipTo(node2, RelationshipType.withName("R2"));
            rel2.setProperty("k1", "v2");
            rel2.setProperty("k2", 4);

            a = node1.getId();
            b = node2.getId();
            r1 = r.getId();
            r2 = rel2.getId();

            tx.commit();
        }
    }

    @Test
    public void shouldProduceGraph1() throws JsonProcessingException, JSONException {
        Graph g = new Graph();

        try (Transaction tx = database.beginTx()) {
            g.addNode(tx.getNodeById(a));
            g.addNode(tx.getNodeById(b));
            g.addRelationship(tx.getRelationshipById(r1));
            g.addRelationship(tx.getRelationshipById(r2));
            tx.commit();
        }

        JSONAssert.assertEquals("{\"nodes\":" +
                "[{\"properties\":{\"k1\":\"v1\",\"k2\":2},\"labels\":[\"L1\",\"L2\"],\"id\":" + a + "}," +
                "{\"properties\":{},\"labels\":[],\"id\":" + b + "}]," +
                "\"relationships\":" +
                "[{\"properties\":{\"k1\":\"v1\",\"k2\":2},\"type\":\"R\",\"id\":" + r1 + ",\"startNodeId\":" + a + ",\"endNodeId\":" + b + "}," +
                "{\"properties\":{\"k1\":\"v2\",\"k2\":4},\"type\":\"R2\",\"id\":" + r2 + ",\"startNodeId\":" + a + ",\"endNodeId\":" + b + "}]}", mapper.writeValueAsString(g), false);
    }

    @Test
    public void shouldProduceGraph2() throws JsonProcessingException, JSONException {
        Graph g = new Graph();

        try (Transaction tx = database.beginTx()) {
            g.addRelationship(tx.getRelationshipById(r2));
            g.addRelationship(tx.getRelationshipById(r1));
            g.addNode(tx.getNodeById(a));
            g.addNode(tx.getNodeById(b));
            tx.commit();
        }

        JSONAssert.assertEquals("{\"nodes\":" +
                "[{\"properties\":{\"k1\":\"v1\",\"k2\":2},\"labels\":[\"L1\",\"L2\"],\"id\":" + a + "}," +
                "{\"properties\":{},\"labels\":[],\"id\":" + b + "}]," +
                "\"relationships\":" +
                "[{\"properties\":{\"k1\":\"v1\",\"k2\":2},\"type\":\"R\",\"id\":" + r1 + ",\"startNodeId\":" + a + ",\"endNodeId\":" + b + "}," +
                "{\"properties\":{\"k1\":\"v2\",\"k2\":4},\"type\":\"R2\",\"id\":" + r2 + ",\"startNodeId\":" + a + ",\"endNodeId\":" + b + "}]}", mapper.writeValueAsString(g), false);
    }

    private class Graph extends JsonGraph<Graph> {

    @Override
        protected Graph self() {
            return this;
        }
    }
}
