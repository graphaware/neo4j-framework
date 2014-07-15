package com.graphaware.runtime;

import com.graphaware.runtime.bootstrap.RuntimeKernelExtension;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.test.RepeatRule;
import org.neo4j.test.TestGraphDatabaseFactory;

/**
 * Aux runtime tests for bugs found while doing manual testing.
 */
public class OtherRuntimeTests {

    @Rule
    public RepeatRule repeatRule = new RepeatRule();

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test(timeout = 5000)
    @RepeatRule.Repeat(times = 100)
    public void makeSureDeadlockDoesNotOccurWhenTransactionsGetInEarly() {
        GraphDatabaseService database = new TestGraphDatabaseFactory()
                .newImpermanentDatabaseBuilder()
                .setConfig(RuntimeKernelExtension.RUNTIME_ENABLED, "true")
                .newGraphDatabase();

        try (Transaction tx = database.beginTx()) {
            Node node = database.createNode(DynamicLabel.label("TEST"));
            node.setProperty("test", "test");
            tx.success();
        }

        database.shutdown();
    }

    @Test(timeout = 5000)
    @RepeatRule.Repeat(times = 100)
    public void makeSureDeadlockDoesNotOccurWhenTransactionsGetInEarly2() {
        GraphDatabaseService database = new GraphDatabaseFactory()
                .newEmbeddedDatabaseBuilder(temporaryFolder.getRoot().getPath())
                .setConfig(RuntimeKernelExtension.RUNTIME_ENABLED, "true")
                .newGraphDatabase();

        try (Transaction tx = database.beginTx()) {
            Node node = database.createNode(DynamicLabel.label("TEST"));
            node.setProperty("test", "test");
            tx.success();
        }

        database.shutdown();
    }
}
