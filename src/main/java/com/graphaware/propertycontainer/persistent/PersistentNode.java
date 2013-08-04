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

package com.graphaware.propertycontainer.persistent;

import org.neo4j.graphdb.*;

/**
 * {@link Persistent} {@link org.neo4j.graphdb.Node}.
 * <p/>
 * Experimental, will probably go away / be changed
 */
public class PersistentNode extends PersistentPropertyContainer<Node> implements Node {

    /**
     * Construct a detached {@link org.neo4j.graphdb.Node} that has never been persisted.
     */
    public PersistentNode() {
        super();
    }

    /**
     * Construct a detached {@link org.neo4j.graphdb.Node}, which has previously been persisted.
     *
     * @param id ID of the node in the database.
     */
    public PersistentNode(long id) {
        super(id);
    }

    /**
     * Construct a persisted {@link org.neo4j.graphdb.Node}.
     *
     * @param realNode the real {@link org.neo4j.graphdb.Node} that this represents.
     */
    public PersistentNode(Node realNode) {
        super(realNode);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected long doGetId() {
        return realPropertyContainer.getId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doDetach() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doMerge(GraphDatabaseService database) {
        try {
            realPropertyContainer = database.getNodeById(getId());
        } catch (NotFoundException e) {
            realPropertyContainer = database.createNode();
        }
    }

    //Operations delegated to the real node and unsupported on detached nodes:

    /**
     * {@inheritDoc}
     *
     * @throws IllegalStateException iff not persisted.
     */
    @Override
    public void delete() {
        persistedOrException();
        realPropertyContainer.delete();
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalStateException iff not persisted.
     */
    @Override
    public Iterable<Relationship> getRelationships() {
        persistedOrException();
        return realPropertyContainer.getRelationships();
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalStateException iff not persisted.
     */
    @Override
    public boolean hasRelationship() {
        persistedOrException();
        return realPropertyContainer.hasRelationship();
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalStateException iff not persisted.
     */
    @Override
    public Iterable<Relationship> getRelationships(RelationshipType... types) {
        persistedOrException();
        return realPropertyContainer.getRelationships(types);
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalStateException iff not persisted.
     */
    @Override
    public Iterable<Relationship> getRelationships(Direction direction, RelationshipType... types) {
        persistedOrException();
        return realPropertyContainer.getRelationships(direction, types);
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalStateException iff not persisted.
     */
    @Override
    public boolean hasRelationship(RelationshipType... types) {
        persistedOrException();
        return realPropertyContainer.hasRelationship(types);
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalStateException iff not persisted.
     */
    @Override
    public boolean hasRelationship(Direction direction, RelationshipType... types) {
        persistedOrException();
        return realPropertyContainer.hasRelationship(direction, types);
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalStateException iff not persisted.
     */
    @Override
    public Iterable<Relationship> getRelationships(Direction dir) {
        persistedOrException();
        return realPropertyContainer.getRelationships(dir);
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalStateException iff not persisted.
     */
    @Override
    public boolean hasRelationship(Direction dir) {
        persistedOrException();
        return realPropertyContainer.hasRelationship(dir);
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalStateException iff not persisted.
     */
    @Override
    public Iterable<Relationship> getRelationships(RelationshipType type, Direction dir) {
        persistedOrException();
        return realPropertyContainer.getRelationships(type, dir);
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalStateException iff not persisted.
     */
    @Override
    public boolean hasRelationship(RelationshipType type, Direction dir) {
        persistedOrException();
        return realPropertyContainer.hasRelationship(type, dir);
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalStateException iff not persisted.
     */
    @Override
    public Relationship getSingleRelationship(RelationshipType type, Direction dir) {
        persistedOrException();
        return realPropertyContainer.getSingleRelationship(type, dir);
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalStateException iff not persisted.
     */
    @Override
    public Relationship createRelationshipTo(Node otherNode, RelationshipType type) {
        persistedOrException();
        return realPropertyContainer.createRelationshipTo(otherNode, type);
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalStateException iff not persisted.
     */
    @SuppressWarnings("deprecation")
    @Override
    public Traverser traverse(Traverser.Order traversalOrder, StopEvaluator stopEvaluator, ReturnableEvaluator returnableEvaluator, RelationshipType relationshipType, Direction direction) {
        persistedOrException();
        return realPropertyContainer.traverse(traversalOrder, stopEvaluator, returnableEvaluator, relationshipType, direction);
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalStateException iff not persisted.
     */
    @SuppressWarnings("deprecation")
    @Override
    public Traverser traverse(Traverser.Order traversalOrder, StopEvaluator stopEvaluator, ReturnableEvaluator returnableEvaluator, RelationshipType firstRelationshipType, Direction firstDirection, RelationshipType secondRelationshipType, Direction secondDirection) {
        persistedOrException();
        return realPropertyContainer.traverse(traversalOrder, stopEvaluator, returnableEvaluator, firstRelationshipType, firstDirection, secondRelationshipType, secondDirection);
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalStateException iff not persisted.
     */
    @SuppressWarnings("deprecation")
    @Override
    public Traverser traverse(Traverser.Order traversalOrder, StopEvaluator stopEvaluator, ReturnableEvaluator returnableEvaluator, Object... relationshipTypesAndDirections) {
        persistedOrException();
        return realPropertyContainer.traverse(traversalOrder, stopEvaluator, returnableEvaluator, relationshipTypesAndDirections);
    }
}
