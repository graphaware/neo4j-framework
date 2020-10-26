/*
 * Copyright (c) 2013-2020 GraphAware
 *
 * This file is part of the GraphAware Framework.
 *
 * GraphAware Framework is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received a copy of
 * the GNU General Public License along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 */

package com.graphaware.tx.executor.batch;

import com.graphaware.common.junit.InjectNeo4j;
import com.graphaware.common.junit.Neo4jExtension;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
import org.neo4j.harness.Neo4j;
import org.neo4j.harness.Neo4jBuilders;

import static com.graphaware.common.util.IterableUtils.countNodes;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit test for {@link com.graphaware.tx.executor.batch.MultiThreadedBatchTransactionExecutor}.
 */
@ExtendWith(Neo4jExtension.class)
public class MultiThreadedBatchTransactionExecutorTest {

    @InjectNeo4j
    protected GraphDatabaseService database;

    @Test
    public void resultShouldBeCorrectWhenExecutedInMultipleThreads() {
        BatchTransactionExecutor batchExecutor = new MultiThreadedBatchTransactionExecutor(new NoInputBatchTransactionExecutor(database, 100, 40000, CreateNode.getInstance()), 4);

        batchExecutor.execute();

        try (Transaction tx = database.beginTx()) {
            assertEquals(40000, countNodes(tx));
        }
    }
}
