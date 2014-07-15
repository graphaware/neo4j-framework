package com.graphaware.runtime;

import com.graphaware.runtime.bootstrap.RuntimeKernelExtension;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.test.RepeatRule;
import org.neo4j.test.TestGraphDatabaseFactory;

import java.util.Random;

/**
 * Aux runtime tests for bugs found while doing manual testing.
 */
public class OtherRuntimeTests {

    private Random random = new Random();

    @Rule
    public RepeatRule repeatRule = new RepeatRule();

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test(timeout = 5000)
    @RepeatRule.Repeat(times = 100)
    public void makeSureDeadlockDoesNotOccur() throws InterruptedException {
        GraphDatabaseService database = new TestGraphDatabaseFactory()
                .newImpermanentDatabaseBuilder()
                .setConfig(RuntimeKernelExtension.RUNTIME_ENABLED, "true")
                .newGraphDatabase();

        Thread.sleep(random.nextInt(10));

        try (Transaction tx = database.beginTx()) {
            Node node = database.createNode(DynamicLabel.label("TEST"));
            node.setProperty("test", "test");
            tx.success();
        }

        Thread.sleep(random.nextInt(200));

        database.shutdown();
    }

    @Test(timeout = 5000)
    @RepeatRule.Repeat(times = 100)
    public void makeSureDeadlockDoesNotOccur1() throws InterruptedException {
        GraphDatabaseService database = new TestGraphDatabaseFactory()
                .newImpermanentDatabaseBuilder()
                .setConfig(RuntimeKernelExtension.RUNTIME_ENABLED, "true")
                .newGraphDatabase();

        Thread.sleep(random.nextInt(10));


        try (Transaction tx = database.beginTx()) {
            Node node1 = database.createNode();
            node1.setProperty("name", "MB");
            node1.addLabel(DynamicLabel.label("Person"));

            tx.success();
        }

        Thread.sleep(random.nextInt(200));

        database.shutdown();
    }

    @Test(timeout = 5000)
    @RepeatRule.Repeat(times = 100)
    public void makeSureDeadlockDoesNotOccur2() {
        GraphDatabaseService database = new TestGraphDatabaseFactory()
                .newImpermanentDatabaseBuilder()
                .setConfig(RuntimeKernelExtension.RUNTIME_ENABLED, "true")
                .newGraphDatabase();

        try (Transaction tx = database.beginTx()) {
            Node node = database.createNode();
            node.setProperty("test", "test");
            tx.success();
        }

        database.shutdown();
    }

    @Test(timeout = 5000)
    @RepeatRule.Repeat(times = 100)
    public void makeSureDeadlockDoesNotOccur3() {
        GraphDatabaseService database = new TestGraphDatabaseFactory()
                .newImpermanentDatabaseBuilder()
                .setConfig(RuntimeKernelExtension.RUNTIME_ENABLED, "true")
                .newGraphDatabase();

        try (Transaction tx = database.beginTx()) {
            database.createNode();
            tx.success();
        }

        database.shutdown();
    }

    @Test(timeout = 5000)
    @RepeatRule.Repeat(times = 100)
    public void makeSureDeadlockDoesNotOccur4() {
        GraphDatabaseService database = new TestGraphDatabaseFactory()
                .newImpermanentDatabaseBuilder()
                .setConfig(RuntimeKernelExtension.RUNTIME_ENABLED, "true")
                .newGraphDatabase();

        try (Transaction tx = database.beginTx()) {
            database.createNode(DynamicLabel.label("TEST"));
            tx.success();
        }

        database.shutdown();
    }

    @Test(timeout = 5000)
    @RepeatRule.Repeat(times = 100)
    public void makeSureDeadlockDoesNotOccur5() {
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

    @Test(timeout = 5000)
    @RepeatRule.Repeat(times = 100)
    public void makeSureDeadlockDoesNotOccur6() throws InterruptedException {
        GraphDatabaseService database = new GraphDatabaseFactory()
                .newEmbeddedDatabaseBuilder(temporaryFolder.getRoot().getPath())
                .setConfig(RuntimeKernelExtension.RUNTIME_ENABLED, "true")
                .newGraphDatabase();

        try (Transaction tx = database.beginTx()) {
            Node node = database.createNode(DynamicLabel.label("TEST"));
            node.setProperty("test", "test");
            tx.success();
        }

        Thread.sleep(random.nextInt(200));

        database.shutdown();
    }
}
