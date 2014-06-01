package com.graphaware.tx.executor;

import com.graphaware.tx.executor.batch.*;
import com.graphaware.tx.executor.single.SimpleTransactionExecutor;
import com.graphaware.tx.executor.single.TransactionExecutor;
import com.graphaware.tx.executor.single.VoidReturningCallback;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.test.TestGraphDatabaseFactory;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Only for documentation. If you need to change this class, change the code in README.md as well please.
 */
public class JustForDocs {

    private void justForDocs() {
        GraphDatabaseService database = new TestGraphDatabaseFactory().newImpermanentDatabase(); //only for demo, use your own persistent one!
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
        BatchTransactionExecutor batchExecutor = new NoInputBatchTransactionExecutor(database, batchSize, noNodes, CreateRandomNode.getInstance());
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
