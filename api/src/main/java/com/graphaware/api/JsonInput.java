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

/**
 * Base-class for JSON-serializable API inputs. Specifies what to include in the output.
 */
public abstract class JsonInput {

    private String[] nodeProperties;
    private Boolean includeNodeLabels;
    private String[] relationshipProperties;

    /**
     * Get the node properties to be included in the output.
     *
     * @return keys of node properties to be included. If the property doesn't exist for a node, nothing happens (i.e.
     * it will not appear in the output in any form).
     */
    public String[] getNodeProperties() {
        return nodeProperties;
    }

    /**
     * Set the node properties to be included in the output.
     *
     * @param nodeProperties keys of node properties to be included. If the property doesn't exist for a node, nothing
     *                       happens (i.e. it will not appear in the output in any form).
     */
    public void setNodeProperties(String[] nodeProperties) {
        this.nodeProperties = nodeProperties;
    }

    /**
     * Find out whether node labels should be included in the output.
     *
     * @return true iff node labels should be included.
     */
    public Boolean getIncludeNodeLabels() {
        return includeNodeLabels;
    }

    /**
     * Specify whether node labels should be included in the output.
     *
     * @param includeNodeLabels true iff node labels should be included, false or null otherwise.
     */
    public void setIncludeNodeLabels(Boolean includeNodeLabels) {
        this.includeNodeLabels = includeNodeLabels;
    }

    /**
     * Get the relationship properties to be included in the output.
     *
     * @return keys of relationship properties to be included. If the property doesn't exist for a relationship, nothing
     * happens (i.e. it will not appear in the output in any form).
     */
    public String[] getRelationshipProperties() {
        return relationshipProperties;
    }

    /**
     * Set the relationship properties to be included in the output.
     *
     * @param relationshipProperties keys of relationship properties to be included. If the property doesn't exist for
     *                               a relationship, nothing happens (i.e. it will not appear in the output in any form).
     */
    public void setRelationshipProperties(String[] relationshipProperties) {
        this.relationshipProperties = relationshipProperties;
    }
}
