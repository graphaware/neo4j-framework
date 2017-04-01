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

import com.graphaware.api.SerializableNode;
import com.graphaware.api.SerializableRelationship;
import com.graphaware.api.transform.NodeTransformer;
import com.graphaware.api.transform.RelationshipTransformer;
import com.graphaware.api.transform.TrivialNodeTransformer;
import com.graphaware.api.transform.TrivialRelationshipTransformer;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import java.util.HashSet;
import java.util.Set;

/**
 * Abstract base class for convenient JSON-serializable graphs with fluent interface.
 *
 * @param <T> type of the concrete class extending this.
 */
public abstract class JsonGraph<T extends JsonGraph<T>> {

    private Set<SerializableNode> nodes = new HashSet<>();
    private Set<SerializableRelationship> relationships = new HashSet<>();

    public T addNode(SerializableNode node) {
        nodes.add(node);
        return self();
    }

    public T addNode(Node node) {
        return addNode(node, TrivialNodeTransformer.getInstance());
    }

    public T addNode(Node node, NodeTransformer<?> transformer) {
        return addNode(transformer.transform(node));
    }

    public T addNodes(Iterable<Node> nodes) {
        for (Node node : nodes) {
            addNode(node);
        }

        return self();
    }

    public T addNodes(Iterable<Node> nodes, NodeTransformer transformer) {
        for (Node node : nodes) {
            addNode(node, transformer);
        }

        return self();
    }

    public T addNodes(NodeTransformer transformer, Node... nodes) {
        for (Node node : nodes) {
            addNode(node, transformer);
        }

        return self();
    }

    public T addNodes(Node... nodes) {
        for (Node node : nodes) {
            addNode(node);
        }

        return self();
    }

    public T addRelationship(SerializableRelationship relationship) {
        relationships.add(relationship);
        return self();
    }

    public T addRelationship(Relationship rel) {
        return addRelationship(rel, TrivialRelationshipTransformer.getInstance());
    }

    public T addRelationship(Relationship rel, RelationshipTransformer<?> transformer) {
        return addRelationship(transformer.transform(rel));
    }

    public T addRelationships(Iterable<Relationship> relationships) {
        for (Relationship rel : relationships) {
            addRelationship(rel);
        }

        return self();
    }

    public T addRelationships(Iterable<Relationship> relationships, RelationshipTransformer transformer) {
        for (Relationship rel : relationships) {
            addRelationship(rel, transformer);
        }

        return self();
    }

    public T addRelationships(RelationshipTransformer transformer, Relationship... relationships) {
        for (Relationship rel : relationships) {
            addRelationship(rel, transformer);
        }

        return self();
    }

    public T addRelationships(Relationship... relationships) {
        for (Relationship rel : relationships) {
            addRelationship(rel);
        }

        return self();
    }

    public T merge(T graph) {
        for (SerializableNode node : graph.getNodes()) {
            addNode(node);
        }

        for (SerializableRelationship relationship : graph.getRelationships()) {
            addRelationship(relationship);
        }

        return self();
    }

    public Set<SerializableNode> getNodes() {
        return nodes;
    }

    public void setNodes(Set<SerializableNode> nodes) {
        this.nodes = nodes;
    }

    public Set<SerializableRelationship> getRelationships() {
        return relationships;
    }

    public void setRelationships(Set<SerializableRelationship> relationships) {
        this.relationships = relationships;
    }

    protected abstract T self();
}
