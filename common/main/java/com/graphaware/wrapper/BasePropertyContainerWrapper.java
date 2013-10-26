/*
 * Copyright (c) 2013 GraphAware
 *
 * This file is part of GraphAware.
 *
 * GraphAware is free software: you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received a copy of
 * the GNU General Public License along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 */

package com.graphaware.wrapper;

import org.neo4j.graphdb.*;
import org.neo4j.helpers.collection.IterableWrapper;

import static org.neo4j.graphdb.Direction.BOTH;

/**
 * Base class for {@link PropertyContainerWrapper} implementations.
 */
public abstract class BasePropertyContainerWrapper<T extends PropertyContainer> extends BasePropertyContainer implements PropertyContainerWrapper<T> {

    /**
     * @return this.
     */
    protected abstract T self();

    //Typically overridden:

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasProperty(String key) {
        return getWrapped().hasProperty(key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getProperty(String key) {
        if (!hasProperty(key)) {
            throw new NotFoundException("Property " + key + " not present on " + self() + " or filtered out");
        }
        return getWrapped().getProperty(key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setProperty(String key, Object value) {
        getWrapped().setProperty(key, value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object removeProperty(String key) {
        return getWrapped().removeProperty(key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterable<String> getPropertyKeys() {
        return getWrapped().getPropertyKeys();
    }

    /**
     * @see {@link org.neo4j.graphdb.Node#getRelationships()}.
     */
    public Iterable<Relationship> getRelationships() {
        return getRelationships(BOTH);
    }

    /**
     * @see {@link org.neo4j.graphdb.Node#getRelationships(org.neo4j.graphdb.RelationshipType...)}.
     */
    public Iterable<Relationship> getRelationships(RelationshipType... types) {
        return getRelationships(BOTH, types);
    }

    /**
     * @see {@link org.neo4j.graphdb.Node#getRelationships(org.neo4j.graphdb.Direction)}.
     */
    public Iterable<Relationship> getRelationships(Direction dir) {
        return getRelationships(dir, new RelationshipType[0]);
    }

    /**
     * @see {@link org.neo4j.graphdb.Node#getRelationships(org.neo4j.graphdb.RelationshipType, org.neo4j.graphdb.Direction)}.
     */
    public Iterable<Relationship> getRelationships(RelationshipType type, Direction dir) {
        return getRelationships(dir, type);
    }

    //The following methods intentionally break object-orientation a bit to keep the rest of the codebase DRY:

    /**
     * @see {@link org.neo4j.graphdb.Node#getRelationships(org.neo4j.graphdb.Direction, org.neo4j.graphdb.RelationshipType...)}.
     */
    public Iterable<Relationship> getRelationships(Direction direction, RelationshipType... types) {
        if (!(getWrapped() instanceof Node)) {
            throw new IllegalStateException("Not a node, this is a bug");
        }

        if (types == null || types.length == 0) {
            return wrapRelationships(((Node) getWrapped()).getRelationships(direction), direction);
        }

        return wrapRelationships(((Node) getWrapped()).getRelationships(direction, types), direction, types);
    }

    /**
     * @see {@link org.neo4j.graphdb.Node#createRelationshipTo(org.neo4j.graphdb.Node, org.neo4j.graphdb.RelationshipType)}.
     */
    public Relationship createRelationshipTo(Node otherNode, RelationshipType type) {
        if (!(getWrapped() instanceof Node)) {
            throw new IllegalStateException("Not a node, this is a bug");
        }

        return wrapRelationship(((Node) getWrapped()).createRelationshipTo(otherNode, type));
    }

    /**
     * @see {@link org.neo4j.graphdb.Relationship#getType()}.
     */
    public RelationshipType getType() {
        if (!(getWrapped() instanceof Relationship)) {
            throw new IllegalStateException("Not a relationship, this is a bug");
        }

        return ((Relationship) getWrapped()).getType();
    }

    /**
     * @see {@link org.neo4j.graphdb.Relationship#getStartNode()}.
     */
    public Node getStartNode() {
        if (!(getWrapped() instanceof Relationship)) {
            throw new IllegalStateException("Not a relationship, this is a bug");
        }

        return wrapNode(((Relationship) getWrapped()).getStartNode());
    }

    /**
     * @see {@link org.neo4j.graphdb.Relationship#getEndNode()}.
     */
    public Node getEndNode() {
        if (!(getWrapped() instanceof Relationship)) {
            throw new IllegalStateException("Not a relationship, this is a bug");
        }

        return wrapNode(((Relationship) getWrapped()).getEndNode());
    }

    /**
     * Allow subclasses to wrap a node. By default, no wrapping is performed. This method is called every time a node
     * is about to be returned.
     *
     * @param node to wrap.
     * @return wrapped node.
     */
    protected Node wrapNode(Node node) {
        return node;
    }

    /**
     * Allow subclasses to wrap a relationship. By default, no wrapping is performed. This method is called every time
     * a relationship is about to be returned.
     *
     * @param relationship to wrap.
     * @return wrapped relationship.
     */
    protected Relationship wrapRelationship(Relationship relationship) {
        return relationship;
    }

    /**
     * Allow subclasses to wrap an iterable relationship. By default, {@link #wrapRelationship(org.neo4j.graphdb.Relationship)}
     * is called for each relationship. This method is called every time a relationship iterable is about to be returned.
     *
     * @param relationships     to wrap.
     * @param direction         of that the returned relationships should all have (for cases where the subclass wants
     *                          to add additional relationships).
     * @param relationshipTypes one of which the returned relationships should all have (for cases where the subclass
     *                          wants to add additional relationships). Empty array means "any".
     * @return wrapped relationship.
     */
    protected Iterable<Relationship> wrapRelationships(Iterable<Relationship> relationships, Direction direction, RelationshipType... relationshipTypes) {
        return new IterableWrapper<Relationship, Relationship>(relationships) {
            @Override
            protected Relationship underlyingObjectToObject(Relationship object) {
                return wrapRelationship(object);
            }
        };
    }

    //Typically no need to override:

    /**
     * @see {@link org.neo4j.graphdb.Node#getId()}  and {@link org.neo4j.graphdb.Relationship#getId()}.
     */
    public long getId() {
        if (getWrapped() instanceof Node) {
            return ((Node) getWrapped()).getId();
        }

        if (getWrapped() instanceof Relationship) {
            return ((Relationship) getWrapped()).getId();
        }

        throw new IllegalStateException(this + " is not a Node or Relationship");
    }

    /**
     * @see {@link org.neo4j.graphdb.Node#delete()}  and {@link org.neo4j.graphdb.Relationship#delete()}.
     */
    public void delete() {
        if (getWrapped() instanceof Node) {
            ((Node) getWrapped()).delete();
            return;
        }

        if (getWrapped() instanceof Relationship) {
            ((Relationship) getWrapped()).delete();
            return;
        }

        throw new IllegalStateException(this + " is not a Node or Relationship");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphDatabaseService getGraphDatabase() {
        return getWrapped().getGraphDatabase();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BasePropertyContainerWrapper that = (BasePropertyContainerWrapper) o;

        if (!getWrapped().equals(that.getWrapped())) return false;

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return getWrapped().hashCode();
    }
}
