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
import com.graphaware.common.transform.RelationshipIdTransformer;
import org.json.JSONException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.neo4j.graphdb.*;
import org.skyscreamer.jsonassert.JSONAssert;

import java.util.Collections;

@ExtendWith(Neo4jExtension.class)
public class LongIdJsonRelationshipTest {

    @InjectNeo4j
    private GraphDatabaseService database;

    private long rId1, rId2, a, b;

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

            Relationship r2 = node1.createRelationshipTo(node2, RelationshipType.withName("R2"));
            r2.setProperty("k1", "v2");
            r2.setProperty("k2", 4);

            a = node1.getId();
            b = node2.getId();
            rId1 = r.getId();
            rId2 = r2.getId();

            tx.commit();
        }
    }

    @Test
    public void shouldCorrectlySerialiseRelationships() throws JsonProcessingException, JSONException {
        try (Transaction tx = database.beginTx()) {
            Relationship r = tx.getRelationshipById(rId1);
            Relationship r2 = tx.getRelationshipById(rId2);

            JSONAssert.assertEquals("{\"id\":" + rId1 + ",\"properties\":{\"k1\":\"v1\",\"k2\":2},\"startNodeId\":" + a + ",\"endNodeId\":" + b + ",\"type\":\"R\"}", mapper.writeValueAsString(new LongIdJsonRelationship(r, null)), true);
            JSONAssert.assertEquals("{\"id\":" + rId1 + ",\"properties\":{\"k1\":\"v1\",\"k2\":2},\"startNodeId\":" + a + ",\"endNodeId\":" + b + ",\"type\":\"R\"}", mapper.writeValueAsString(new LongIdJsonRelationship(r)), true);

            JSONAssert.assertEquals("{\"id\":" + rId2 * 1000 + ",\"properties\":{\"k1\":\"v2\",\"k2\":4},\"startNodeId\":" + a * 1000 + ",\"endNodeId\":" + b * 1000 + ",\"type\":\"R2\"}", mapper.writeValueAsString(new LongIdJsonRelationship(r2, new TimesThousandRelationshipIdTransformer(), new TimesThousandNodeIdTransformer())), true);
            JSONAssert.assertEquals("{\"id\":" + rId2 * 1000 + ",\"properties\":{\"k1\":\"v2\"},\"startNodeId\":" + a * 1000 + ",\"endNodeId\":" + b * 1000 + ",\"type\":\"R2\"}", mapper.writeValueAsString(new LongIdJsonRelationship(r2, new String[]{"k1"}, new TimesThousandRelationshipIdTransformer(), new TimesThousandNodeIdTransformer())), true);

            JSONAssert.assertEquals("{\"id\":33,\"properties\":{\"a\":\"b\"},\"startNodeId\":44,\"endNodeId\":55,\"type\":\"XX\"}", mapper.writeValueAsString(new LongIdJsonRelationship(33, 44, 55, "XX", Collections.singletonMap("a", "b"))), true);

            SerializationSpecification jsonInput1 = new SerializationSpecification();
            jsonInput1.setRelationshipProperties(null);
            JSONAssert.assertEquals("{\"id\":" + rId1 + ",\"properties\":{\"k1\":\"v1\",\"k2\":2},\"startNodeId\":" + a + ",\"endNodeId\":" + b + ",\"type\":\"R\"}", mapper.writeValueAsString(new LongIdJsonRelationship(r, jsonInput1.getRelationshipProperties())), true);

            SerializationSpecification jsonInput2 = new SerializationSpecification();
            jsonInput2.setRelationshipProperties(new String[]{"k1"});
            JSONAssert.assertEquals("{\"id\":" + rId1 + ",\"properties\":{\"k1\":\"v1\"},\"startNodeId\":" + a + ",\"endNodeId\":" + b + ",\"type\":\"R\"}", mapper.writeValueAsString(new LongIdJsonRelationship(r, jsonInput2.getRelationshipProperties())), true);

            SerializationSpecification jsonInput3 = new SerializationSpecification();
            jsonInput3.setRelationshipProperties(new String[]{"k3"});
            JSONAssert.assertEquals("{\"id\":" + rId1 + ",\"startNodeId\":" + a + ",\"endNodeId\":" + b + ",\"type\":\"R\",\"properties\":{}}", mapper.writeValueAsString(new LongIdJsonRelationship(r, jsonInput3.getRelationshipProperties())), true);

            SerializationSpecification jsonInput4 = new SerializationSpecification();
            jsonInput4.setRelationshipProperties(new String[0]);
            JSONAssert.assertEquals("{\"id\":" + rId1 + ",\"startNodeId\":" + a + ",\"endNodeId\":" + b + ",\"type\":\"R\",\"properties\":{}}", mapper.writeValueAsString(new LongIdJsonRelationship(r, jsonInput4.getRelationshipProperties())), true);
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

    private class TimesThousandRelationshipIdTransformer implements RelationshipIdTransformer<Long> {

    @Override
        public long toGraphId(Long id) {
            if (id == null) {
                return DetachedEntity.NEW;
            }

            return id / 1000;
        }

    @Override
        public Long fromEntity(Relationship entity) {
            return entity.getId() * 1000;
        }
    }
}
