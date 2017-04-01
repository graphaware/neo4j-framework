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

import com.graphaware.common.transform.NodeIdTransformer;
import com.graphaware.common.transform.TrivialNodeIdTransformer;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;

import java.util.Map;

/**
 * {@link JsonNode} with {@link Long} custom ID. Can be easily used to directly represent Neo4j nodes by using constructors
 * without supplying {@link NodeIdTransformer}. In these cases, {@link TrivialNodeIdTransformer} is used and custom node ID
 * becomes the internal Neo4j node ID. It is recommended, however, that true custom IDs are used, such as UUIDs.
 */
public class LongIdJsonNode extends JsonNode<Long> {

    public LongIdJsonNode() {
    }

    public LongIdJsonNode(Node node) {
        this(node, TrivialNodeIdTransformer.getInstance());
    }

    public LongIdJsonNode(Node node, NodeIdTransformer<Long> transformer) {
        super(node, transformer);
    }

    public LongIdJsonNode(Node node, String[] properties) {
        super(node, properties, TrivialNodeIdTransformer.getInstance());
    }

    public LongIdJsonNode(Node node, String[] properties, NodeIdTransformer<Long> transformer) {
        super(node, properties, transformer);
    }

    public LongIdJsonNode(long id) {
        super(id);
    }

    public LongIdJsonNode(String[] labels, Map<String, Object> properties) {
        super(null, labels, properties);
    }

    public LongIdJsonNode(long id, String[] labels, Map<String, Object> properties) {
        super(id, labels, properties);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Node producePropertyContainer(GraphDatabaseService database) {
        return producePropertyContainer(database, TrivialNodeIdTransformer.getInstance());
    }
}
