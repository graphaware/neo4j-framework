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
import com.graphaware.common.representation.DetachedEntity;
import com.graphaware.common.representation.SerializationSpecification;
import com.graphaware.common.transform.NodeIdTransformer;
import org.json.JSONException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.neo4j.graphdb.*;
import org.skyscreamer.jsonassert.JSONAssert;

import java.io.IOException;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;

@TestInstance(PER_CLASS)
@ExtendWith(Neo4jExtension.class)
public class LongIdJsonNodeTest {

    @InjectNeo4j(lifecycle = InjectNeo4j.Lifecycle.CLASS)
    private GraphDatabaseService database;

    private ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    protected void populate() {
        try (Transaction tx = database.beginTx()) {
            Node node1 = tx.createNode(Label.label("L1"), Label.label("L2"));
            Node node2 = tx.createNode();

            node1.setProperty("k1", "v1");
            node1.setProperty("k2", 2);

            tx.commit();
        }
    }

    @Test
    public void shouldCorrectlySerialiseNodes() throws JsonProcessingException, JSONException {
        try (Transaction tx = database.beginTx()) {
            Node node = tx.getNodeById(0);

            JSONAssert.assertEquals("{\"id\":0,\"properties\":{\"k1\":\"v1\",\"k2\":2},\"labels\":[\"L1\",\"L2\"]}", mapper.writeValueAsString(new LongIdJsonNode(node, new SerializationSpecification().getNodeProperties())), true);
            JSONAssert.assertEquals("{\"id\":0,\"properties\":{\"k1\":\"v1\",\"k2\":2},\"labels\":[\"L1\",\"L2\"]}", mapper.writeValueAsString(new LongIdJsonNode(node)), true);

            SerializationSpecification jsonInput1 = new SerializationSpecification();
            jsonInput1.setNodeProperties(null);
            JSONAssert.assertEquals("{\"id\":0,\"properties\":{\"k1\":\"v1\",\"k2\":2},\"labels\":[\"L1\",\"L2\"]}", mapper.writeValueAsString(new LongIdJsonNode(node, jsonInput1.getNodeProperties())), true);

            SerializationSpecification jsonInput2 = new SerializationSpecification();
            jsonInput2.setNodeProperties(new String[]{"k1"});
            JSONAssert.assertEquals("{\"id\":0,\"properties\":{\"k1\":\"v1\"},\"labels\":[\"L1\",\"L2\"]}", mapper.writeValueAsString(new LongIdJsonNode(node, jsonInput2.getNodeProperties())), true);

            JSONAssert.assertEquals("{\"id\":0,\"labels\":[\"L1\",\"L2\"],\"properties\":{}}", mapper.writeValueAsString(new LongIdJsonNode(node, new String[]{"k3"})), true);

            JSONAssert.assertEquals("{\"id\":0,\"labels\":[\"L1\",\"L2\"],\"properties\":{}}", mapper.writeValueAsString(new LongIdJsonNode(node, new String[0])), true);

            JSONAssert.assertEquals("{\"id\":1000,\"labels\":[],\"properties\":{}}", mapper.writeValueAsString(new LongIdJsonNode(tx.getNodeById(1), new String[0], new TimesThousandNodeIdTransformer())), true);
            JSONAssert.assertEquals("{\"id\":1000,\"labels\":[],\"properties\":{}}", mapper.writeValueAsString(new LongIdJsonNode(tx.getNodeById(1), new TimesThousandNodeIdTransformer())), true);

            JSONAssert.assertEquals("{\"id\":0,\"labels\":[\"L1\",\"L2\"],\"properties\":{}}", mapper.writeValueAsString(new LongIdJsonNode(new LongIdJsonNode(0).produceEntity(tx), new String[0])), true);
            JSONAssert.assertEquals("{\"id\":1,\"labels\":[],\"properties\":{}}", mapper.writeValueAsString(new LongIdJsonNode(new LongIdJsonNode(1000).produceEntity(tx, new TimesThousandNodeIdTransformer()))), true);

            JSONAssert.assertEquals("{\"id\":1000,\"labels\":[\"L1\"], \"properties\":{\"k\":\"v\"}}", mapper.writeValueAsString(new LongIdJsonNode(1000L, new String[]{"L1"}, Collections.singletonMap("k", "v"))), true);
            JSONAssert.assertEquals("{\"labels\":[\"L1\"], \"properties\":{\"k\":\"v\"}}", mapper.writeValueAsString(new LongIdJsonNode(new String[]{"L1"}, Collections.singletonMap("k", "v"))), true);
        }
    }

