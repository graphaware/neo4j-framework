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

package com.graphaware.common.wrapper;

import org.neo4j.graphdb.*;
import org.neo4j.graphdb.traversal.Traverser;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Base class for custom {@link org.neo4j.graphdb.PropertyContainer} implementations.
 */
public abstract class BasePropertyContainer implements PropertyContainer {

    //Typically no need to override:

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getProperty(String key, Object defaultValue) {
        if (!hasProperty(key)) {
            return defaultValue;
        }
        return getProperty(key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Object> getAllProperties() {
        Map<String, Object> result = new HashMap<>();

        for (String key : getPropertyKeys()) {
            result.put(key, getProperty(key));
        }

        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Object> getProperties(String... keys) {
        Map<String, Object> result = new HashMap<>();

        for (String key : keys) {
            if (!hasProperty(key)) {
                continue;
            }
            result.put(key, getProperty(key));
        }

        return result;
    }

    //the following methods intentionally break object-orientation a bit to keep the rest of the codebase DRY

    /**
     * @see org.neo4j.graphdb.Relationship#getOtherNode(org.neo4j.graphdb.Node).
     */
    public Node getOtherNode(Node node) {
        Relationship self = relationship();

        Node startNode = self.getStartNode();
        Node endNode = self.getEndNode();

        if (node.getId() == startNode.getId()) {
            return endNode;
        }

        if (node.getId() == endNode.getId()) {
            return startNode;
        }

        throw new IllegalArgumentException("Node with ID " + node.getId() + " does not participate in relationship ID " + self.getId());
    }

    /**
     * @see org.neo4j.graphdb.Relationship#getNodes().
     */
    public Node[] getNodes() {
        Relationship self = relationship();
        return new Node[]{self.getStartNode(), self.getEndNode()};
    }

    /**
     * @see org.neo4j.graphdb.Node#getSingleRelationship(org.neo4j.graphdb.RelationshipType, org.neo4j.graphdb.Direction).
     */
    public Relationship getSingleRelationship(RelationshipType type, Direction dir) {
        Node self = node();

        Iterator<Relationship> iterator = self.getRelationships(type, dir).iterator();
        if (!iterator.hasNext()) {
            return null;
        }

        Relationship toReturn = iterator.next();

        if (iterator.hasNext()) {
            throw new NotFoundException("More than one relationship[" + type + ", " + dir + "] found for " + self);
        }

        return toReturn;
    }

    /**
     * @see org.neo4j.graphdb.Node#hasRelationship()
     */
    public boolean hasRelationship() {
        return node().getRelationships().iterator().hasNext();
    }

    /**
     * @see org.neo4j.graphdb.Node#hasRelationship(org.neo4j.graphdb.RelationshipType...)
     */
    public boolean hasRelationship(RelationshipType... types) {
        return node().getRelationships(types).iterator().hasNext();
    }

    /**
     * @see org.neo4j.graphdb.Node#hasRelationship(org.neo4j.graphdb.Direction, org.neo4j.graphdb.RelationshipType...)
     */
    public boolean hasRelationship(Direction direction, RelationshipType... types) {
        return node().getRelationships(direction, types).iterator().hasNext();
    }

    /**
     * @see org.neo4j.graphdb.Node#hasRelationship(org.neo4j.graphdb.Direction)
     */
    public boolean hasRelationship(Direction dir) {
        return node().getRelationships(dir).iterator().hasNext();
    }

    /**
     * @see org.neo4j.graphdb.Node#hasRelationship(org.neo4j.graphdb.RelationshipType, org.neo4j.graphdb.Direction)
     */
    public boolean hasRelationship(RelationshipType type, Direction dir) {
        return node().getRelationships(type, dir).iterator().hasNext();
    }

    /**
     * @see org.neo4j.graphdb.Relationship#isType(org.neo4j.graphdb.RelationshipType).
     */
    public boolean isType(RelationshipType type) {
        return relationship().getType().name().equals(type.name());
    }

    /**
     * @return self as node.
     * @throws IllegalStateException if this instance isn't a {@link org.neo4j.graphdb.Node}.
     */
    protected Node node() {
        if (!(this instanceof Node)) {
            throw new IllegalStateException("Not a node, this is a bug");
        }

        return (Node) this;
    }

    /**
     * @return self as relationship.
     * @throws IllegalStateException if this instance isn't a {@link org.neo4j.graphdb.Relationship}.
     */
    protected Relationship relationship() {
        if (!(this instanceof Relationship)) {
            throw new IllegalStateException("Not a relationship, this is a bug");
        }

        return (Relationship) this;
    }
}
