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

import com.graphaware.performance.*;
import com.graphaware.test.TestUtils;
import com.graphaware.tx.executor.NullItem;
import com.graphaware.tx.executor.batch.BatchTransactionExecutor;
import com.graphaware.tx.executor.batch.NoInputBatchTransactionExecutor;
import com.graphaware.tx.executor.batch.UnitOfWork;
import org.apache.log4j.Logger;
import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.graphdb.*;

import java.util.*;

import static com.graphaware.blog.RelationshipTypeOrProperty.PROPERTY;
import static com.graphaware.blog.RelationshipTypeOrProperty.RELATIONSHIP_TYPE;
import static org.neo4j.graphdb.Direction.INCOMING;
import static org.neo4j.graphdb.Direction.OUTGOING;


/**
 *
 */
public class RelationshipTypeVsPropertyReadCypherTest implements PerformanceTest {

    private static final Logger LOG = Logger.getLogger(RelationshipTypeVsPropertyReadCypherTest.class);

    protected static final Random RANDOM = new Random(System.currentTimeMillis());

    private static final String NO_TYPES = "types";
    private static final String TYPE_OR_PROPERTY = "top";
    private static final String CACHE = "cache";

    private static final int NO_NODES = 100;
    private static final int NO_RELATIONSHIPS = 5000;
    public static final int MAX_TYPES = 20;

    private static final RelationshipType RATED = DynamicRelationshipType.withName("RATED");
    private static final RelationshipType[] TYPES;

    static {
        List<RelationshipType> types = new LinkedList<>();
        for (int i = 0; i < MAX_TYPES; i++) {
            types.add(DynamicRelationshipType.withName("TYPE" + i));
        }
        TYPES = types.toArray(new RelationshipType[MAX_TYPES]);
    }

    private RelationshipTypeOrProperty lastRelationshipTypeOrProperty;

    private ExecutionEngine executionEngine;

    @Override
    public String shortName() {
        return "rel-type-vs-property-read";
    }

    @Override
    public String longName() {
        return "Relationship Type vs. Property";
    }

    @Override
    public List<Parameter> parameters() {
        List<Parameter> result = new LinkedList<>();

        result.add(new LinearParameter(NO_TYPES, 5, 5, 1));
        result.add(new CacheParameter(CACHE));
        result.add(new EnumParameter(TYPE_OR_PROPERTY, RelationshipTypeOrProperty.class));

        return result;
    }

    @Override
    public int dryRuns(Map<String, Object> params) {
        return ((CacheConfiguration) params.get(CACHE)).needsWarmup() ? 10000 : 100;
    }

    @Override
    public int measuredRuns() {
        return 100;
    }

    @Override
    public Map<String, String> databaseParameters(Map<String, Object> params) {
        return ((CacheConfiguration) params.get(CACHE)).addToConfig(Collections.<String, String>emptyMap());
    }

    @Override
    public void prepareDatabase(GraphDatabaseService database, final Map<String, Object> params) {
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

                int noTypes = (int) params.get(NO_TYPES);
                int randomInt = RANDOM.nextInt(noTypes);

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

        executor.execute();

        executionEngine = new ExecutionEngine(database);
    }

    @Override
    public long run(GraphDatabaseService database, final Map<String, Object> params) {
        long time = 0;

        final String query = buildQuery(params);

        for (int i = 0; i < 100; i++) {
            time += TestUtils.time(new TestUtils.Timed() {
                @Override
                public void time() {
                    executionEngine.execute(query,Collections.<String, Object>singletonMap("id", RANDOM.nextInt(NO_NODES)));
                }
            });

        }

        return time;
    }

    @Override
    public RebuildDatabase rebuildDatabase() {
        return RebuildDatabase.TEST_DECIDES;
    }

    @Override
    public boolean rebuildDatabase(Map<String, Object> params) {
        RelationshipTypeOrProperty typeOrProperty = (RelationshipTypeOrProperty) params.get(TYPE_OR_PROPERTY);
        boolean result = !typeOrProperty.equals(lastRelationshipTypeOrProperty);
        lastRelationshipTypeOrProperty = typeOrProperty;
        return result;
    }

    private String buildQuery(Map<String, Object> params) {
        StringBuilder query = new StringBuilder("START n=node({id}) MATCH n");

        Direction direction = randomDirection();
        RelationshipTypeOrProperty typeOrProperty = (RelationshipTypeOrProperty) params.get(TYPE_OR_PROPERTY);

        if (INCOMING.equals(direction)) {
            query.append("<");
        }

        query.append("-[");

        final int noTypes = (int) params.get(NO_TYPES);
        if (RELATIONSHIP_TYPE.equals(typeOrProperty)) {
            for (int i = 0; i < noTypes / 2; i++) {
                if (i == 0) {
                    query.append(":");
                }
                else {
                    query.append("|");
                }
                query.append("TYPE").append((noTypes / 2) + i);
            }
        } else {
            query.append("r:RATED");
        }

        query.append("]-");

        if (OUTGOING.equals(direction)) {
            query.append(">");
        }

        query.append("m");

        if (PROPERTY.equals(typeOrProperty)) {
            query.append(" WHERE r.rating >= ").append(noTypes / 2);
        }

        query.append(" RETURN count (m)");

        return query.toString();
    }

    private Node randomNode(GraphDatabaseService database, int noNodes) {
        return database.getNodeById(RANDOM.nextInt(noNodes) + 1);
    }

    private Direction randomDirection() {
        return RANDOM.nextBoolean() ? INCOMING : OUTGOING;
    }
}
