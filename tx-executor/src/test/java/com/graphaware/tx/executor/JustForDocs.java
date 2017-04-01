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

package com.graphaware.tx.executor;

import com.graphaware.tx.executor.batch.*;
import com.graphaware.tx.executor.input.AllNodes;
import com.graphaware.tx.executor.input.TransactionalInput;
import com.graphaware.tx.executor.single.SimpleTransactionExecutor;
import com.graphaware.tx.executor.single.TransactionCallback;
import com.graphaware.tx.executor.single.TransactionExecutor;
import com.graphaware.tx.executor.single.VoidReturningCallback;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.test.TestGraphDatabaseFactory;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static com.graphaware.common.util.DatabaseUtils.registerShutdownHook;

/**
 * Only for documentation. If you need to change this class, change the code in README.md as well please.
 */
public class JustForDocs {

    private void justForDocs() {
        GraphDatabaseService database = new TestGraphDatabaseFactory().newImpermanentDatabase(); //only for demo, use your own persistent one!
        registerShutdownHook(database);

        TransactionExecutor executor = new SimpleTransactionExecutor(database);

        executor.executeInTransaction(new VoidReturningCallback() {
            @Override
            public void doInTx(GraphDatabaseService database) {
                database.createNode();
            }
        });
    }

    private void justForDocs2() {
        GraphDatabaseService database = new TestGraphDatabaseFactory().newImpermanentDatabase(); //only for demo, use your own persistent one!
        registerShutdownHook(database);

        List<String> nodeNames = Arrays.asList("Name1", "Name2", "Name3");  //there will be many more

        int batchSize = 10;
        BatchTransactionExecutor executor = new IterableInputBatchTransactionExecutor<>(database, batchSize, nodeNames, new UnitOfWork<String>() {
            @Override
            public void execute(GraphDatabaseService database, String nodeName, int batchNumber, int stepNumber) {
                Node node = database.createNode();
                node.setProperty("name", nodeName);
            }
        });

        executor.execute();
    }

    private void justForDocs5() {
        GraphDatabaseService database = new TestGraphDatabaseFactory().newImpermanentDatabase(); //only for demo, use your own persistent one!
        registerShutdownHook(database);

        BatchTransactionExecutor executor = new IterableInputBatchTransactionExecutor<>(
                database,
                1000,
                new AllNodes(database, 1000),
                new UnitOfWork<Node>() {
                    @Override
                    public void execute(GraphDatabaseService database, Node node, int batchNumber, int stepNumber) {
                        node.setProperty("uuid", UUID.randomUUID());
                    }
                }
        );

        executor.execute();
    }

    private void justForDocs6() {
        GraphDatabaseService database = new TestGraphDatabaseFactory().newImpermanentDatabase(); //only for demo, use your own persistent one!
        registerShutdownHook(database);

        BatchTransactionExecutor executor = new IterableInputBatchTransactionExecutor<>(
                database,
                1000,
                new TransactionalInput<>(database, 1000, database1 -> database1.getAllNodes()),
                new UnitOfWork<Node>() {
                    @Override
                    public void execute(GraphDatabaseService database, Node node, int batchNumber, int stepNumber) {
                        node.setProperty("uuid", UUID.randomUUID());
                    }
                }
        );

        executor.execute();
    }

    private void justForDocs3() {
        GraphDatabaseService database = null;

        //create 100,000 nodes in batches of 1,000:
        int batchSize = 1000;
        int noNodes = 100000;
        BatchTransactionExecutor batchExecutor = new NoInputBatchTransactionExecutor(database, batchSize, noNodes, CreateRandomNode.getInstance());
        batchExecutor.execute();
    }

    public void justForDocs4() {
        GraphDatabaseService database = null;

        int batchSize = 1000;
        int noNodes = 100000;
        IterableInputBatchTransactionExecutor batchExecutor = new NoInputBatchTransactionExecutor(database, batchSize, noNodes, CreateRandomNode.getInstance());
        BatchTransactionExecutor multiThreadedExecutor = new MultiThreadedBatchTransactionExecutor(batchExecutor, 4);
        multiThreadedExecutor.execute();
    }

    /**
     * Unit of work that creates an empty node with random name. Singleton.
     */
    public static class CreateRandomNode implements UnitOfWork<NullItem> {
        private static final CreateRandomNode INSTANCE = new CreateRandomNode();

        public static CreateRandomNode getInstance() {
            return INSTANCE;
        }

        private CreateRandomNode() {
        }

        @Override
        public void execute(GraphDatabaseService database, NullItem input, int batchNumber, int stepNumber) {
            Node node = database.createNode();
            node.setProperty("name", UUID.randomUUID());
        }
    }
}
