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

package com.graphaware.relcount.count;

import com.graphaware.common.description.property.LazyPropertiesDescription;
import com.graphaware.common.description.property.PropertiesDescription;
import com.graphaware.common.description.relationship.RelationshipDescription;
import com.graphaware.relcount.module.RelationshipCountStrategies;
import com.graphaware.relcount.module.RelationshipCountStrategiesImpl;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import static org.neo4j.graphdb.Direction.BOTH;

/**
 * A naive {@link RelationshipCounter} that counts matching relationships by inspecting all {@link org.neo4j.graphdb.Node}'s {@link org.neo4j.graphdb.Relationship}s.
 * <p/>
 * Because relationships are counted on the fly (no caching performed), this can be used without the
 * {@link com.graphaware.runtime.GraphAwareRuntime} and/or any {@link com.graphaware.runtime.GraphAwareRuntimeModule}s.
 * <p/>
 * This counter always returns a count, never throws {@link UnableToCountException}.
 */
public class NaiveRelationshipCounter implements RelationshipCounter {

    private final RelationshipCountStrategies relationshipCountStrategies;

    /**
     * Construct a new relationship counter with default strategies.
     */
    public NaiveRelationshipCounter() {
        this(RelationshipCountStrategiesImpl.defaultStrategies());
    }

    /**
     * Construct a new relationship counter. Use when custom {@link com.graphaware.relcount.module.RelationshipCountStrategies} have been used for the
     * {@link com.graphaware.relcount.module.RelationshipCountRuntimeModule}. Alternatively, it might be easier
     * use {@link com.graphaware.relcount.module.RelationshipCountRuntimeModule#naiveCounter()}.
     *
     * @param relationshipCountStrategies strategies, of which only {@link WeighingStrategy} is used.
     */
    public NaiveRelationshipCounter(RelationshipCountStrategies relationshipCountStrategies) {
        this.relationshipCountStrategies = relationshipCountStrategies;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int count(Node node, RelationshipDescription description) {
        int result = 0;

        for (Relationship candidateRelationship : node.getRelationships(description.getDirection(), description.getType())) {
            PropertiesDescription candidate = new LazyPropertiesDescription(candidateRelationship);

            if (candidate.isMoreSpecificThan(description.getPropertiesDescription())) {
                int relationshipWeight = relationshipCountStrategies.getWeighingStrategy().getRelationshipWeight(candidateRelationship, node);
                result = result + relationshipWeight;

                //double count loops if looking for BOTH
                if (BOTH.equals(description.getDirection()) && candidateRelationship.getStartNode().getId() == candidateRelationship.getEndNode().getId()) {
                    result = result + relationshipWeight;
                }
            }
        }

        return result;
    }
}
