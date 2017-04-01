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

import com.graphaware.common.expression.DetachedNodeExpressions;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.helpers.collection.Iterables;

import java.util.Arrays;
import java.util.Map;

import static org.springframework.util.Assert.notNull;

/**
 * {@link DetachedPropertyContainer} for a {@link Node}.
 */
public abstract class DetachedNode<ID> extends DetachedPropertyContainer<ID, Node> implements DetachedNodeExpressions {

    private String[] labels;

    /**
     * Public no-arg constructor (for Jackson et al).
     */
    protected DetachedNode() {
    }

    /**
     * Construct a representation from a Neo4j node.
     *
     * @param node node to create the representation from. Must not be <code>null</code>.
     */
    protected DetachedNode(Node node) {
        this(node, null);
    }

    /**
     * Construct a representation from a Neo4j node.
     *
     * @param node       node to create the representation from. Must not be <code>null</code>.
     * @param properties keys of properties to be included in the representation.
     *                   Can be <code>null</code>, which represents all. Empty array represents none.
     */
    protected DetachedNode(Node node, String[] properties) {
        super(node, properties);
        setLabels(Iterables.asArray(String.class, Iterables.map(Label::name, node.getLabels())));
    }

    /**
     * Construct a representation from a Neo4j node ID.
     *
     * @param graphId of a node to create the representation from.
     */
    protected DetachedNode(long graphId) {
        super(graphId);
    }

    /**
     * Construct a representation from an array of labels and a map of properties.
     *
     * @param labels     of the new node. Must not be <code>null</code>, but can be empty.
     * @param properties of the new node. Can be <code>null</code>, which is equivalent to an empty map.
     */
    protected DetachedNode(String[] labels, Map<String, Object> properties) {
        super(properties);

        notNull(labels);
        this.labels = labels;
    }

    /**
     * Construct a representation from a Neo4j node ID, an array of labels, and a map of properties.
     * <p>
     * Note that this constructor is only intended for testing.
     *
     * @param graphId    ID.
     * @param labels     of the new node. Must not be <code>null</code>, but can be empty.
     * @param properties of the new node. Can be <code>null</code>, which is equivalent to an empty map.
     */
    protected DetachedNode(long graphId, String[] labels, Map<String, Object> properties) {
        super(graphId, properties);

        notNull(labels);
        this.labels = labels;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Node create(GraphDatabaseService database) {
        return database.createNode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Node fetch(GraphDatabaseService database) {
        return database.getNodeById(getGraphId());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void populate(Node node) {
        super.populate(node);

        if (labels != null) {
            for (String label : labels) {
                node.addLabel(Label.label(label));
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void checkCanFetch() {
        super.checkCanFetch();

        if (labels != null && labels.length != 0) {
            throw new IllegalStateException("Must not specify labels for existing node!");
        }
    }

    //getters and setters

    public String[] getLabels() {
        return labels;
    }

    public void setLabels(String[] labels) {
        notNull(labels);
        this.labels = labels;
    }

    //helpers

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

        DetachedNode that = (DetachedNode) o;

        return Arrays.equals(labels, that.labels);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + Arrays.hashCode(labels);
        return result;
    }
}
