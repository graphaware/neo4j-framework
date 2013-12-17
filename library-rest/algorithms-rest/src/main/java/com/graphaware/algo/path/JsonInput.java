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

package com.graphaware.algo.path;

/**
 *
 */
public class JsonInput {

    private String[] nodeProperties;
    private Boolean includeNodeLabels;
    private String[] relationshipProperties;

    public String[] getNodeProperties() {
        return nodeProperties;
    }

    public void setNodeProperties(String[] nodeProperties) {
        this.nodeProperties = nodeProperties;
    }

    public Boolean getIncludeNodeLabels() {
        return includeNodeLabels;
    }

    public void setIncludeNodeLabels(Boolean includeNodeLabels) {
        this.includeNodeLabels = includeNodeLabels;
    }

    public String[] getRelationshipProperties() {
        return relationshipProperties;
    }

    public void setRelationshipProperties(String[] relationshipProperties) {
        this.relationshipProperties = relationshipProperties;
    }
}
