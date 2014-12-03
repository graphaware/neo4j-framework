package com.graphaware.runtime.walk;

import com.graphaware.common.policy.NodeInclusionPolicy;
import com.graphaware.common.policy.none.IncludeNoNodes;
import com.graphaware.tx.executor.NullItem;
import com.graphaware.tx.executor.batch.IterableInputBatchTransactionExecutor;
import com.graphaware.tx.executor.batch.NoInputBatchTransactionExecutor;
import com.graphaware.tx.executor.batch.UnitOfWork;
import com.graphaware.tx.executor.callback.AllNodes;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.test.TestGraphDatabaseFactory;

import static com.graphaware.common.util.DatabaseUtils.registerShutdownHook;
import static org.junit.Assert.*;

/**
 * Unit test for {@link RandomNodeSelector}.
 */
public class RandomNodeSelectorTest {

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
    public void shouldReturnNullOnEmptyDatabase() {
        try (Transaction tx = database.beginTx()) {
            assertNull(new RandomNodeSelector().selectNode(database));
            tx.success();
        }
    }

    @Test
    public void shouldReturnNullOnDatabaseWithAllNodesDeleted() {
        new NoInputBatchTransactionExecutor(database, 1000, 1000, new UnitOfWork<NullItem>() {
            @Override
            public void execute(GraphDatabaseService database, NullItem input, int batchNumber, int stepNumber) {
                database.createNode();
            }
        }).execute();

        new IterableInputBatchTransactionExecutor<>(database, 1000, AllNodes.getInstance(), new UnitOfWork<Node>() {
            @Override
            public void execute(GraphDatabaseService database, Node node, int batchNumber, int stepNumber) {
                node.delete();
            }
        }).execute();

        try (Transaction tx = database.beginTx()) {
            assertNull(new RandomNodeSelector().selectNode(database));
            tx.success();
        }
    }

    @Test
    public void shouldReturnNullWhenNoNodeMatchesTheSpec() {
        new NoInputBatchTransactionExecutor(database, 1000, 1000, new UnitOfWork<NullItem>() {
            @Override
            public void execute(GraphDatabaseService database, NullItem input, int batchNumber, int stepNumber) {
                database.createNode();
            }
        }).execute();

        try (Transaction tx = database.beginTx()) {
            assertNull(new RandomNodeSelector(IncludeNoNodes.getInstance()).selectNode(database));
            tx.success();
        }
    }

    @Test
    public void shouldReturnCorrectNode() {
        new NoInputBatchTransactionExecutor(database, 1000, 1000, new UnitOfWork<NullItem>() {
            @Override
            public void execute(GraphDatabaseService database, NullItem input, int batchNumber, int stepNumber) {
                database.createNode(DynamicLabel.label("Label" + (stepNumber % 10)));
            }
        }).execute();

        try (Transaction tx = database.beginTx()) {
            assertNotNull(new RandomNodeSelector().selectNode(database));

            Node node = new RandomNodeSelector(new NodeInclusionPolicy() {
                @Override
                public boolean include(Node object) {
                    return object.hasLabel(DynamicLabel.label("Label4"));
                }
            }).selectNode(database);

            assertNotNull(node);
            assertTrue(node.hasLabel(DynamicLabel.label("Label4")));

            tx.success();
        }
    }

    @Test
    public void shouldReturnTheOnlyNode() {
        try (Transaction tx = database.beginTx()) {
            database.createNode();
            tx.success();
        }

        try (Transaction tx = database.beginTx()) {
            Node node = new RandomNodeSelector().selectNode(database);
            assertEquals(0, node.getId());
            tx.success();
        }
    }

    @Test
    public void shouldReturnTheOnlyRemainingNodeAfterTheRestHasBeenDeleted() {
        new NoInputBatchTransactionExecutor(database, 1000, 1000, new UnitOfWork<NullItem>() {
            @Override
            public void execute(GraphDatabaseService database, NullItem input, int batchNumber, int stepNumber) {
                database.createNode();
            }
        }).execute();

        new IterableInputBatchTransactionExecutor<>(database, 1000, AllNodes.getInstance(), new UnitOfWork<Node>() {
            @Override
            public void execute(GraphDatabaseService database, Node node, int batchNumber, int stepNumber) {
                if (stepNumber != 1000) {
                    node.delete();
                }
            }
        }).execute();

        try (Transaction tx = database.beginTx()) {
            Node node = new RandomNodeSelector().selectNode(database);
            assertEquals(999, node.getId());
            tx.success();
        }
    }
}
