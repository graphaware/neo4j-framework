package com.graphaware.runtime;

import com.graphaware.runtime.bootstrap.RuntimeKernelExtension;
import org.junit.Rule;
import org.junit.Test;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.test.RepeatRule;
import org.neo4j.test.TestGraphDatabaseFactory;

/**
 * Aux runtime tests for bugs found while doing manual testing.
 */
public class OtherRuntimeTests {

    @Rule
    public RepeatRule repeatRule = new RepeatRule();

    @Test(timeout = 5000)
    @RepeatRule.Repeat(times = 10)
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
    }
}
