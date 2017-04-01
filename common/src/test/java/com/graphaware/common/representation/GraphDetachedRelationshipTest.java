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

package com.graphaware.common.representation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONException;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.Collections;

import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;

public class GraphDetachedRelationshipTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void relationshipRepresentationIsCorrectlyConvertedToJson() throws JsonProcessingException, JSONException {
        DetachedRelationship representation = new GraphDetachedRelationship(0, 1, 2, "TYPE", Collections.singletonMap("key", "value"));
        assertEquals("{\"graphId\":0,\"properties\":{\"key\":\"value\"},\"startNodeGraphId\":1,\"endNodeGraphId\":2,\"type\":\"TYPE\"}", mapper.writeValueAsString(representation), true);
    }

    @Test
    public void relationshipRepresentationIsCorrectlyConvertedFromJson() throws IOException, JSONException {
        String json = "{\"graphId\":0,\"properties\":{\"key\":\"value\"},\"startNodeGraphId\":1,\"endNodeGraphId\":2,\"type\":\"TYPE\"}";
        Assert.assertEquals(new GraphDetachedRelationship(0, 1, 2, "TYPE", Collections.singletonMap("key", "value")), mapper.readValue(json, GraphDetachedRelationship.class));
    }
}
