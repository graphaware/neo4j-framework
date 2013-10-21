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

package com.graphaware.blog;

import com.graphaware.performance.EnumParameter;
import com.graphaware.performance.ExponentialParameter;
import com.graphaware.performance.Parameter;
import com.graphaware.performance.PerformanceTest;
import com.graphaware.test.TestUtils;
import com.graphaware.tx.executor.NullItem;
import com.graphaware.tx.executor.batch.BatchTransactionExecutor;
import com.graphaware.tx.executor.batch.NoInputBatchTransactionExecutor;
import com.graphaware.tx.executor.batch.UnitOfWork;
import org.neo4j.graphdb.*;

import java.util.*;

import static org.neo4j.graphdb.Direction.INCOMING;
import static org.neo4j.graphdb.Direction.OUTGOING;
import static org.neo4j.graphdb.DynamicRelationshipType.withName;

/**
 *
 */
public class RelationshipTypeVsPropertyWriteTest implements PerformanceTest {

    protected static final Random RANDOM = new Random(System.currentTimeMillis());

    private static final String BATCH_SIZE = "batchSize";
    private static final String TYPE_OR_PROPERTY = "top";

    private static final int NO_NODES = 100;
    private static final int NO_RELATIONSHIPS = 1000;

    private static final RelationshipType RATED = DynamicRelationshipType.withName("RATED");
    private static final RelationshipType[] TYPES = new RelationshipType[]{
            DynamicRelationshipType.withName("LOVES"),
            DynamicRelationshipType.withName("LIKES"),
            DynamicRelationshipType.withName("NEUTRAL"),
            DynamicRelationshipType.withName("DISLIKES"),
            DynamicRelationshipType.withName("HATES")
    };

    @Override
    public String shortName() {
        return "rel-type-vs-property-write";
    }

    @Override
    public String longName() {
        return "Relationship Type vs. Property";
    }

    @Override
    public List<Parameter> parameters() {
        List<Parameter> result = new LinkedList<>();

        result.add(new EnumParameter(TYPE_OR_PROPERTY, RelationshipTypeOrProperty.class));
        result.add(new ExponentialParameter(BATCH_SIZE, 10, 0, 3, 0.25));

        return result;
    }

    @Override
    public int dryRuns(Map<String, Object> params) {
        return 10;
    }

    @Override
    public int measuredRuns() {
        return 100;
    }

    @Override
    public Map<String, String> databaseParameters(Map<String, Object> params) {
        return Collections.emptyMap();
    }

    @Override
    public void prepareDatabase(GraphDatabaseService database, Map<String, Object> params) {
        new NoInputBatchTransactionExecutor(database, 1000, NO_NODES, new UnitOfWork<NullItem>() {
            @Override
            public void execute(GraphDatabaseService database, NullItem input, int batchNumber, int stepNumber) {
                database.createNode();
            }
        }).execute();
    }

    @Override
    public long run(GraphDatabaseService database, final Map<String, Object> params) {
        final BatchTransactionExecutor executor = new NoInputBatchTransactionExecutor(database, (int) params.get(BATCH_SIZE), NO_RELATIONSHIPS, new UnitOfWork<NullItem>() {
            @Override
            public void execute(GraphDatabaseService database, NullItem input, int batchNumber, int stepNumber) {
                final Node node1 = randomNode(database, NO_NODES);
                final Node node2 = randomNode(database, NO_NODES);

                int randomInt = RANDOM.nextInt(5);

                switch ((RelationshipTypeOrProperty) params.get(TYPE_OR_PROPERTY)) {
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

    @Override
    public RebuildDatabase rebuildDatabase() {
        return RebuildDatabase.AFTER_EVERY_RUN;
    }

    @Override
    public boolean rebuildDatabase(Map<String, Object> params) {
        return false;
    }

    private Node randomNode(GraphDatabaseService database, int noNodes) {
        return database.getNodeById(RANDOM.nextInt(noNodes) + 1);
    }

    private Direction randomDirection() {
        return RANDOM.nextBoolean() ? INCOMING : OUTGOING;
    }

    private DynamicRelationshipType randomType() {
        return withName("TEST" + RANDOM.nextInt(5));
    }
}
