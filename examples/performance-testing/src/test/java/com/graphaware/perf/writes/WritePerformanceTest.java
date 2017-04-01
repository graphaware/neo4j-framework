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

package com.graphaware.perf.writes;

import com.graphaware.test.performance.EnumParameter;
import com.graphaware.test.performance.ExponentialParameter;
import com.graphaware.test.performance.Parameter;
import com.graphaware.test.performance.PerformanceTest;
import com.graphaware.test.util.TestUtils;
import com.graphaware.writer.neo4j.BatchWriter;
import com.graphaware.writer.neo4j.DefaultWriter;
import com.graphaware.writer.neo4j.Neo4jWriter;
import com.graphaware.writer.neo4j.TxPerTaskWriter;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.logging.Log;
import com.graphaware.common.log.LoggerFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Performance test for writing.
 */
public class WritePerformanceTest implements PerformanceTest {

    private static final Log LOG = LoggerFactory.getLogger(WritePerformanceTest.class);

    private static final String BATCH_SIZE = "batchSize";
    private static final String NETWORK_SIZE = "networkSize";
    private static final String NUMBER_OF_THREADS = "numberOfThreads";
    private static final String WRITER = "writer";

    enum Writer {
        NONE,
        DEFAULT,
        TX_PER_TASK,
        BATCH
    }

    @Override
    public String shortName() {
        return "createNetwork";
    }

    @Override
    public String longName() {
        return "Create a random social network according to the Barabasi-Albert model";
    }

    @Override
    public List<Parameter> parameters() {
        List<Parameter> result = new LinkedList<>();

        result.add(new EnumParameter(WRITER, Writer.class));
        result.add(new ExponentialParameter(NUMBER_OF_THREADS, 2, 0, 3, 1));
        result.add(new ExponentialParameter(BATCH_SIZE, 10, 0, 0, 0.25));
        result.add(new ExponentialParameter(NETWORK_SIZE, 10, 3, 3, 1));

        return result;
    }

    @Override
    public int dryRuns(Map<String, Object> stringObjectMap) {
        return 1;
    }

    @Override
    public int measuredRuns() {
        return 3;
    }

    @Override
    public Map<String, String> databaseParameters(Map<String, Object> params) {
        return null;
    }

    @Override
    public void prepare(GraphDatabaseService database, final Map<String, Object> params) {
    }

    @Override
    public long run(final GraphDatabaseService database, final Map<String, Object> params) {
        final Neo4jWriter writer = resolveWriter((Writer) params.get(WRITER), database);
        if (writer != null) {
            writer.start();
        }

        final Neo4jGraphGenerator generator = new Neo4jGraphGenerator(database, (Integer) params.get(NUMBER_OF_THREADS), writer);

        generator.generateGraph((Integer) params.get(NETWORK_SIZE));

        long timeToProcessQueue = 0;
        timeToProcessQueue += TestUtils.time(() -> {
            if (writer != null) {
                writer.stop();
            }
        });

        LOG.warn("Deadlocks: " + generator.getDeadlocks());

        return generator.getTime() + timeToProcessQueue;
    }

    @Override
    public RebuildDatabase rebuildDatabase() {
        return RebuildDatabase.AFTER_EVERY_RUN;
    }

    @Override
    public boolean rebuildDatabase(Map<String, Object> stringObjectMap) {
        return false;
    }

    private Neo4jWriter resolveWriter(Writer writer, GraphDatabaseService database) {
        switch (writer) {
            case NONE:
                return null;
            case DEFAULT:
                return new DefaultWriter(database);
            case TX_PER_TASK:
                return new TxPerTaskWriter(database);
            case BATCH:
                return new BatchWriter(database, 1000, 1000) {
                    @Override
                    protected boolean offer(RunnableFuture<?> futureTask) {
                        try {
                            return queue.offer(futureTask, 1, TimeUnit.MINUTES);
                        } catch (InterruptedException e) {
                            return false;
                        }
                    }
                };
            default:
                throw new IllegalStateException("Unknown writer");
        }
    }
}
