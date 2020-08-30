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
import com.graphaware.common.representation.DetachedEntity;
import com.graphaware.common.representation.SerializationSpecification;
import com.graphaware.common.transform.NodeIdTransformer;
import com.graphaware.common.transform.RelationshipIdTransformer;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.neo4j.graphdb.*;
import org.skyscreamer.jsonassert.JSONAssert;

import java.util.Collections;

public class LongIdJsonRelationshipTest extends UnitTest {

    private ObjectMapper mapper = new ObjectMapper();

    @Override
    protected void populate(GraphDatabaseService database) {
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

    @Test
    public void shouldCorrectlySerialiseRelationships() throws JsonProcessingException, JSONException {
        try (Transaction tx = database.beginTx()) {
            Relationship r = database.getRelationshipById(0);
            Relationship r2 = database.getRelationshipById(1);

            JSONAssert.assertEquals("{\"id\":0,\"properties\":{\"k1\":\"v1\",\"k2\":2},\"startNodeId\":0,\"endNodeId\":1,\"type\":\"R\"}", mapper.writeValueAsString(new LongIdJsonRelationship(r, null)), true);
            JSONAssert.assertEquals("{\"id\":0,\"properties\":{\"k1\":\"v1\",\"k2\":2},\"startNodeId\":0,\"endNodeId\":1,\"type\":\"R\"}", mapper.writeValueAsString(new LongIdJsonRelationship(r)), true);

            JSONAssert.assertEquals("{\"id\":1000,\"properties\":{\"k1\":\"v2\",\"k2\":4},\"startNodeId\":0,\"endNodeId\":1000,\"type\":\"R2\"}", mapper.writeValueAsString(new LongIdJsonRelationship(r2, new TimesThousandRelationshipIdTransformer(), new TimesThousandNodeIdTransformer())), true);
            JSONAssert.assertEquals("{\"id\":1000,\"properties\":{\"k1\":\"v2\"},\"startNodeId\":0,\"endNodeId\":1000,\"type\":\"R2\"}", mapper.writeValueAsString(new LongIdJsonRelationship(r2, new String[]{"k1"}, new TimesThousandRelationshipIdTransformer(), new TimesThousandNodeIdTransformer())), true);

            JSONAssert.assertEquals("{\"id\":33,\"properties\":{\"a\":\"b\"},\"startNodeId\":44,\"endNodeId\":55,\"type\":\"XX\"}", mapper.writeValueAsString(new LongIdJsonRelationship(33, 44, 55, "XX", Collections.singletonMap("a", "b"))), true);

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
