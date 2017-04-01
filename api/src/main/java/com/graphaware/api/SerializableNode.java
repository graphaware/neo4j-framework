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

package com.graphaware.api;

import com.graphaware.common.transform.NodeIdTransformer;
import com.graphaware.common.representation.DetachedNode;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;

import java.util.Map;

/**
 * Serializable {@link DetachedNode} with custom node ID. It is recommended not to expose Neo4j internal IDs (graphId)
 * outside of the database.
 *
 * @param <ID> type of custom node ID.
 */
public class SerializableNode<ID> extends DetachedNode<ID> {

    private ID id;

    /**
     * Public no-arg constructor (for Jackson et. al.)
     */
    public SerializableNode() {
    }

    /**
     * Create a Serializable {@link DetachedNode} from a Neo4j node. All properties will be included.
     *
     * @param node        node to create the representation from.
     * @param transformer ID transformer.
     */
    public SerializableNode(Node node, NodeIdTransformer<ID> transformer) {
        this(node, null, transformer);
    }

    /**
     * Create a Serializable {@link DetachedNode} from a Neo4j node.
     *
     * @param node        node to create the representation from. Must not be <code>null</code>.
     * @param properties  keys of properties to be included in the representation.
     *                    Can be <code>null</code>, which represents all. Empty array represents none.
     * @param transformer ID transformer.
     */
    public SerializableNode(Node node, String[] properties, NodeIdTransformer<ID> transformer) {
        super(node, properties);
        setId(transformer.fromContainer(node));
    }

    /**
     * Create a Serializable {@link DetachedNode} from custom node ID.
     *
     * @param id          custom ID of the node. Can be <code>null</code> to represent a new node.
     */
    public SerializableNode(ID id) {
        this.id = id;
    }

    /**
     * Construct Serializable {@link DetachedNode} of a node.
     *
     * @param id         custom ID of the node. Can be <code>null</code> to represent a new node.
     * @param labels     of the new node representation.
     * @param properties of the new node representation.
     */
    public SerializableNode(ID id, String[] labels, Map<String, Object> properties) {
        super(labels, properties);
        this.id = id;
    }

    /**
     * Produce a {@link Node} from this representation. This means either fetch the node from the
     * given database (iff id is set), or create it.
     *
     * @param database    to create/fetch node in.
     * @param transformer ID transformer.
     * @return node.
     */
    public Node producePropertyContainer(GraphDatabaseService database, NodeIdTransformer<ID> transformer) {
        setGraphId(transformer.toGraphId(id));
        return super.producePropertyContainer(database);
    }

    public ID getId() {
        return id;
    }

    public void setId(ID id) {
        this.id = id;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        SerializableNode<?> that = (SerializableNode<?>) o;

        return !(id != null ? !id.equals(that.id) : that.id != null);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (id != null ? id.hashCode() : 0);
        return result;
    }
}
