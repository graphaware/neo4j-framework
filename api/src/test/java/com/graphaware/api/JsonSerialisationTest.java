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

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.*;
import org.neo4j.test.TestGraphDatabaseFactory;

import java.io.IOException;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Test for {@link JsonNode}, {@link JsonRelationship}, and {@link JsonInput}.
 */
public class JsonSerialisationTest {

    private GraphDatabaseService database;
    private ObjectMapper mapper = new ObjectMapper();

    @Before
    public void setUp() {
        database = new TestGraphDatabaseFactory().newImpermanentDatabase();

        try (Transaction tx = database.beginTx()) {
            Node node1 = database.createNode(DynamicLabel.label("L1"), DynamicLabel.label("L2"));
            Node node2 = database.createNode();

            node1.setProperty("k1", "v1");
            node1.setProperty("k2", 2);

            Relationship r = node1.createRelationshipTo(node2, DynamicRelationshipType.withName("R"));
            r.setProperty("k1", "v1");
            r.setProperty("k2", 2);

            tx.success();
        }
    }

    @After
    public void tearDown() {
        database.shutdown();
    }

    @Test
    public void shouldCorrectlySerialiseNodes() throws JsonProcessingException {
        try (Transaction tx = database.beginTx()) {
            Node node = database.getNodeById(0);

            assertEquals("{\"id\":0,\"properties\":{\"k1\":\"v1\",\"k2\":2},\"labels\":[\"L1\",\"L2\"]}", mapper.writeValueAsString(new JsonNode(node, new JsonInput())));
            assertEquals("{\"id\":0,\"properties\":{\"k1\":\"v1\",\"k2\":2},\"labels\":[\"L1\",\"L2\"]}", mapper.writeValueAsString(new JsonNode(node)));

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
    public void shouldCorrectlyDeserialiseNodes() throws IOException {
        try (Transaction tx = database.beginTx()) {
            JsonNode jsonNode = mapper.readValue("{\"id\":0}", JsonNode.class);
            Node node = jsonNode.producePropertyContainer(database);

            assertEquals(0, node.getId());
            assertEquals(node, database.getNodeById(0));

            tx.success();
        }

        try (Transaction tx = database.beginTx()) {
            JsonNode jsonNode = new JsonNode(0);
            Node node = jsonNode.producePropertyContainer(database);

            assertEquals(0, node.getId());
            assertEquals(node, database.getNodeById(0));

            tx.success();
        }

        try (Transaction tx = database.beginTx()) {
            JsonNode jsonNode = mapper.readValue("{\"properties\":{\"k1\":\"v1\",\"k2\":2},\"labels\":[\"L1\",\"L2\"]}", JsonNode.class);
            Node node = jsonNode.producePropertyContainer(database);

            assertEquals(2, node.getId());
            assertEquals("{\"id\":2,\"properties\":{\"k1\":\"v1\",\"k2\":2},\"labels\":[\"L1\",\"L2\"]}", mapper.writeValueAsString(new JsonNode(node)));

            tx.success();
        }

        try (Transaction tx = database.beginTx()) {
            JsonNode jsonNode = mapper.readValue("{\"properties\":{},\"labels\":[]}", JsonNode.class);
            Node node = jsonNode.producePropertyContainer(database);

            assertEquals(3, node.getId());
            assertEquals("{\"id\":3,\"labels\":[]}", mapper.writeValueAsString(new JsonNode(node)));

            tx.success();
        }

        try (Transaction tx = database.beginTx()) {
            JsonNode jsonNode = mapper.readValue("{}", JsonNode.class);
            Node node = jsonNode.producePropertyContainer(database);

            assertEquals(4, node.getId());
            assertEquals("{\"id\":4,\"labels\":[]}", mapper.writeValueAsString(new JsonNode(node)));

            tx.success();
        }

        try (Transaction tx = database.beginTx()) {
            JsonNode jsonNode = new JsonNode(new String[]{"L1"}, Collections.<String, Object>singletonMap("k1", "v1"));
            Node node = jsonNode.producePropertyContainer(database);

            assertEquals(5, node.getId());
            assertEquals("{\"id\":5,\"properties\":{\"k1\":\"v1\"},\"labels\":[\"L1\"]}", mapper.writeValueAsString(new JsonNode(node)));

            tx.success();
        }

        try (Transaction tx = database.beginTx()) {
            JsonNode jsonNode = mapper.readValue("{\"id\":1, \"properties\":{\"k1\":\"v1\",\"k2\":2}}", JsonNode.class);
            jsonNode.producePropertyContainer(database);

            tx.success();
            fail();
        } catch (IllegalStateException e) {
            //ok
        }

        try (Transaction tx = database.beginTx()) {
            JsonNode jsonNode = mapper.readValue("{\"id\":1, \"labels\":[\"L1\",\"L2\"]}", JsonNode.class);
            jsonNode.producePropertyContainer(database);

            tx.success();
            fail();
        } catch (IllegalStateException e) {
            //ok
        }
    }

    @Test
    public void shouldCorrectlySerialiseRelationships() throws JsonProcessingException {
        try (Transaction tx = database.beginTx()) {
            Relationship r = database.getRelationshipById(0);

            assertEquals("{\"id\":0,\"properties\":{\"k1\":\"v1\",\"k2\":2},\"startNodeId\":0,\"endNodeId\":1,\"type\":\"R\"}", mapper.writeValueAsString(new JsonRelationship(r, new JsonInput())));
            assertEquals("{\"id\":0,\"properties\":{\"k1\":\"v1\",\"k2\":2},\"startNodeId\":0,\"endNodeId\":1,\"type\":\"R\"}", mapper.writeValueAsString(new JsonRelationship(r)));

            JsonInput jsonInput1 = new JsonInput();
            jsonInput1.setRelationshipProperties(null);
            assertEquals("{\"id\":0,\"properties\":{\"k1\":\"v1\",\"k2\":2},\"startNodeId\":0,\"endNodeId\":1,\"type\":\"R\"}", mapper.writeValueAsString(new JsonRelationship(r, jsonInput1)));

            JsonInput jsonInput2 = new JsonInput();
            jsonInput2.setRelationshipProperties(new String[]{"k1"});
            assertEquals("{\"id\":0,\"properties\":{\"k1\":\"v1\"},\"startNodeId\":0,\"endNodeId\":1,\"type\":\"R\"}", mapper.writeValueAsString(new JsonRelationship(r, jsonInput2)));

            JsonInput jsonInput3 = new JsonInput();
            jsonInput3.setRelationshipProperties(new String[]{"k3"});
            assertEquals("{\"id\":0,\"startNodeId\":0,\"endNodeId\":1,\"type\":\"R\"}", mapper.writeValueAsString(new JsonRelationship(r, jsonInput3)));

            JsonInput jsonInput4 = new JsonInput();
            jsonInput4.setRelationshipProperties(new String[0]);
            assertEquals("{\"id\":0,\"startNodeId\":0,\"endNodeId\":1,\"type\":\"R\"}", mapper.writeValueAsString(new JsonRelationship(r, jsonInput4)));
        }
    }

    @Test
    public void shouldCorrectlyDeserialiseRelationships() throws IOException {
        try (Transaction tx = database.beginTx()) {
            JsonRelationship jsonRel = mapper.readValue("{\"id\":0}", JsonRelationship.class);
            Relationship rel = jsonRel.producePropertyContainer(database);

            assertEquals(0, rel.getId());
            assertEquals(rel, database.getRelationshipById(0));

            tx.success();
        }

        try (Transaction tx = database.beginTx()) {
            JsonRelationship jsonRel = new JsonRelationship(0);
            Relationship rel = jsonRel.producePropertyContainer(database);

            assertEquals(0, rel.getId());
            assertEquals(rel, database.getRelationshipById(0));

            tx.success();
        }

        try (Transaction tx = database.beginTx()) {
            JsonRelationship jsonRel = mapper.readValue("{\"startNodeId\":0, \"endNodeId\":1, \"properties\":{\"k1\":\"v1\",\"k2\":2},\"type\":\"R\"}", JsonRelationship.class);
            Relationship rel = jsonRel.producePropertyContainer(database);

            assertEquals(1, rel.getId());
            assertEquals("{\"id\":1,\"properties\":{\"k1\":\"v1\",\"k2\":2},\"startNodeId\":0,\"endNodeId\":1,\"type\":\"R\"}", mapper.writeValueAsString(new JsonRelationship(rel)));

            tx.success();
        }

        try (Transaction tx = database.beginTx()) {
            JsonRelationship jsonRel = mapper.readValue("{\"startNodeId\":0, \"endNodeId\":1, \"properties\":{},\"type\":\"R\"}", JsonRelationship.class);
            Relationship rel = jsonRel.producePropertyContainer(database);

            assertEquals(2, rel.getId());
            assertEquals("{\"id\":2,\"startNodeId\":0,\"endNodeId\":1,\"type\":\"R\"}", mapper.writeValueAsString(new JsonRelationship(rel)));

            tx.success();
        }

        try (Transaction tx = database.beginTx()) {
            JsonRelationship jsonRel = mapper.readValue("{\"startNodeId\":0, \"endNodeId\":1,\"type\":\"R\"}", JsonRelationship.class);
            Relationship rel = jsonRel.producePropertyContainer(database);

            assertEquals(3, rel.getId());
            assertEquals("{\"id\":3,\"startNodeId\":0,\"endNodeId\":1,\"type\":\"R\"}", mapper.writeValueAsString(new JsonRelationship(rel)));

            tx.success();
        }

        try (Transaction tx = database.beginTx()) {
            JsonRelationship jsonRel = new JsonRelationship(0, 1, "R", Collections.<String, Object>singletonMap("k1", "v1"));
            Relationship rel = jsonRel.producePropertyContainer(database);

            assertEquals(4, rel.getId());
            assertEquals("{\"id\":4,\"properties\":{\"k1\":\"v1\"},\"startNodeId\":0,\"endNodeId\":1,\"type\":\"R\"}", mapper.writeValueAsString(new JsonRelationship(rel)));

            tx.success();
        }

        try (Transaction tx = database.beginTx()) {
            JsonRelationship jsonRel = mapper.readValue("{\"id\":0, \"properties\":{\"k1\":\"v1\",\"k2\":2}}", JsonRelationship.class);
            jsonRel.producePropertyContainer(database);

            tx.success();
            fail();
        } catch (IllegalStateException e) {
            //ok
        }

        try (Transaction tx = database.beginTx()) {
            JsonRelationship jsonRel = mapper.readValue("{\"id\":0, \"type\":\"R\"}", JsonRelationship.class);
            jsonRel.producePropertyContainer(database);

            tx.success();
            fail();
        } catch (IllegalStateException e) {
            //ok
        }

        try (Transaction tx = database.beginTx()) {
            JsonRelationship jsonRel = mapper.readValue("{\"id\":0, \"startNodeId\":1}", JsonRelationship.class);
            jsonRel.producePropertyContainer(database);

            tx.success();
            fail();
        } catch (IllegalStateException e) {
            //ok
        }

        try (Transaction tx = database.beginTx()) {
            JsonRelationship jsonRel = mapper.readValue("{\"id\":0, \"endNodeId\":0}", JsonRelationship.class);
            jsonRel.producePropertyContainer(database);

            tx.success();
            fail();
        } catch (IllegalStateException e) {
            //ok
        }

        try (Transaction tx = database.beginTx()) {
            JsonRelationship jsonRel = mapper.readValue("{\"endNodeId\":1, \"properties\":{},\"type\":\"R\"}", JsonRelationship.class);
            jsonRel.producePropertyContainer(database);

            tx.success();
            fail();
        } catch (IllegalStateException e) {
            //ok
        }

        try (Transaction tx = database.beginTx()) {
            JsonRelationship jsonRel = mapper.readValue("{\"startNodeId\":0, \"properties\":{},\"type\":\"R\"}", JsonRelationship.class);
            jsonRel.producePropertyContainer(database);

            tx.success();
            fail();
        } catch (IllegalStateException e) {
            //ok
        }

        try (Transaction tx = database.beginTx()) {
            JsonRelationship jsonRel = mapper.readValue("{\"startNodeId\":0, \"endNodeId\":1, \"properties\":{}}", JsonRelationship.class);
            jsonRel.producePropertyContainer(database);

            tx.success();
            fail();
        } catch (IllegalStateException e) {
            //ok
        }

        try (Transaction tx = database.beginTx()) {
            JsonRelationship jsonRel = mapper.readValue("{\"startNodeId\":x, \"endNodeId\":1, \"properties\":{},\"type\":\"R\"}", JsonRelationship.class);
            jsonRel.producePropertyContainer(database);

            tx.success();
            fail();
        } catch (JsonParseException e) {
            //ok
        }

        try (Transaction tx = database.beginTx()) {
            JsonRelationship jsonRel = mapper.readValue("{\"startNodeId\":0, \"endNodeId\":1, \"properties\":{},\"type\":\"\"}", JsonRelationship.class);
            jsonRel.producePropertyContainer(database);

            tx.success();
            fail();
        } catch (IllegalStateException e) {
            //ok
        }

        try (Transaction tx = database.beginTx()) {
            JsonRelationship jsonRel = mapper.readValue("{\"startNodeId\":0, \"endNodeId\":1, \"properties\":{},\"type\":null}", JsonRelationship.class);
            jsonRel.producePropertyContainer(database);

            tx.success();
            fail();
        } catch (IllegalStateException e) {
            //ok
        }

        try (Transaction tx = database.beginTx()) {
            JsonRelationship jsonRel = mapper.readValue("{\"startNodeId\":0, \"endNodeId\":x, \"properties\":{},\"type\":\"R\"}", JsonRelationship.class);
            jsonRel.producePropertyContainer(database);

            tx.success();
            fail();
        } catch (JsonParseException e) {
            //ok
        }
    }

    @Test
    public void shouldCorrectlySerialiseArrayProps() throws JsonProcessingException {
        try (Transaction tx = database.beginTx()) {
            Node node = database.createNode(DynamicLabel.label("L1"), DynamicLabel.label("L2"));
            node.setProperty("k1", new String[]{"v1", "v2"});
            node.setProperty("k2", new int[]{2, 3});

            assertEquals("{\"id\":" + node.getId() + ",\"properties\":{\"k1\":[\"v1\",\"v2\"],\"k2\":[2,3]},\"labels\":[\"L1\",\"L2\"]}", mapper.writeValueAsString(new JsonNode(node, new JsonInput())));
        }
    }
}
