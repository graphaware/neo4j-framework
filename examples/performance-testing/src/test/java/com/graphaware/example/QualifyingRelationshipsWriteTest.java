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

import com.graphaware.test.performance.EnumParameter;
import com.graphaware.test.performance.ExponentialParameter;
import com.graphaware.test.performance.Parameter;
import com.graphaware.test.performance.PerformanceTest;
import com.graphaware.test.util.TestUtils;
import com.graphaware.tx.executor.NullItem;
import com.graphaware.tx.executor.batch.BatchTransactionExecutor;
import com.graphaware.tx.executor.batch.NoInputBatchTransactionExecutor;
import com.graphaware.tx.executor.batch.UnitOfWork;
import org.neo4j.graphdb.*;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * A {@link PerformanceTest} to find the performance implications of using properties vs relationship types
 * to qualify relationships, when writing to the database.
 */
public class QualifyingRelationshipsWriteTest implements PerformanceTest {

    //parameter names
    private static final String BATCH_SIZE = "batchSize";
    private static final String TYPE_OR_PROPERTY = "top";

    private static final int NO_NODES = 100;
    private static final int NO_RELATIONSHIPS = 1000;

    private static final RelationshipType RATED = RelationshipType.withName("RATED");
    private static final RelationshipType[] TYPES = new RelationshipType[]{
            RelationshipType.withName("LOVES"),
            RelationshipType.withName("LIKES"),
            RelationshipType.withName("NEUTRAL"),
            RelationshipType.withName("DISLIKES"),
            RelationshipType.withName("HATES")
    };

    /**
     * {@inheritDoc}
     */
    @Override
    public String shortName() {
        return "qualifying-relationships-write";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String longName() {
        return "Qualifying Relationships (Write Throughput)";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Parameter> parameters() {
        List<Parameter> result = new LinkedList<>();

        result.add(new EnumParameter(TYPE_OR_PROPERTY, RelationshipQualifier.class));
        result.add(new ExponentialParameter(BATCH_SIZE, 10, 0, 3, 0.25));

        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int dryRuns(Map<String, Object> params) {
        return 20;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int measuredRuns() {
        return 100;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, String> databaseParameters(Map<String, Object> params) {
        return Collections.emptyMap();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void prepare(GraphDatabaseService database, Map<String, Object> params) {
        new NoInputBatchTransactionExecutor(database, 1000, NO_NODES, new UnitOfWork<NullItem>() {
            @Override
            public void execute(GraphDatabaseService database, NullItem input, int batchNumber, int stepNumber) {
                database.createNode();
            }
        }).execute();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long run(GraphDatabaseService database, final Map<String, Object> params) {
        final BatchTransactionExecutor executor = new NoInputBatchTransactionExecutor(database, (int) params.get(BATCH_SIZE), NO_RELATIONSHIPS, new UnitOfWork<NullItem>() {
            @Override
            public void execute(GraphDatabaseService database, NullItem input, int batchNumber, int stepNumber) {
                final Node node1 = randomNode(database, NO_NODES);
                final Node node2 = randomNode(database, NO_NODES);

                int randomInt = RANDOM.nextInt(5);

                switch ((RelationshipQualifier) params.get(TYPE_OR_PROPERTY)) {
                    case PROPERTY:
                        Relationship relationship = node1.createRelationshipTo(node2, RATED);
                        relationship.setProperty("rating", randomInt);
                        break;
                    case RELATIONSHIP_TYPE:
                        node1.createRelationshipTo(node2, TYPES[randomInt]);
                        break;
                }
            }
        });

        return TestUtils.time(new TestUtils.Timed() {
            @Override
            public void time() {
                executor.execute();
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RebuildDatabase rebuildDatabase() {
        return RebuildDatabase.AFTER_EVERY_RUN;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean rebuildDatabase(Map<String, Object> params) {
        throw new UnsupportedOperationException("This should not be called, since database rebuilt after every run");
    }

    /**
     * Get a random node from the database.
     *
     * @param database to get the node from.
     * @param noNodes  total number of nodes.
     * @return a random node.
     */
    private Node randomNode(GraphDatabaseService database, int noNodes) {
        return database.getNodeById(RANDOM.nextInt(noNodes));
    }
}
