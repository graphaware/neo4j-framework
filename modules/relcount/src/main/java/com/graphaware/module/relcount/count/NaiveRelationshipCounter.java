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

package com.graphaware.module.relcount.count;

import com.graphaware.common.description.property.LazyPropertiesDescription;
import com.graphaware.common.description.property.PropertiesDescription;
import com.graphaware.common.description.relationship.RelationshipDescription;
import com.graphaware.common.description.serialize.Serializer;
import com.graphaware.module.relcount.RelationshipCountConfiguration;
import com.graphaware.module.relcount.RelationshipCountConfigurationImpl;
import com.graphaware.module.relcount.RelationshipCountRuntimeModule;
import com.graphaware.runtime.BaseGraphAwareRuntime;
import com.graphaware.runtime.config.DefaultRuntimeConfiguration;
import com.graphaware.runtime.config.RuntimeConfiguration;
import org.apache.log4j.Logger;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.tooling.GlobalGraphOperations;

import static com.graphaware.runtime.BaseGraphAwareRuntime.CONFIG;
import static com.graphaware.runtime.BaseGraphAwareRuntime.RUNTIME;
import static com.graphaware.runtime.ProductionGraphAwareRuntime.getOrCreateRoot;
import static org.neo4j.graphdb.Direction.BOTH;

/**
 * A naive {@link RelationshipCounter} that counts matching relationships by inspecting all {@link org.neo4j.graphdb.Node}'s {@link org.neo4j.graphdb.Relationship}s.
 * <p/>
 * Because relationships are counted on the fly (no caching performed), this can be used without the
 * {@link com.graphaware.runtime.ProductionGraphAwareRuntime} and/or any {@link com.graphaware.runtime.GraphAwareRuntimeModule}s.
 * <p/>
 * This counter always returns a count, never throws {@link UnableToCountException}.
 */
public class NaiveRelationshipCounter implements RelationshipCounter {

    private static final Logger LOG = Logger.getLogger(NaiveRelationshipCounter.class);

    private final RelationshipCountConfiguration relationshipCountConfiguration;

    /**
     * Construct a new relationship counter with default strategies.
     */
    public NaiveRelationshipCounter(GraphDatabaseService database) {
        this(database, RelationshipCountRuntimeModule.FULL_RELCOUNT_DEFAULT_ID);
    }

    public NaiveRelationshipCounter(GraphDatabaseService database, String id) {
        this(database, id, OneForEach.getInstance());
    }

    public NaiveRelationshipCounter(GraphDatabaseService database,WeighingStrategy weighingStrategy) {
        this(database, RelationshipCountRuntimeModule.FULL_RELCOUNT_DEFAULT_ID, weighingStrategy);
    }

    /**
     * Construct a new relationship counter. Use when custom {@link com.graphaware.module.relcount.RelationshipCountConfiguration} have been used for the
     * {@link com.graphaware.module.relcount.RelationshipCountRuntimeModule}. Alternatively, it might be easier
     * use {@link com.graphaware.module.relcount.RelationshipCountRuntimeModule#naiveCounter()}.
     *
     * @param relationshipCountConfiguration strategies, of which only {@link WeighingStrategy} is used.
     */
    protected NaiveRelationshipCounter(GraphDatabaseService database, String id, WeighingStrategy weighingStrategy) {
        try (Transaction tx = database.beginTx()) {
            if (!GlobalGraphOperations.at(database).getAllNodesWithLabel(RuntimeConfiguration.GA_ROOT).iterator().hasNext()) {
                this.relationshipCountConfiguration = RelationshipCountConfigurationImpl.defaultConfiguration().with(weighingStrategy);
            } else {
                String key = DefaultRuntimeConfiguration.getInstance().createPrefix(RUNTIME) + id;
                Node root = getOrCreateRoot(database);
                if (!root.hasProperty(key)) {
                    this.relationshipCountConfiguration = RelationshipCountConfigurationImpl.defaultConfiguration().with(weighingStrategy);
                } else {
                    String string = getOrCreateRoot(database).getProperty(key).toString();
                    this.relationshipCountConfiguration = Serializer.fromString(string, RelationshipCountConfigurationImpl.class, CONFIG);
                }
            }
            tx.success();
        }
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
                int relationshipWeight = relationshipCountConfiguration.getWeighingStrategy().getRelationshipWeight(candidateRelationship, node);
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
