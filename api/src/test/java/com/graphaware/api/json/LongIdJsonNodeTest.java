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

package com.graphaware.api.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.graphaware.api.SerializationSpecification;
import com.graphaware.common.transform.NodeIdTransformer;
import com.graphaware.common.representation.DetachedPropertyContainer;
import com.graphaware.test.unit.GraphUnit;
import org.json.JSONException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.*;
import org.neo4j.test.TestGraphDatabaseFactory;
import org.skyscreamer.jsonassert.JSONAssert;

import java.io.IOException;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class LongIdJsonNodeTest {

    private GraphDatabaseService database;
    private ObjectMapper mapper = new ObjectMapper();

    @Before
    public void setUp() {
        database = new TestGraphDatabaseFactory().newImpermanentDatabase();

        try (Transaction tx = database.beginTx()) {
            Node node1 = database.createNode(Label.label("L1"), Label.label("L2"));
            Node node2 = database.createNode();

            node1.setProperty("k1", "v1");
            node1.setProperty("k2", 2);

            tx.success();
        }
    }

    @After
    public void tearDown() {
        database.shutdown();
    }

    @Test
    public void shouldCorrectlySerialiseNodes() throws JsonProcessingException, JSONException {
        try (Transaction tx = database.beginTx()) {
            Node node = database.getNodeById(0);

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

            JSONAssert.assertEquals("{\"id\":1000,\"labels\":[],\"properties\":{}}", mapper.writeValueAsString(new LongIdJsonNode(database.getNodeById(1), new String[0], new TimesThousandNodeIdTransformer())), true);
            JSONAssert.assertEquals("{\"id\":1000,\"labels\":[],\"properties\":{}}", mapper.writeValueAsString(new LongIdJsonNode(database.getNodeById(1), new TimesThousandNodeIdTransformer())), true);

            JSONAssert.assertEquals("{\"id\":0,\"labels\":[\"L1\",\"L2\"],\"properties\":{}}", mapper.writeValueAsString(new LongIdJsonNode(new LongIdJsonNode(0).producePropertyContainer(database), new String[0])), true);
            JSONAssert.assertEquals("{\"id\":1,\"labels\":[],\"properties\":{}}", mapper.writeValueAsString(new LongIdJsonNode(new LongIdJsonNode(1000).producePropertyContainer(database, new TimesThousandNodeIdTransformer()))), true);

            JSONAssert.assertEquals("{\"id\":1000,\"labels\":[\"L1\"], \"properties\":{\"k\":\"v\"}}", mapper.writeValueAsString(new LongIdJsonNode(1000L, new String[]{"L1"}, Collections.singletonMap("k", "v"))), true);
            JSONAssert.assertEquals("{\"labels\":[\"L1\"], \"properties\":{\"k\":\"v\"}}", mapper.writeValueAsString(new LongIdJsonNode(new String[]{"L1"}, Collections.singletonMap("k", "v"))), true);
        }
    }

    @Test
    public void shouldCorrectlyDeserialiseNodes() throws IOException, JSONException {
        try (Transaction tx = database.beginTx()) {
            LongIdJsonNode jsonNode = mapper.readValue("{\"id\":0}", LongIdJsonNode.class);
            Node node = jsonNode.producePropertyContainer(database);

            assertEquals(0, node.getId());
            assertEquals(node, database.getNodeById(0));

            tx.success();
        }

        try (Transaction tx = database.beginTx()) {
            LongIdJsonNode jsonNode = new LongIdJsonNode(0);
            Node node = jsonNode.producePropertyContainer(database);

            assertEquals(0, node.getId());
            assertEquals(node, database.getNodeById(0));

            tx.success();
        }

        try (Transaction tx = database.beginTx()) {
            LongIdJsonNode jsonNode = new LongIdJsonNode(1000);
            Node node = jsonNode.producePropertyContainer(database, new TimesThousandNodeIdTransformer());

            assertEquals(1, node.getId());
            assertEquals(node, database.getNodeById(1));

            tx.success();
        }

        int i = 19; //for whatever reason

        try (Transaction tx = database.beginTx()) {
            LongIdJsonNode jsonNode = mapper.readValue("{\"properties\":{\"k1\":\"v1\",\"k2\":2},\"labels\":[\"L1\",\"L2\"]}", LongIdJsonNode.class);
            Node node = jsonNode.producePropertyContainer(database);

            assertEquals(++i, node.getId());
            JSONAssert.assertEquals("{\"id\":" + i + ",\"properties\":{\"k1\":\"v1\",\"k2\":2},\"labels\":[\"L1\",\"L2\"]}", mapper.writeValueAsString(new LongIdJsonNode(node)), true);

            tx.success();
        }

        try (Transaction tx = database.beginTx()) {
            LongIdJsonNode jsonNode = mapper.readValue("{\"properties\":{},\"labels\":[]}", LongIdJsonNode.class);
            Node node = jsonNode.producePropertyContainer(database);

            assertEquals(++i, node.getId());
            JSONAssert.assertEquals("{\"id\":" + i + ",\"labels\":[],\"properties\":{}}", mapper.writeValueAsString(new LongIdJsonNode(node)), true);

            tx.success();
        }

        try (Transaction tx = database.beginTx()) {
            LongIdJsonNode jsonNode = mapper.readValue("{}", LongIdJsonNode.class);
            Node node = jsonNode.producePropertyContainer(database);

            assertEquals(++i, node.getId());
            JSONAssert.assertEquals("{\"id\":" + i + ",\"labels\":[],\"properties\":{}}", mapper.writeValueAsString(new LongIdJsonNode(node)), true);

            tx.success();
        }

        try (Transaction tx = database.beginTx()) {
            LongIdJsonNode jsonNode = new LongIdJsonNode(DetachedPropertyContainer.NEW, new String[]{"L1"}, Collections.singletonMap("k1", "v1"));
            Node node = jsonNode.producePropertyContainer(database);

            assertEquals(++i, node.getId());
            JSONAssert.assertEquals("{\"id\":" + i + ",\"properties\":{\"k1\":\"v1\"},\"labels\":[\"L1\"]}", mapper.writeValueAsString(new LongIdJsonNode(node)), true);

            tx.success();
        }

        try (Transaction tx = database.beginTx()) {
            LongIdJsonNode jsonNode = new LongIdJsonNode(new String[]{"L1"}, Collections.singletonMap("k1", "v1"));
            Node node = jsonNode.producePropertyContainer(database);

            assertEquals(++i, node.getId());
            JSONAssert.assertEquals("{\"id\":" + i + ",\"properties\":{\"k1\":\"v1\"},\"labels\":[\"L1\"]}", mapper.writeValueAsString(new LongIdJsonNode(node)), true);

            tx.success();
        }

        try (Transaction tx = database.beginTx()) {
            LongIdJsonNode jsonNode = mapper.readValue("{\"id\":" + i * 1000 + "}", LongIdJsonNode.class);
            Node node = jsonNode.producePropertyContainer(database, new TimesThousandNodeIdTransformer());

            assertEquals(i, node.getId());
            JSONAssert.assertEquals("{\"id\":" + i + ",\"properties\":{\"k1\":\"v1\"},\"labels\":[\"L1\"]}", mapper.writeValueAsString(new LongIdJsonNode(node)), true);

            tx.success();
        }

        try (Transaction tx = database.beginTx()) {
            LongIdJsonNode jsonNode = mapper.readValue("{\"id\":1, \"properties\":{\"k1\":\"v1\",\"k2\":2}}", LongIdJsonNode.class);
            jsonNode.producePropertyContainer(database);

            tx.success();
            fail();
        } catch (IllegalStateException e) {
            //ok
        }

        try (Transaction tx = database.beginTx()) {
            LongIdJsonNode jsonNode = mapper.readValue("{\"id\":10000}", LongIdJsonNode.class);
            jsonNode.producePropertyContainer(database, new TimesThousandNodeIdTransformer());

            tx.success();
            fail();
        } catch (NotFoundException e) {
            //ok
        }

        try (Transaction tx = database.beginTx()) {
            LongIdJsonNode jsonNode = mapper.readValue("{\"id\":1, \"labels\":[\"L1\",\"L2\"]}", LongIdJsonNode.class);
            jsonNode.producePropertyContainer(database);

            tx.success();
            fail();
        } catch (IllegalStateException e) {
            //ok
        }
    }

    @Test
    public void shouldCorrectlySerialiseArrayProps() throws JsonProcessingException, JSONException {
        try (Transaction tx = database.beginTx()) {
            Node node = database.createNode(Label.label("L1"), Label.label("L2"));
            node.setProperty("k1", new String[]{"v1", "v2"});
            node.setProperty("k2", new int[]{2, 3});

            JSONAssert.assertEquals("{\"id\":" + node.getId() + ",\"properties\":{\"k1\":[\"v1\",\"v2\"],\"k2\":[2,3]},\"labels\":[\"L1\",\"L2\"]}", mapper.writeValueAsString(new LongIdJsonNode(node)), true);
        }
    }

    @Test
    public void shouldCorrectlyProducePropertyContainer() {
        try (Transaction tx = database.beginTx()) {
            LongIdJsonNode jsonNode = new LongIdJsonNode(database.getNodeById(1));
            jsonNode.producePropertyContainer(database);
            tx.success();
        } catch (IllegalStateException e) {
            //ok
        }

        try (Transaction tx = database.beginTx()) {
            LongIdJsonNode jsonNode = new LongIdJsonNode(database.getNodeById(1), new TimesThousandNodeIdTransformer());
            jsonNode.producePropertyContainer(database);
            tx.success();
        } catch (IllegalStateException e) {
            //ok
        }

        try (Transaction tx = database.beginTx()) {
            LongIdJsonNode jsonNode = new LongIdJsonNode(database.getNodeById(1));
            jsonNode.producePropertyContainer(database, new TimesThousandNodeIdTransformer());
            tx.success();
        } catch (IllegalStateException e) {
            //ok
        }

        try (Transaction tx = database.beginTx()) {
            LongIdJsonNode jsonNode = new LongIdJsonNode(database.getNodeById(1), new TimesThousandNodeIdTransformer());
            jsonNode.producePropertyContainer(database, new TimesThousandNodeIdTransformer());
            tx.success();
        } catch (IllegalStateException e) {
            //ok
        }

        try (Transaction tx = database.beginTx()) {
            LongIdJsonNode jsonNode = new LongIdJsonNode(database.getNodeById(1), new String[0]);
            jsonNode.producePropertyContainer(database, new TimesThousandNodeIdTransformer());
            tx.success();
        } catch (IllegalStateException e) {
            //ok
        }

        try (Transaction tx = database.beginTx()) {
            LongIdJsonNode jsonNode = new LongIdJsonNode(database.getNodeById(1), new String[0], new TimesThousandNodeIdTransformer());
            jsonNode.producePropertyContainer(database, new TimesThousandNodeIdTransformer());
            tx.success();
        } catch (IllegalStateException e) {
            //ok
        }

        try (Transaction tx = database.beginTx()) {
            LongIdJsonNode jsonNode = new LongIdJsonNode(1);
            Node node = jsonNode.producePropertyContainer(database);
            assertEquals(node, database.getNodeById(1));

            tx.success();
        }

        try (Transaction tx = database.beginTx()) {
            LongIdJsonNode jsonNode = new LongIdJsonNode(1000);
            Node node = jsonNode.producePropertyContainer(database, new TimesThousandNodeIdTransformer());
            assertEquals(node, database.getNodeById(1));

            tx.success();
        }

        try (Transaction tx = database.beginTx()) {
            LongIdJsonNode jsonNode = new LongIdJsonNode(0, new String[]{"test"}, Collections.singletonMap("k", "v"));
            jsonNode.producePropertyContainer(database);
            tx.success();
        } catch (IllegalStateException e) {
            //ok
        }

        try (Transaction tx = database.beginTx()) {
            LongIdJsonNode jsonNode = new LongIdJsonNode(0, new String[]{"test"}, Collections.singletonMap("k", "v"));
            jsonNode.producePropertyContainer(database, new TimesThousandNodeIdTransformer());
            tx.success();
        } catch (IllegalStateException e) {
            //ok
        }

        try (Transaction tx = database.beginTx()) {
            LongIdJsonNode jsonNode = new LongIdJsonNode(555, new String[]{"test"}, Collections.singletonMap("k", "v"));
            jsonNode.producePropertyContainer(database);
            tx.success();
        } catch (IllegalStateException e) {
            //ok
        }

        try (Transaction tx = database.beginTx()) {
            LongIdJsonNode jsonNode = new LongIdJsonNode(5000, new String[]{"test"}, Collections.singletonMap("k", "v"));
            jsonNode.producePropertyContainer(database, new TimesThousandNodeIdTransformer());
            tx.success();
        } catch (IllegalStateException e) {
            //ok
        }

        try (Transaction tx = database.beginTx()) {
            LongIdJsonNode jsonNode = new LongIdJsonNode(new String[]{"Test"}, Collections.singletonMap("k", "v"));
            Node n = jsonNode.producePropertyContainer(database, new TimesThousandNodeIdTransformer());
            assertEquals(20, n.getId());

            tx.success();
        }

        try (Transaction tx = database.beginTx()) {
            LongIdJsonNode jsonNode = new LongIdJsonNode(new String[]{"Test"}, Collections.singletonMap("k", "v"));
            Node n = jsonNode.producePropertyContainer(database);
            assertEquals(2, n.getId());

            tx.success();
        }

        GraphUnit.assertSameGraph(database, "CREATE " +
                "(n1:L1:L2 {k1: 'v1', k2: 2})," +
                "()," +
                "(:Test {k: 'v'})," +
                "(:Test {k: 'v'})"
        );
    }

    private class TimesThousandNodeIdTransformer implements NodeIdTransformer<Long> {

        @Override
        public long toGraphId(Long id) {
            if (id == null) {
                return DetachedPropertyContainer.NEW;
            }

            return id / 1000;
        }

        @Override
        public Long fromContainer(Node pc) {
            return pc.getId() * 1000;
        }
    }
}
