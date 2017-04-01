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

package com.graphaware.common.policy.inclusion;

import com.graphaware.common.policy.inclusion.all.IncludeAllNodeProperties;
import com.graphaware.common.policy.inclusion.all.IncludeAllNodes;
import com.graphaware.common.policy.inclusion.all.IncludeAllRelationshipProperties;
import com.graphaware.common.policy.inclusion.all.IncludeAllRelationships;
import com.graphaware.common.policy.inclusion.none.IncludeNoNodeProperties;
import com.graphaware.common.policy.inclusion.none.IncludeNoNodes;
import com.graphaware.common.policy.inclusion.none.IncludeNoRelationshipProperties;
import com.graphaware.common.policy.inclusion.none.IncludeNoRelationships;

/**
 * Wrapper for {@link InclusionPolicy}s.
 */
public class InclusionPolicies {

    private final NodeInclusionPolicy nodeInclusionPolicy;
    private final NodePropertyInclusionPolicy nodePropertyInclusionPolicy;
    private final RelationshipInclusionPolicy relationshipInclusionPolicy;
    private final RelationshipPropertyInclusionPolicy relationshipPropertyInclusionPolicy;

    /**
     * Create all-including policies.
     *
     * @return all-including policies.
     */
    public static InclusionPolicies all() {
        return new InclusionPolicies(
                IncludeAllNodes.getInstance(),
                IncludeAllNodeProperties.getInstance(),
                IncludeAllRelationships.getInstance(),
                IncludeAllRelationshipProperties.getInstance());
    }

    /**
     * Create nothing-including policies.
     *
     * @return nothing-including policies.
     */
    public static InclusionPolicies none() {
        return new InclusionPolicies(
                IncludeNoNodes.getInstance(),
                IncludeNoNodeProperties.getInstance(),
                IncludeNoRelationships.getInstance(),
                IncludeNoRelationshipProperties.getInstance());
    }

    /**
     * Constructor.
     *
     * @param nodeInclusionPolicy         policy.
     * @param nodePropertyInclusionPolicy policy.
     * @param relationshipInclusionPolicy policy.
     * @param relationshipPropertyInclusionPolicy
     *                                      policy.
     */
    public InclusionPolicies(NodeInclusionPolicy nodeInclusionPolicy, NodePropertyInclusionPolicy nodePropertyInclusionPolicy, RelationshipInclusionPolicy relationshipInclusionPolicy, RelationshipPropertyInclusionPolicy relationshipPropertyInclusionPolicy) {
        if (nodeInclusionPolicy == null || nodePropertyInclusionPolicy == null || relationshipInclusionPolicy == null || relationshipPropertyInclusionPolicy == null) {
            throw new IllegalArgumentException("An inclusion policy must not be null");
        }

        this.nodeInclusionPolicy = nodeInclusionPolicy;
        this.nodePropertyInclusionPolicy = nodePropertyInclusionPolicy;
        this.relationshipInclusionPolicy = relationshipInclusionPolicy;
        this.relationshipPropertyInclusionPolicy = relationshipPropertyInclusionPolicy;
    }

    /**
     * Reconfigure this instance to use a custom node inclusion policy.
     *
     * @param nodeInclusionPolicy to use.
     * @return reconfigured policies.
     */
    public InclusionPolicies with(NodeInclusionPolicy nodeInclusionPolicy) {
        return new InclusionPolicies(nodeInclusionPolicy, getNodePropertyInclusionPolicy(), getRelationshipInclusionPolicy(), getRelationshipPropertyInclusionPolicy());
    }

    /**
     * Reconfigure this instance to use a custom node property inclusion policy.
     *
     * @param nodePropertyInclusionPolicy to use.
     * @return reconfigured policies.
     */
    public InclusionPolicies with(NodePropertyInclusionPolicy nodePropertyInclusionPolicy) {
        return new InclusionPolicies(getNodeInclusionPolicy(), nodePropertyInclusionPolicy, getRelationshipInclusionPolicy(), getRelationshipPropertyInclusionPolicy());
    }

    /**
     * Reconfigure this instance to use a custom relationship inclusion policy.
     *
     * @param relationshipInclusionPolicy to use.
     * @return reconfigured policies.
     */
    public InclusionPolicies with(RelationshipInclusionPolicy relationshipInclusionPolicy) {
        return new InclusionPolicies(getNodeInclusionPolicy(), getNodePropertyInclusionPolicy(), relationshipInclusionPolicy, getRelationshipPropertyInclusionPolicy());
    }

    /**
     * Reconfigure this instance to use a custom relationship property inclusion policy.
     *
     * @param relationshipPropertyInclusionPolicy
     *         to use.
     * @return reconfigured policies.
     */
    public InclusionPolicies with(RelationshipPropertyInclusionPolicy relationshipPropertyInclusionPolicy) {
        return new InclusionPolicies(getNodeInclusionPolicy(), getNodePropertyInclusionPolicy(), getRelationshipInclusionPolicy(), relationshipPropertyInclusionPolicy);
    }

    /**
     * @return contained node inclusion policy.
     */
    public NodeInclusionPolicy getNodeInclusionPolicy() {
        return nodeInclusionPolicy;
    }

    /**
     * @return contained node property inclusion policy.
     */
    public NodePropertyInclusionPolicy getNodePropertyInclusionPolicy() {
        return nodePropertyInclusionPolicy;
    }

    /**
     * @return contained relationship inclusion policy.
     */
    public RelationshipInclusionPolicy getRelationshipInclusionPolicy() {
        return relationshipInclusionPolicy;
    }

    /**
     * @return contained relationship property inclusion policy.
     */
    public RelationshipPropertyInclusionPolicy getRelationshipPropertyInclusionPolicy() {
        return relationshipPropertyInclusionPolicy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        InclusionPolicies that = (InclusionPolicies) o;

        if (!nodeInclusionPolicy.equals(that.nodeInclusionPolicy)) return false;
        if (!nodePropertyInclusionPolicy.equals(that.nodePropertyInclusionPolicy)) return false;
        if (!relationshipInclusionPolicy.equals(that.relationshipInclusionPolicy)) return false;
        if (!relationshipPropertyInclusionPolicy.equals(that.relationshipPropertyInclusionPolicy)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = nodeInclusionPolicy.hashCode();
        result = 31 * result + nodePropertyInclusionPolicy.hashCode();
        result = 31 * result + relationshipInclusionPolicy.hashCode();
        result = 31 * result + relationshipPropertyInclusionPolicy.hashCode();
        return result;
    }
}
