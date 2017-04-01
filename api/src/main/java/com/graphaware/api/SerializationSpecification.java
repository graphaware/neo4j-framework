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

package com.graphaware.api;

/**
 * Specification of what to include an API output.
 */
public class SerializationSpecification {

    private String[] nodeProperties;
    private String[] relationshipProperties;

    /**
     * Construct a new spec: Return all node and relationship properties.
     */
    public SerializationSpecification() {
    }

    /**
     * Construct a new spec.
     *
     * @param nodeProperties         keys of node properties to be included in the output. If the property doesn't exist for a node, nothing
     *                               happens (i.e. it will not appear in the output in any form). <code>null</code> means all,
     *                               empty array means none.
     * @param relationshipProperties keys of relationship properties to be included in the output. If the property doesn't exist for
     *                               a relationship, nothing happens (i.e. it will not appear in the output in any form).
     *                               <code>null</code> means all, empty array means none.
     */
    public SerializationSpecification(String[] nodeProperties, String[] relationshipProperties) {
        this.nodeProperties = nodeProperties;
        this.relationshipProperties = relationshipProperties;
    }

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
     *                       happens (i.e. it will not appear in the output in any form). <code>null</code> means all,
     *                       empty array means none.
     */
    public void setNodeProperties(String[] nodeProperties) {
        this.nodeProperties = nodeProperties;
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
     *                               <code>null</code> means all, empty array means none.
     */
    public void setRelationshipProperties(String[] relationshipProperties) {
        this.relationshipProperties = relationshipProperties;
    }
}
