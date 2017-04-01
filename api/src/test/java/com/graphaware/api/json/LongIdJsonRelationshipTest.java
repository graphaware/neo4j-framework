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

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.graphaware.api.SerializationSpecification;
import com.graphaware.common.transform.NodeIdTransformer;
import com.graphaware.common.transform.RelationshipIdTransformer;
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

public class LongIdJsonRelationshipTest {

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

            Relationship r = node1.createRelationshipTo(node2, RelationshipType.withName("R"));
            r.setProperty("k1", "v1");
            r.setProperty("k2", 2);

            Relationship r2 = node1.createRelationshipTo(node2, RelationshipType.withName("R2"));
            r2.setProperty("k1", "v2");
            r2.setProperty("k2", 4);

            tx.success();
        }
    }

    @After
    public void tearDown() {
        database.shutdown();
    }

    @Test
    public void shouldCorrectlySerialiseRelationships() throws JsonProcessingException, JSONException {
        try (Transaction tx = database.beginTx()) {
            Relationship r = database.getRelationshipById(0);
            Relationship r2 = database.getRelationshipById(1);

            JSONAssert.assertEquals("{\"id\":0,\"properties\":{\"k1\":\"v1\",\"k2\":2},\"startNodeId\":0,\"endNodeId\":1,\"type\":\"R\"}", mapper.writeValueAsString(new LongIdJsonRelationship(r, null)), true);
            JSONAssert.assertEquals("{\"id\":0,\"properties\":{\"k1\":\"v1\",\"k2\":2},\"startNodeId\":0,\"endNodeId\":1,\"type\":\"R\"}", mapper.writeValueAsString(new LongIdJsonRelationship(r)), true);

            JSONAssert.assertEquals("{\"id\":1000,\"properties\":{\"k1\":\"v2\",\"k2\":4},\"startNodeId\":0,\"endNodeId\":1000,\"type\":\"R2\"}", mapper.writeValueAsString(new LongIdJsonRelationship(r2, new TimesThousandRelationshipIdTransformer(), new TimesThousandNodeIdTransformer())), true);
            JSONAssert.assertEquals("{\"id\":1000,\"properties\":{\"k1\":\"v2\"},\"startNodeId\":0,\"endNodeId\":1000,\"type\":\"R2\"}", mapper.writeValueAsString(new LongIdJsonRelationship(r2, new String[]{"k1"}, new TimesThousandRelationshipIdTransformer(), new TimesThousandNodeIdTransformer())), true);

            JSONAssert.assertEquals("{\"id\":33,\"properties\":{\"a\":\"b\"},\"startNodeId\":44,\"endNodeId\":55,\"type\":\"XX\"}", mapper.writeValueAsString(new LongIdJsonRelationship(33, 44, 55, "XX", Collections.<String, Object>singletonMap("a", "b"))), true);

            SerializationSpecification jsonInput1 = new SerializationSpecification();
            jsonInput1.setRelationshipProperties(null);
            JSONAssert.assertEquals("{\"id\":0,\"properties\":{\"k1\":\"v1\",\"k2\":2},\"startNodeId\":0,\"endNodeId\":1,\"type\":\"R\"}", mapper.writeValueAsString(new LongIdJsonRelationship(r, jsonInput1.getRelationshipProperties())), true);

            SerializationSpecification jsonInput2 = new SerializationSpecification();
            jsonInput2.setRelationshipProperties(new String[]{"k1"});
            JSONAssert.assertEquals("{\"id\":0,\"properties\":{\"k1\":\"v1\"},\"startNodeId\":0,\"endNodeId\":1,\"type\":\"R\"}", mapper.writeValueAsString(new LongIdJsonRelationship(r, jsonInput2.getRelationshipProperties())), true);

            SerializationSpecification jsonInput3 = new SerializationSpecification();
            jsonInput3.setRelationshipProperties(new String[]{"k3"});
            JSONAssert.assertEquals("{\"id\":0,\"startNodeId\":0,\"endNodeId\":1,\"type\":\"R\",\"properties\":{}}", mapper.writeValueAsString(new LongIdJsonRelationship(r, jsonInput3.getRelationshipProperties())), true);

            SerializationSpecification jsonInput4 = new SerializationSpecification();
            jsonInput4.setRelationshipProperties(new String[0]);
            JSONAssert.assertEquals("{\"id\":0,\"startNodeId\":0,\"endNodeId\":1,\"type\":\"R\",\"properties\":{}}", mapper.writeValueAsString(new LongIdJsonRelationship(r, jsonInput4.getRelationshipProperties())), true);
        }
    }

    @Test
    public void shouldCorrectlyDeserialiseRelationships() throws IOException, JSONException {
        try (Transaction tx = database.beginTx()) {
            LongIdJsonRelationship jsonRel = mapper.readValue("{\"id\":0}", LongIdJsonRelationship.class);
            Relationship rel = jsonRel.producePropertyContainer(database);

            assertEquals(0, rel.getId());
            assertEquals(rel, database.getRelationshipById(0));

            tx.success();
        }

        try (Transaction tx = database.beginTx()) {
            LongIdJsonRelationship jsonRel = new LongIdJsonRelationship(0);
            Relationship rel = jsonRel.producePropertyContainer(database);

            assertEquals(0, rel.getId());
            assertEquals(rel, database.getRelationshipById(0));

            tx.success();
        }

        try (Transaction tx = database.beginTx()) {
            LongIdJsonRelationship jsonRel = new LongIdJsonRelationship(1000);
            Relationship rel = jsonRel.producePropertyContainer(database, new TimesThousandRelationshipIdTransformer(), new TimesThousandNodeIdTransformer());

            assertEquals(1, rel.getId());
            assertEquals(rel, database.getRelationshipById(1));

            tx.success();
        }

        try (Transaction tx = database.beginTx()) {
            LongIdJsonRelationship jsonRel = mapper.readValue("{\"startNodeId\":0, \"endNodeId\":1, \"properties\":{\"k1\":\"v1\",\"k2\":2},\"type\":\"R\"}", LongIdJsonRelationship.class);
            Relationship rel = jsonRel.producePropertyContainer(database);

            assertEquals(2, rel.getId());
            JSONAssert.assertEquals("{\"id\":2,\"properties\":{\"k1\":\"v1\",\"k2\":2},\"startNodeId\":0,\"endNodeId\":1,\"type\":\"R\"}", mapper.writeValueAsString(new LongIdJsonRelationship(rel)), true);

            tx.success();
        }

        try (Transaction tx = database.beginTx()) {
            LongIdJsonRelationship jsonRel = mapper.readValue("{\"startNodeId\":0, \"endNodeId\":1, \"properties\":{},\"type\":\"R\"}", LongIdJsonRelationship.class);
            Relationship rel = jsonRel.producePropertyContainer(database);

            assertEquals(3, rel.getId());
            JSONAssert.assertEquals("{\"id\":3,\"startNodeId\":0,\"endNodeId\":1,\"type\":\"R\",\"properties\":{}}", mapper.writeValueAsString(new LongIdJsonRelationship(rel)), true);

            tx.success();
        }

        try (Transaction tx = database.beginTx()) {
            LongIdJsonRelationship jsonRel = mapper.readValue("{\"startNodeId\":0, \"endNodeId\":1,\"type\":\"R\"}", LongIdJsonRelationship.class);
            Relationship rel = jsonRel.producePropertyContainer(database);

            assertEquals(4, rel.getId());
            JSONAssert.assertEquals("{\"id\":4,\"startNodeId\":0,\"endNodeId\":1,\"type\":\"R\",\"properties\":{}}", mapper.writeValueAsString(new LongIdJsonRelationship(rel)), true);

            tx.success();
        }

        try (Transaction tx = database.beginTx()) {
            LongIdJsonRelationship jsonRel = new LongIdJsonRelationship(0, 1, "R", Collections.<String, Object>singletonMap("k1", "v1"));
            Relationship rel = jsonRel.producePropertyContainer(database);

            assertEquals(5, rel.getId());
            JSONAssert.assertEquals("{\"id\":5,\"properties\":{\"k1\":\"v1\"},\"startNodeId\":0,\"endNodeId\":1,\"type\":\"R\"}", mapper.writeValueAsString(new LongIdJsonRelationship(rel)), true);

            tx.success();
        }

        try (Transaction tx = database.beginTx()) {
            LongIdJsonRelationship jsonRel = mapper.readValue("{\"id\":0, \"properties\":{\"k1\":\"v1\",\"k2\":2}}", LongIdJsonRelationship.class);
            jsonRel.producePropertyContainer(database);

            tx.success();
            fail();
        } catch (IllegalStateException e) {
            //ok
        }

        try (Transaction tx = database.beginTx()) {
            LongIdJsonRelationship jsonRel = mapper.readValue("{\"id\":0, \"type\":\"R\"}", LongIdJsonRelationship.class);
            jsonRel.producePropertyContainer(database);

            tx.success();
            fail();
        } catch (IllegalStateException e) {
            //ok
        }

        try (Transaction tx = database.beginTx()) {
            LongIdJsonRelationship jsonRel = mapper.readValue("{\"id\":0, \"startNodeId\":1}", LongIdJsonRelationship.class);
            jsonRel.producePropertyContainer(database);

            tx.success();
            fail();
        } catch (IllegalStateException e) {
            //ok
        }

        try (Transaction tx = database.beginTx()) {
            LongIdJsonRelationship jsonRel = mapper.readValue("{\"id\":0, \"endNodeId\":0}", LongIdJsonRelationship.class);
            jsonRel.producePropertyContainer(database);

            tx.success();
            fail();
        } catch (IllegalStateException e) {
            //ok
        }

        try (Transaction tx = database.beginTx()) {
            LongIdJsonRelationship jsonRel = mapper.readValue("{\"endNodeId\":1, \"properties\":{},\"type\":\"R\"}", LongIdJsonRelationship.class);
            jsonRel.producePropertyContainer(database);

            tx.success();
            fail();
        } catch (IllegalStateException e) {
            //ok
        }

        try (Transaction tx = database.beginTx()) {
            LongIdJsonRelationship jsonRel = mapper.readValue("{\"startNodeId\":0, \"properties\":{},\"type\":\"R\"}", LongIdJsonRelationship.class);
            jsonRel.producePropertyContainer(database);

            tx.success();
            fail();
        } catch (IllegalStateException e) {
            //ok
        }

        try (Transaction tx = database.beginTx()) {
            LongIdJsonRelationship jsonRel = mapper.readValue("{\"startNodeId\":0, \"endNodeId\":1, \"properties\":{}}", LongIdJsonRelationship.class);
            jsonRel.producePropertyContainer(database);

            tx.success();
            fail();
        } catch (IllegalStateException e) {
            //ok
        }

        try (Transaction tx = database.beginTx()) {
            LongIdJsonRelationship jsonRel = mapper.readValue("{\"startNodeId\":x, \"endNodeId\":1, \"properties\":{},\"type\":\"R\"}", LongIdJsonRelationship.class);
            jsonRel.producePropertyContainer(database);

            tx.success();
            fail();
        } catch (JsonParseException e) {
            //ok
        }

        try (Transaction tx = database.beginTx()) {
            LongIdJsonRelationship jsonRel = mapper.readValue("{\"startNodeId\":0, \"endNodeId\":1, \"properties\":{},\"type\":\"\"}", LongIdJsonRelationship.class);
            jsonRel.producePropertyContainer(database);

            tx.success();
            fail();
        } catch (IllegalStateException | IllegalArgumentException | JsonMappingException e) {
            //ok
        }

        try (Transaction tx = database.beginTx()) {
            LongIdJsonRelationship jsonRel = mapper.readValue("{\"startNodeId\":0, \"endNodeId\":1, \"properties\":{},\"type\":null}", LongIdJsonRelationship.class);
            jsonRel.producePropertyContainer(database);

            tx.success();
            fail();
        } catch (IllegalStateException | IllegalArgumentException | JsonMappingException e) {
            //ok
        }

        try (Transaction tx = database.beginTx()) {
            LongIdJsonRelationship jsonRel = mapper.readValue("{\"startNodeId\":0, \"endNodeId\":x, \"properties\":{},\"type\":\"R\"}", LongIdJsonRelationship.class);
            jsonRel.producePropertyContainer(database);

            tx.success();
            fail();
        } catch (JsonParseException e) {
            //ok
        }
    }

    @Test
    public void shouldCorrectlyProducePropertyContainer() {
        try (Transaction tx = database.beginTx()) {
            LongIdJsonRelationship rel = new LongIdJsonRelationship(database.getRelationshipById(1), new TimesThousandRelationshipIdTransformer(), new TimesThousandNodeIdTransformer());
            rel.producePropertyContainer(database);
            tx.success();
        } catch (IllegalStateException e) {
            //ok
        }

        try (Transaction tx = database.beginTx()) {
            LongIdJsonRelationship rel = new LongIdJsonRelationship(database.getRelationshipById(1), new TimesThousandRelationshipIdTransformer(), new TimesThousandNodeIdTransformer());
            rel.producePropertyContainer(database, new TimesThousandRelationshipIdTransformer(), new TimesThousandNodeIdTransformer());
            tx.success();
        } catch (IllegalStateException e) {
            //ok
        }

        try (Transaction tx = database.beginTx()) {
            LongIdJsonRelationship rel = new LongIdJsonRelationship(database.getRelationshipById(1));
            rel.producePropertyContainer(database);
            tx.success();
        } catch (IllegalStateException e) {
            //ok
        }

        try (Transaction tx = database.beginTx()) {
            LongIdJsonRelationship rel = new LongIdJsonRelationship(database.getRelationshipById(1));
            rel.producePropertyContainer(database, new TimesThousandRelationshipIdTransformer(), new TimesThousandNodeIdTransformer());
            tx.success();
        } catch (IllegalStateException e) {
            //ok
        }

        try (Transaction tx = database.beginTx()) {
            LongIdJsonRelationship rel = new LongIdJsonRelationship(database.getRelationshipById(0), new String[0], new TimesThousandRelationshipIdTransformer(), new TimesThousandNodeIdTransformer());
            rel.producePropertyContainer(database);
            tx.success();
        } catch (IllegalStateException e) {
            //ok
        }

        try (Transaction tx = database.beginTx()) {
            LongIdJsonRelationship rel = new LongIdJsonRelationship(database.getRelationshipById(0), new String[0], new TimesThousandRelationshipIdTransformer(), new TimesThousandNodeIdTransformer());
            rel.producePropertyContainer(database, new TimesThousandRelationshipIdTransformer(), new TimesThousandNodeIdTransformer());
            tx.success();
        } catch (IllegalStateException e) {
            //ok
        }

        try (Transaction tx = database.beginTx()) {
            LongIdJsonRelationship jsonRel = new LongIdJsonRelationship(0);
            Relationship r = jsonRel.producePropertyContainer(database);
            assertEquals(r, database.getRelationshipById(0));

            tx.success();
        }

        try (Transaction tx = database.beginTx()) {
            LongIdJsonRelationship jsonRel = new LongIdJsonRelationship(1000);
            Relationship r = jsonRel.producePropertyContainer(database, new TimesThousandRelationshipIdTransformer(), new TimesThousandNodeIdTransformer());
            assertEquals(r, database.getRelationshipById(1));

            tx.success();
        }

        try (Transaction tx = database.beginTx()) {
            LongIdJsonRelationship jsonRel = new LongIdJsonRelationship(0, 0, 0, "test", Collections.<String, Object>emptyMap());
            jsonRel.producePropertyContainer(database);
            tx.success();
        } catch (IllegalStateException e) {
            //ok
        }

        try (Transaction tx = database.beginTx()) {
            LongIdJsonRelationship jsonRel = new LongIdJsonRelationship(0, 0, 0, "test", Collections.<String, Object>emptyMap());
            jsonRel.producePropertyContainer(database, new TimesThousandRelationshipIdTransformer(), new TimesThousandNodeIdTransformer());
            tx.success();
        } catch (IllegalStateException e) {
            //ok
        }

        try (Transaction tx = database.beginTx()) {
            LongIdJsonRelationship jsonRel = new LongIdJsonRelationship(0, 1000, "TEST", Collections.<String, Object>singletonMap("k", "v"));
            Relationship r = jsonRel.producePropertyContainer(database, new TimesThousandRelationshipIdTransformer(), new TimesThousandNodeIdTransformer());
            assertEquals(2, r.getId());

            tx.success();
        }

        try (Transaction tx = database.beginTx()) {
            LongIdJsonRelationship jsonRel = new LongIdJsonRelationship(0, 1, "TEST", Collections.<String, Object>singletonMap("k", "v"));
            Relationship r = jsonRel.producePropertyContainer(database);
            assertEquals(3, r.getId());

            tx.success();
        }

        GraphUnit.assertSameGraph(database, "CREATE " +
                "(n1:L1:L2 {k1: 'v1', k2: 2})," +
                "(n2)," +
                "(n1)-[:R {k1: 'v1', k2: 2}]->(n2)," +
                "(n1)-[:R2 {k1: 'v2', k2: 4}]->(n2)," +
                "(n1)-[:TEST {k: 'v'}]->(n2)," +
                "(n1)-[:TEST {k: 'v'}]->(n2)");
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

    private class TimesThousandRelationshipIdTransformer implements RelationshipIdTransformer<Long> {

        @Override
        public long toGraphId(Long id) {
            if (id == null) {
                return DetachedPropertyContainer.NEW;
            }

            return id / 1000;
        }

        @Override
        public Long fromContainer(Relationship pc) {
            return pc.getId() * 1000;
        }
    }
}
