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

import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;

import java.util.LinkedList;
import java.util.List;

/**
 * JSON-serializable representation of a Neo4j node.
 */
public class JsonNode extends JsonPropertyContainer {

    private String[] labels;

    public JsonNode(Node node, JsonInput jsonInput) {
        super(node.getId());

        if (jsonInput.getNodeProperties() != null) {
            for (String property : jsonInput.getNodeProperties()) {
                if (node.hasProperty(property)) {
                    putProperty(property, node.getProperty(property));
                }
            }
        }

        if (Boolean.TRUE.equals(jsonInput.getIncludeNodeLabels())) {
            setLabels(labelsToStringArray(node.getLabels()));
        }
    }

    private String[] labelsToStringArray(Iterable<Label> labels) {
        List<String> labelsAsList = new LinkedList<>();
        for (Label label : labels) {
            labelsAsList.add(label.name());
        }
        return labelsAsList.toArray(new String[labelsAsList.size()]);
    }

    public String[] getLabels() {
        return labels;
    }

    public void setLabels(String[] labels) {
        this.labels = labels;
    }
}
