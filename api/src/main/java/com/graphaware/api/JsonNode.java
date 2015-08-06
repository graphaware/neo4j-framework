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

package com.graphaware.api;

import org.neo4j.graphdb.*;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * JSON-serializable representation of a Neo4j node.
 */
public class JsonNode extends JsonPropertyContainer<Node> {

    private String[] labels;

    /**
     * Public no-arg constructor (for Jackson)
     */
    public JsonNode() {
    }

    /**
     * Create a JSON-serializable representation from a Neo4j node.
     *
     * @param node node to create JSON from.
     */
    public JsonNode(Node node) {
        this(node, new JsonInput());
    }

    /**
     * Create a JSON-serializable representation from a Neo4j node.
     *
     * @param node      node to create JSON from.
     * @param jsonInput specifying what to include in the produced JSON.
     */
    public JsonNode(Node node, JsonInput jsonInput) {
        super(node, jsonInput.getNodeProperties());
        setLabels(labelsToStringArray(node.getLabels()));
    }

    /**
     * Create a JSON-serializable representation from a Neo4j node ID.
     *
     * @param id of a node to create JSON from.
     */
    public JsonNode(long id) {
        super(id);
    }

    /**
     * Construct a new representation of a node.
     *
     * @param labels of the new node.
     * @param properties of the new node.
     */
    public JsonNode(String[] labels, Map<String, Object> properties) {
        super(properties);
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
}
