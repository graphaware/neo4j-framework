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
import com.graphaware.common.transform.RelationshipIdTransformer;
import com.graphaware.common.transform.TrivialNodeIdTransformer;
import com.graphaware.common.transform.TrivialRelationshipIdTransformer;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Relationship;

import java.util.Map;

/**
 * {@link JsonRelationship} with {@link Long} custom ID. Can be easily used to directly represent Neo4j relationships by using constructors
 * without supplying {@link NodeIdTransformer} or {@link RelationshipIdTransformer}.
 *
 * In these cases, {@link TrivialNodeIdTransformer} and {@link TrivialRelationshipIdTransformer} are used and custom node/relationship IDs
 * become the internal Neo4j node IDs. It is recommended, however, that true custom IDs are used, such as UUIDs.
 */
public class LongIdJsonRelationship extends JsonRelationship<Long> {

    public LongIdJsonRelationship() {
    }

    public LongIdJsonRelationship(Relationship relationship, RelationshipIdTransformer<Long> relationshipIdTransformer, NodeIdTransformer<Long> nodeIdTransformer) {
        super(relationship, relationshipIdTransformer, nodeIdTransformer);
    }

    public LongIdJsonRelationship(Relationship relationship) {
        super(relationship, TrivialRelationshipIdTransformer.getInstance(), TrivialNodeIdTransformer.getInstance());
    }

    public LongIdJsonRelationship(Relationship relationship, String[] properties, RelationshipIdTransformer<Long> relationshipIdTransformer, NodeIdTransformer<Long> nodeIdTransformer) {
        super(relationship, properties, relationshipIdTransformer, nodeIdTransformer);
    }

    public LongIdJsonRelationship(Relationship relationship, String[] properties) {
        super(relationship, properties, TrivialRelationshipIdTransformer.getInstance(), TrivialNodeIdTransformer.getInstance());
    }

    public LongIdJsonRelationship(long id) {
        super(id);
    }

    public LongIdJsonRelationship(long id, long startNodeId, long endNodeId, String type, Map<String, Object> properties) {
        super(id, startNodeId, endNodeId, type, properties);
    }

    public LongIdJsonRelationship(long startNodeId, long endNodeId, String type, Map<String, Object> properties) {
        super(null, startNodeId, endNodeId, type, properties);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Relationship producePropertyContainer(GraphDatabaseService database) {
        return producePropertyContainer(database, TrivialRelationshipIdTransformer.getInstance(), TrivialNodeIdTransformer.getInstance());
    }
}

