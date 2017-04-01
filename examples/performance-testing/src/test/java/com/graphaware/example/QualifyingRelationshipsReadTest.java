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

package com.graphaware.example;

import com.graphaware.test.performance.*;
import com.graphaware.tx.executor.NullItem;
import com.graphaware.tx.executor.batch.BatchTransactionExecutor;
import com.graphaware.tx.executor.batch.NoInputBatchTransactionExecutor;
import com.graphaware.tx.executor.batch.UnitOfWork;
import org.neo4j.graphdb.*;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.neo4j.graphdb.Direction.INCOMING;
import static org.neo4j.graphdb.Direction.OUTGOING;

/**
 * A {@link PerformanceTest} to find the performance implications of using properties vs relationship types
 * to qualify relationships, when reading from the database.
 */
public abstract class QualifyingRelationshipsReadTest implements PerformanceTest {

    //parameter names
    protected static final String TYPE_OR_PROPERTY = "top";
    protected static final String CACHE = "cache";

    protected static final String RATING = "rating";
    protected static final String TYPE = "TYPE";

    protected static final int NO_NODES = 100;
    protected static final int NO_RELATIONSHIPS = 5000;
    protected static final int NO_TYPES = 5;

    protected static final int OPS_PER_TRIAL = 100;
    protected static final int TRIALS = 100;

    protected static final RelationshipType RATED = RelationshipType.withName("RATED");

    protected static final RelationshipType[] REL_TYPES;

    static {
        List<RelationshipType> types = new LinkedList<>();
        for (int i = 0; i < NO_TYPES; i++) {
            types.add(RelationshipType.withName(TYPE + i));
        }
        REL_TYPES = types.toArray(new RelationshipType[NO_TYPES]);
    }

    protected RelationshipQualifier lastRelationshipQualifier;

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Parameter> parameters() {
        List<Parameter> result = new LinkedList<>();

        result.add(new CacheParameter(CACHE));
        result.add(new EnumParameter(TYPE_OR_PROPERTY, RelationshipQualifier.class));

        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int dryRuns(Map<String, Object> params) {
        return ((CacheConfiguration) params.get(CACHE)).needsWarmup() ? 10000 : 100;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int measuredRuns() {
        return TRIALS;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, String> databaseParameters(Map<String, Object> params) {
        return ((CacheConfiguration) params.get(CACHE)).addToConfig(Collections.<String, String>emptyMap());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void prepare(GraphDatabaseService database, final Map<String, Object> params) {
        new NoInputBatchTransactionExecutor(database, 1000, NO_NODES, new UnitOfWork<NullItem>() {
            @Override
            public void execute(GraphDatabaseService database, NullItem input, int batchNumber, int stepNumber) {
                database.createNode();
            }
        }).execute();

        final BatchTransactionExecutor executor = new NoInputBatchTransactionExecutor(database, 1000, NO_RELATIONSHIPS, new UnitOfWork<NullItem>() {
            @Override
            public void execute(GraphDatabaseService database, NullItem input, int batchNumber, int stepNumber) {
                final Node node1 = randomNode(database, NO_NODES);
                final Node node2 = randomNode(database, NO_NODES);

                int randomInt = RANDOM.nextInt(NO_TYPES);

                switch ((RelationshipQualifier) params.get(TYPE_OR_PROPERTY)) {
                    case PROPERTY:
                        Relationship relationship = node1.createRelationshipTo(node2, RATED);
                        relationship.setProperty(RATING, randomInt);
                        break;
                    case RELATIONSHIP_TYPE:
                        node1.createRelationshipTo(node2, REL_TYPES[randomInt]);
                        break;
                }
            }
        });

        executor.execute();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RebuildDatabase rebuildDatabase() {
        return RebuildDatabase.TEST_DECIDES;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean rebuildDatabase(Map<String, Object> params) {
        RelationshipQualifier typeOrProperty = (RelationshipQualifier) params.get(TYPE_OR_PROPERTY);
        boolean result = !typeOrProperty.equals(lastRelationshipQualifier);
        lastRelationshipQualifier = typeOrProperty;
        return result;
    }

    /**
     * Get a random node from the database.
     *
     * @param database to get the node from.
     * @param noNodes  total number of nodes.
     * @return a random node.
     */
    protected Node randomNode(GraphDatabaseService database, int noNodes) {
        return database.getNodeById(RANDOM.nextInt(noNodes));
    }

    /**
     * Get a random direction.
     *
     * @return random direction.
     */
    protected Direction randomDirection() {
        return RANDOM.nextBoolean() ? INCOMING : OUTGOING;
    }
}