    @Test
    public void shouldCorrectlyDeserialiseNodes() throws IOException, JSONException {
        try (Transaction tx = database.beginTx()) {
            LongIdJsonNode jsonNode = mapper.readValue("{\"id\":0}", LongIdJsonNode.class);
            Node node = jsonNode.produceEntity(tx);

            assertEquals(0, node.getId());
            assertEquals(node, tx.getNodeById(0));

            tx.commit();
        }

        try (Transaction tx = database.beginTx()) {
            LongIdJsonNode jsonNode = new LongIdJsonNode(0);
            Node node = jsonNode.produceEntity(tx);

            assertEquals(0, node.getId());
            assertEquals(node, tx.getNodeById(0));

            tx.commit();
        }

        try (Transaction tx = database.beginTx()) {
            LongIdJsonNode jsonNode = new LongIdJsonNode(1000);
            Node node = jsonNode.produceEntity(tx, new TimesThousandNodeIdTransformer());

            assertEquals(1, node.getId());
            assertEquals(node, tx.getNodeById(1));

            tx.commit();
        }

        int i = 1;

        try (Transaction tx = database.beginTx()) {
            LongIdJsonNode jsonNode = mapper.readValue("{\"properties\":{\"k1\":\"v1\",\"k2\":2},\"labels\":[\"L1\",\"L2\"]}", LongIdJsonNode.class);
            Node node = jsonNode.produceEntity(tx);

            assertEquals(++i, node.getId());
            LongIdJsonNode value = new LongIdJsonNode(node);
            JSONAssert.assertEquals("{\"id\":" + i + ",\"properties\":{\"k1\":\"v1\",\"k2\":2},\"labels\":[\"L1\",\"L2\"]}", mapper.writeValueAsString(value), true);

            tx.commit();
        }

        try (Transaction tx = database.beginTx()) {
            LongIdJsonNode jsonNode = mapper.readValue("{\"properties\":{},\"labels\":[]}", LongIdJsonNode.class);
            Node node = jsonNode.produceEntity(tx);

            assertEquals(++i, node.getId());
            JSONAssert.assertEquals("{\"id\":" + i + ",\"labels\":[],\"properties\":{}}", mapper.writeValueAsString(new LongIdJsonNode(node)), true);

            tx.commit();
        }

        try (Transaction tx = database.beginTx()) {
            LongIdJsonNode jsonNode = mapper.readValue("{}", LongIdJsonNode.class);
            Node node = jsonNode.produceEntity(tx);

            assertEquals(++i, node.getId());
            JSONAssert.assertEquals("{\"id\":" + i + ",\"labels\":[],\"properties\":{}}", mapper.writeValueAsString(new LongIdJsonNode(node)), true);

            tx.commit();
        }

        try (Transaction tx = database.beginTx()) {
            LongIdJsonNode jsonNode = new LongIdJsonNode(DetachedEntity.NEW, new String[]{"L1"}, Collections.singletonMap("k1", "v1"));
            Node node = jsonNode.produceEntity(tx);

            assertEquals(++i, node.getId());
            JSONAssert.assertEquals("{\"id\":" + i + ",\"properties\":{\"k1\":\"v1\"},\"labels\":[\"L1\"]}", mapper.writeValueAsString(new LongIdJsonNode(node)), true);

            tx.commit();
        }

        try (Transaction tx = database.beginTx()) {
            LongIdJsonNode jsonNode = new LongIdJsonNode(new String[]{"L1"}, Collections.singletonMap("k1", "v1"));
            Node node = jsonNode.produceEntity(tx);

            assertEquals(++i, node.getId());
            JSONAssert.assertEquals("{\"id\":" + i + ",\"properties\":{\"k1\":\"v1\"},\"labels\":[\"L1\"]}", mapper.writeValueAsString(new LongIdJsonNode(node)), true);

            tx.commit();
        }

        try (Transaction tx = database.beginTx()) {
            LongIdJsonNode jsonNode = mapper.readValue("{\"id\":" + i * 1000 + "}", LongIdJsonNode.class);
            Node node = jsonNode.produceEntity(tx, new TimesThousandNodeIdTransformer());

            assertEquals(i, node.getId());
            JSONAssert.assertEquals("{\"id\":" + i + ",\"properties\":{\"k1\":\"v1\"},\"labels\":[\"L1\"]}", mapper.writeValueAsString(new LongIdJsonNode(node)), true);

            tx.commit();
        }

        assertThrows(IllegalStateException.class, () -> {
            try (Transaction tx = database.beginTx()) {
                LongIdJsonNode jsonNode = mapper.readValue("{\"id\":1, \"properties\":{\"k1\":\"v1\",\"k2\":2}}", LongIdJsonNode.class);
                jsonNode.produceEntity(tx);

                tx.commit();
            }
        });

        assertThrows(NotFoundException.class, () -> {
            try (Transaction tx = database.beginTx()) {
                LongIdJsonNode jsonNode = mapper.readValue("{\"id\":10000}", LongIdJsonNode.class);
                jsonNode.produceEntity(tx, new TimesThousandNodeIdTransformer());

                tx.commit();
            }
        });

        assertThrows(IllegalStateException.class, () -> {
            try (Transaction tx = database.beginTx()) {
                LongIdJsonNode jsonNode = mapper.readValue("{\"id\":1, \"labels\":[\"L1\",\"L2\"]}", LongIdJsonNode.class);
                jsonNode.produceEntity(tx);

                tx.commit();
            }
        });
    }

    @Test
    public void shouldCorrectlySerialiseArrayProps() throws JsonProcessingException, JSONException {
        try (Transaction tx = database.beginTx()) {
            Node node = tx.createNode(Label.label("L1"), Label.label("L2"));
            node.setProperty("k1", new String[]{"v1", "v2"});
            node.setProperty("k2", new int[]{2, 3});

            JSONAssert.assertEquals("{\"id\":" + node.getId() + ",\"properties\":{\"k1\":[\"v1\",\"v2\"],\"k2\":[2,3]},\"labels\":[\"L1\",\"L2\"]}", mapper.writeValueAsString(new LongIdJsonNode(node)), true);
        }
    }

    private class TimesThousandNodeIdTransformer implements NodeIdTransformer<Long> {

        @Override
        public long toGraphId(Long id) {
            if (id == null) {
                return DetachedEntity.NEW;
            }

            return id / 1000;
        }

        @Override
        public Long fromEntity(Node entity) {
            return entity.getId() * 1000;
        }
    }
}
