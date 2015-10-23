/*
 * Copyright (c) 2015 GraphAware
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

package com.graphaware.common.representation;

import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.springframework.util.Assert;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.springframework.util.Assert.*;

/**
 * {@link PropertyContainerRepresentation} for a {@link Node}.
 */
public class NodeRepresentation extends PropertyContainerRepresentation<Node> {

    private String[] labels;

    /**
     * Public no-arg constructor (for Jackson et al).
     */
    public NodeRepresentation() {
    }

    /**
     * Construct a representation from a Neo4j node.
     *
     * @param node node to create the representation from. Must not be <code>null</code>.
     */
    public NodeRepresentation(Node node) {
        super(node, null);
        setLabels(labelsToStringArray(node.getLabels()));
    }

    /**
     * Construct a representation from a Neo4j node.
     *
     * @param node       node to create the representation from. Must not be <code>null</code>.
     * @param properties keys of properties to be included in the representation.
     *                   Can be <code>null</code>, which represents all. Empty array represents none.
     */
    public NodeRepresentation(Node node, String[] properties) {
        super(node, properties);
        setLabels(labelsToStringArray(node.getLabels()));
    }

    /**
     * Construct a representation from a Neo4j node ID.
     *
     * @param id of a node to create the representation from.
     */
    public NodeRepresentation(long id) {
        super(id);
    }

    /**
     * Construct a representation from an array of labels and a map of properties.
     *
     * @param labels     of the new node. Must not be <code>null</code>, but can be empty.
     * @param properties of the new node. Can be <code>null</code>, which is equivalent to an empty map.
     */
    public NodeRepresentation(String[] labels, Map<String, Object> properties) {
        super(properties);

        notNull(labels);
        this.labels = labels;
    }

    /**
     * Construct a representation from a Neo4j node ID, an array of labels, and a map of properties.
     * <p/>
     * Note that this constructor is only intended for testing.
     *
     * @param id         ID.
     * @param labels     of the new node. Must not be <code>null</code>, but can be empty.
     * @param properties of the new node. Can be <code>null</code>, which is equivalent to an empty map.
     */
    public NodeRepresentation(long id, String[] labels, Map<String, Object> properties) {
        super(id, properties);

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
        return database.getNodeById(getId());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void populate(Node node) {
        super.populate(node);

        if (labels != null) {
            for (String label : labels) {
                node.addLabel(DynamicLabel.label(label));
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

    private String[] labelsToStringArray(Iterable<Label> labels) {
        List<String> labelsAsList = new LinkedList<>();
        for (Label label : labels) {
            labelsAsList.add(label.name());
        }
        return labelsAsList.toArray(new String[labelsAsList.size()]);
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

        NodeRepresentation that = (NodeRepresentation) o;

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
