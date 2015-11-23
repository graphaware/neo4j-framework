package com.graphaware.tx.executor.batch;

import com.graphaware.tx.executor.NullItem;
import com.graphaware.tx.executor.input.AllNodes;
import com.graphaware.tx.executor.input.AllNodesWithLabel;
import com.graphaware.tx.executor.input.NoInput;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.*;
import org.neo4j.test.TestGraphDatabaseFactory;

import java.util.concurrent.atomic.AtomicInteger;

import static com.graphaware.common.util.DatabaseUtils.registerShutdownHook;
import static org.junit.Assert.assertEquals;

public class IterableInputAddingLabelsTest {

    private GraphDatabaseService database;

    @Before
    public void setUp() {
        database = new TestGraphDatabaseFactory().newImpermanentDatabase();
        registerShutdownHook(database);
    }

    @After
    public void tearDown() {
        database.shutdown();
    }

    @Test
    public void testLabelsCanBeAddedInBatch() {
        BatchTransactionExecutor batchExecutor = new NoInputBatchTransactionExecutor(database, 1000, 2000000, new UnitOfWork<NullItem>() {
            @Override
            public void execute(GraphDatabaseService database, NullItem input, int batchNumber, int stepNumber) {
                Node node = database.createNode();
                node.addLabel(DynamicLabel.label("FirstLabel"));
            }
        });
        batchExecutor.execute();

        IterableInputBatchTransactionExecutor executor = new IterableInputBatchTransactionExecutor<Node>(database, 1000,
                new AllNodesWithLabel(database, 1000, DynamicLabel.label("FirstLabel")),
                new UnitOfWork<Node>() {
                    @Override
                    public void execute(GraphDatabaseService database, Node node, int batchNumber, int stepNumber) {
                        node.addLabel(DynamicLabel.label("SecondLabel"));
                    }
                });
        executor.execute();

        AtomicInteger i = new AtomicInteger(0);
        try (Transaction tx = database.beginTx()) {
            ResourceIterator<Node> nodes = database.findNodes(DynamicLabel.label("SecondLabel"));
            while (nodes.hasNext()) {
                i.incrementAndGet();
                nodes.next();
            }

            tx.success();
        }

        assertEquals(2000000, i.get());
    }

}
