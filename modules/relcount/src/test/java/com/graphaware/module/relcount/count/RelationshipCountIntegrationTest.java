package com.graphaware.module.relcount.count;

import com.graphaware.common.strategy.RelationshipInclusionStrategy;
import com.graphaware.common.strategy.RelationshipPropertyInclusionStrategy;
import com.graphaware.module.relcount.RelationshipCountRuntimeModule;
import com.graphaware.module.relcount.compact.ThresholdBasedCompactionStrategy;
import com.graphaware.runtime.ProductionGraphAwareRuntime;
import com.graphaware.tx.executor.single.SimpleTransactionExecutor;
import com.graphaware.tx.executor.single.VoidReturningCallback;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.test.TestGraphDatabaseFactory;

import java.io.IOException;
import java.nio.ByteBuffer;

import static com.graphaware.common.description.predicate.Predicates.equalTo;
import static com.graphaware.common.description.relationship.RelationshipDescriptionFactory.literal;
import static com.graphaware.common.description.relationship.RelationshipDescriptionFactory.wildcard;
import static com.graphaware.module.relcount.RelationshipCountConfigurationImpl.defaultConfiguration;
import static com.graphaware.module.relcount.count.RelationshipCountIntegrationTest.RelationshipTypes.ONE;
import static com.graphaware.module.relcount.count.RelationshipCountIntegrationTest.RelationshipTypes.TWO;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.neo4j.graphdb.Direction.*;
import static org.neo4j.graphdb.DynamicRelationshipType.withName;

/**
 * Integration test for relationship counting.
 */
@SuppressWarnings("PointlessArithmeticExpression")
public class RelationshipCountIntegrationTest {

    public static final String WEIGHT = "weight";
    public static final String NAME = "name";
    public static final String TIMESTAMP = "timestamp";
    public static final String K1 = "K1";
    public static final String K2 = "K2";

    public enum RelationshipTypes implements RelationshipType {
        ONE,
        TWO
    }

    protected GraphDatabaseService database;

    @Before
    public void setUp() {
        database = new TestGraphDatabaseFactory().newImpermanentDatabase();

        try (Transaction tx = database.beginTx()) {
            database.createNode(); //ID = 0
            tx.success();
        }
    }

    @After
    public void tearDown() {
        database.shutdown();
    }

    @Test
    public void naiveCounterShouldWorkWithoutRuntime() {
        setUpTwoNodes();
        simulateUsage();

        verifyCounts(1, new NaiveRelationshipCounter(database));
    }

    @Test
    public void naiveCounterShouldRespectWeighingWithoutRuntime() {
        setUpTwoNodes();
        simulateUsage();

        verifyCounts(2, new NaiveRelationshipCounter(database, new WeighingStrategy() {
            @Override
            public int getRelationshipWeight(Relationship relationship, Node pointOfView) {
                return 2;
            }
        }));
    }

    @Test(expected = IllegalStateException.class)
    public void cachedCounterShouldNotWorkWithoutRuntime() {
        setUpTwoNodes();
        simulateUsage();

        verifyCounts(0, new CachedRelationshipCounter(database));
    }

    @Test(expected = IllegalStateException.class)
    public void fallBackCounterShouldNotWorkWithoutRuntime() {
        setUpTwoNodes();
        simulateUsage();

        verifyCounts(0, new FallbackRelationshipCounter(database));
    }

    @Test
    public void cachedCountsCanBeRebuilt() {
        ProductionGraphAwareRuntime runtime = new ProductionGraphAwareRuntime(database);
        final RelationshipCountRuntimeModule module = new RelationshipCountRuntimeModule();
        runtime.registerModule(module);
        runtime.start();

        setUpTwoNodes();
        simulateUsage();

        module.reinitialize(database);

        verifyCounts(1, new NaiveRelationshipCounter(database));
        verifyCounts(1, new CachedRelationshipCounter(database));
        verifyCounts(1, new FallbackRelationshipCounter(database));
    }

    @Test
    public void defaultRuntimeOnNewDatabase() {
        ProductionGraphAwareRuntime runtime = new ProductionGraphAwareRuntime(database);
        final RelationshipCountRuntimeModule module = new RelationshipCountRuntimeModule();
        runtime.registerModule(module);
        runtime.start();

        setUpTwoNodes();
        simulateUsage();

        verifyCounts(1, new NaiveRelationshipCounter(database));
        verifyCounts(1, new CachedRelationshipCounter(database));
        verifyCounts(1, new FallbackRelationshipCounter(database));
    }

    @Test
    public void defaultRuntimeWithChangedModule() throws IOException {
        TemporaryFolder temporaryFolder = new TemporaryFolder();
        temporaryFolder.create();
        database = new GraphDatabaseFactory().newEmbeddedDatabase(temporaryFolder.getRoot().getAbsolutePath());

        ProductionGraphAwareRuntime runtime = new ProductionGraphAwareRuntime(database);
        RelationshipCountRuntimeModule module = new RelationshipCountRuntimeModule();
        runtime.registerModule(module);
        runtime.start();

        setUpTwoNodes();
        simulateUsage();

        verifyCounts(1, new NaiveRelationshipCounter(database));
        verifyCounts(1, new CachedRelationshipCounter(database));
        verifyCounts(1, new FallbackRelationshipCounter(database));

        database.shutdown();

        database = new GraphDatabaseFactory().newEmbeddedDatabase(temporaryFolder.getRoot().getAbsolutePath());

        runtime = new ProductionGraphAwareRuntime(database);
        module = new RelationshipCountRuntimeModule(defaultConfiguration().with(new ThresholdBasedCompactionStrategy(4)));
        runtime.registerModule(module);
        runtime.start();

        verifyCounts(1, new NaiveRelationshipCounter(database));
        verifyCompactedCounts(1, new CachedRelationshipCounter(database));
        verifyCounts(1, new FallbackRelationshipCounter(database));

        database.shutdown();

        database = new GraphDatabaseFactory().newEmbeddedDatabase(temporaryFolder.getRoot().getAbsolutePath());

        runtime = new ProductionGraphAwareRuntime(database);
        module = new RelationshipCountRuntimeModule(defaultConfiguration().with(new ThresholdBasedCompactionStrategy(20)));
        runtime.registerModule(module);
        runtime.start();

        verifyCounts(1, new NaiveRelationshipCounter(database));
        verifyCounts(1, new CachedRelationshipCounter(database));
        verifyCounts(1, new FallbackRelationshipCounter(database));
    }

    @Test
    public void defaultRuntimeOnExistingDatabase() {
        setUpTwoNodes();
        simulateUsage();

        ProductionGraphAwareRuntime runtime = new ProductionGraphAwareRuntime(database);
        final RelationshipCountRuntimeModule module = new RelationshipCountRuntimeModule();
        runtime.registerModule(module);
        runtime.start();

        verifyCounts(1, new NaiveRelationshipCounter(database));
        verifyCounts(1, new CachedRelationshipCounter(database));
        verifyCounts(1, new FallbackRelationshipCounter(database));
    }

    @Test
    public void customRuntimeOnNewDatabase() {
        ProductionGraphAwareRuntime runtime = new ProductionGraphAwareRuntime(database);
        final RelationshipCountRuntimeModule module = new RelationshipCountRuntimeModule();
        runtime.registerModule(module);
        runtime.start();

        setUpTwoNodes();
        simulateUsage();

        verifyCounts(1, new NaiveRelationshipCounter(database));
        verifyCounts(1, new CachedRelationshipCounter(database));
        verifyCounts(1, new FallbackRelationshipCounter(database));
    }

    @Test
    public void customRuntimeOnExistingDatabase() {
        setUpTwoNodes();
        simulateUsage();

        ProductionGraphAwareRuntime runtime = new ProductionGraphAwareRuntime(database);
        final RelationshipCountRuntimeModule module = new RelationshipCountRuntimeModule();
        runtime.registerModule(module);
        runtime.start();

        verifyCounts(1, new NaiveRelationshipCounter(database));
        verifyCounts(1, new CachedRelationshipCounter(database));
        verifyCounts(1, new FallbackRelationshipCounter(database));
    }

    @Test
    public void weightedRelationships() {
        for (int numberOfRounds = 1; numberOfRounds <= 10; numberOfRounds++) {
            setUp();

            ProductionGraphAwareRuntime runtime = new ProductionGraphAwareRuntime(database);
            final RelationshipCountRuntimeModule module = new RelationshipCountRuntimeModule(
                    defaultConfiguration()
                            .with(new WeighingStrategy() {
                                @Override
                                public int getRelationshipWeight(Relationship relationship, Node pointOfView) {
                                    return (int) relationship.getProperty(WEIGHT, 1);
                                }
                            }));

            runtime.registerModule(module);
            runtime.start();

            setUpTwoNodes();

            for (int i = 0; i < numberOfRounds; i++) {
                simulateUsage();
            }

            verifyWeightedCounts(numberOfRounds, new NaiveRelationshipCounter(database));
            verifyWeightedCounts(numberOfRounds, new CachedRelationshipCounter(database));
            verifyWeightedCounts(numberOfRounds, new FallbackRelationshipCounter(database));

            tearDown();
        }
    }

    @Test
    public void defaultStrategiesWithLowerThreshold() {
        ProductionGraphAwareRuntime runtime = new ProductionGraphAwareRuntime(database);
        final RelationshipCountRuntimeModule module = new RelationshipCountRuntimeModule(
                defaultConfiguration().with(new ThresholdBasedCompactionStrategy(4))
        );
        runtime.registerModule(module);
        runtime.start();

        setUpTwoNodes();
        simulateUsage();

        verifyCounts(1, new NaiveRelationshipCounter(database));
        verifyCompactedCounts(1, new CachedRelationshipCounter(database));
        verifyCounts(1, new FallbackRelationshipCounter(database));
    }

    @Test
    public void defaultStrategiesWithLowerThreshold2() {
        ProductionGraphAwareRuntime runtime = new ProductionGraphAwareRuntime(database);
        final RelationshipCountRuntimeModule module = new RelationshipCountRuntimeModule(
                defaultConfiguration().with(new ThresholdBasedCompactionStrategy(4))
        );
        runtime.registerModule(module);
        runtime.start();

        setUpTwoNodes();
        simulateUsage();
        simulateUsage();

        verifyCounts(2, new NaiveRelationshipCounter(database));
        verifyCompactedCounts(2, new CachedRelationshipCounter(database));
        verifyCounts(2, new FallbackRelationshipCounter(database));
    }

    @Test
    public void defaultStrategiesWithLowerThreshold3() {
        ProductionGraphAwareRuntime runtime = new ProductionGraphAwareRuntime(database);
        final RelationshipCountRuntimeModule module = new RelationshipCountRuntimeModule(
                defaultConfiguration().with(new ThresholdBasedCompactionStrategy(3))
        );
        runtime.registerModule(module);
        runtime.start();

        setUpTwoNodes();
        simulateUsage();

        try (Transaction tx = database.beginTx()) {
            try {
                new CachedRelationshipCounter(database).count(database.getNodeById(1), wildcard(ONE, OUTGOING).with(WEIGHT, equalTo(2)).with(TIMESTAMP, equalTo("123")).with(K1, equalTo("V1")));
                fail();
            } catch (UnableToCountException e) {
                //OK
            }

            tx.success();
        }
    }

    @Test
    public void defaultStrategiesWithLowerThreshold20() {
        for (int threshold = 3; threshold <= 20; threshold++) {
            setUp();

            ProductionGraphAwareRuntime runtime = new ProductionGraphAwareRuntime(database);
            final RelationshipCountRuntimeModule module = new RelationshipCountRuntimeModule(
                    defaultConfiguration().with(new ThresholdBasedCompactionStrategy(threshold))
            );
            runtime.registerModule(module);
            runtime.start();

            setUpTwoNodes();
            simulateUsage();
            simulateUsage();
            simulateUsage();

            verifyCounts(3, new FallbackRelationshipCounter(database));
            verifyCounts(3, new NaiveRelationshipCounter(database));

            tearDown();
        }
    }

    @Test
    public void weightedRelationshipsWithCompaction() {
        ProductionGraphAwareRuntime runtime = new ProductionGraphAwareRuntime(database);
        final RelationshipCountRuntimeModule module = new RelationshipCountRuntimeModule(
                defaultConfiguration()
                        .with(new WeighingStrategy() {
                            @Override
                            public int getRelationshipWeight(Relationship relationship, Node pointOfView) {
                                return (int) relationship.getProperty(WEIGHT, 1);
                            }
                        })
                        .with(new ThresholdBasedCompactionStrategy(10)));

        runtime.registerModule(module);
        runtime.start();

        setUpTwoNodes();
        simulateUsage();
        simulateUsage();
        simulateUsage();
        simulateUsage();

        verifyWeightedCounts(4, new FallbackRelationshipCounter(database));
        verifyWeightedCounts(4, new NaiveRelationshipCounter(database));
    }

    @Test
    public void twoSimultaneousModules() {
        ProductionGraphAwareRuntime runtime = new ProductionGraphAwareRuntime(database);
        final RelationshipCountRuntimeModule module1 = new RelationshipCountRuntimeModule("M1", defaultConfiguration());
        final RelationshipCountRuntimeModule module2 = new RelationshipCountRuntimeModule("M2",
                defaultConfiguration()
                        .with(new WeighingStrategy() {
                            @Override
                            public int getRelationshipWeight(Relationship relationship, Node pointOfView) {
                                return (int) relationship.getProperty(WEIGHT, 1);
                            }
                        }));

        runtime.registerModule(module1);
        runtime.registerModule(module2);
        runtime.start();

        setUpTwoNodes();
        simulateUsage();
        simulateUsage();

        verifyCounts(2, new NaiveRelationshipCounter(database, "M1"));
        verifyCounts(2, new CachedRelationshipCounter(database, "M1"));
        verifyCounts(2, new FallbackRelationshipCounter(database, "M1"));

        verifyWeightedCounts(2, new NaiveRelationshipCounter(database, "M2"));
        verifyWeightedCounts(2, new CachedRelationshipCounter(database, "M2"));
        verifyWeightedCounts(2, new FallbackRelationshipCounter(database, "M2"));
    }

    @Test
    public void customRelationshipInclusionStrategy() {
        ProductionGraphAwareRuntime runtime = new ProductionGraphAwareRuntime(database);
        final RelationshipCountRuntimeModule module = new RelationshipCountRuntimeModule(
                defaultConfiguration()
                        .with(new RelationshipInclusionStrategy() {
                            @Override
                            public boolean include(Relationship relationship) {
                                return !relationship.isType(TWO);
                            }
                        }));

        runtime.registerModule(module);
        runtime.start();

        setUpTwoNodes();
        simulateUsage();
        simulateUsage();

        try (Transaction tx = database.beginTx()) {
            //naive doesn't care about this strategy
            assertEquals(2, new NaiveRelationshipCounter(database).count(database.getNodeById(1), wildcard(TWO, OUTGOING)));
            assertEquals(0, new FallbackRelationshipCounter(database).count(database.getNodeById(1), wildcard(TWO, OUTGOING)));
            assertEquals(0, new CachedRelationshipCounter(database).count(database.getNodeById(1), wildcard(TWO, OUTGOING)));

            tx.success();
        }
    }

    @Test
    public void customRelationshipPropertiesInclusionStrategy() {
        ProductionGraphAwareRuntime runtime = new ProductionGraphAwareRuntime(database);
        final RelationshipCountRuntimeModule module = new RelationshipCountRuntimeModule(
                defaultConfiguration()
                        .with(new RelationshipPropertyInclusionStrategy() {
                            @Override
                            public boolean include(String key, Relationship propertyContainer) {
                                return !WEIGHT.equals(key);
                            }
                        }));

        runtime.registerModule(module);
        runtime.start();

        setUpTwoNodes();
        simulateUsage();
        simulateUsage();

        try (Transaction tx = database.beginTx()) {
            //naive doesn't care about this strategy
            assertEquals(2, new NaiveRelationshipCounter(database).count(database.getNodeById(1), wildcard(ONE, OUTGOING).with(WEIGHT, equalTo(7))));
            assertEquals(2, new NaiveRelationshipCounter(database).count(database.getNodeById(1), literal(ONE, OUTGOING).with(WEIGHT, equalTo(7))));
            assertEquals(0, new FallbackRelationshipCounter(database).count(database.getNodeById(1), wildcard(ONE, OUTGOING).with(WEIGHT, equalTo(7))));
            assertEquals(0, new FallbackRelationshipCounter(database).count(database.getNodeById(1), literal(ONE, OUTGOING).with(WEIGHT, equalTo(7))));
            assertEquals(0, new CachedRelationshipCounter(database).count(database.getNodeById(1), wildcard(ONE, OUTGOING).with(WEIGHT, equalTo(7))));
            assertEquals(0, new CachedRelationshipCounter(database).count(database.getNodeById(1), literal(ONE, OUTGOING).with(WEIGHT, equalTo(7))));

            tx.success();
        }
    }

    @Test
    public void batchTest() {
        ProductionGraphAwareRuntime runtime = new ProductionGraphAwareRuntime(database);
        final RelationshipCountRuntimeModule module = new RelationshipCountRuntimeModule();
        runtime.registerModule(module);
        runtime.start();

        setUpTwoNodes();

        new SimpleTransactionExecutor(database).executeInTransaction(new VoidReturningCallback() {
            @Override
            protected void doInTx(GraphDatabaseService database) {
                for (int i = 0; i < 100; i++) {
                    simulateUsage();
                }
            }
        });

        verifyCounts(100, new NaiveRelationshipCounter(database));
        verifyCounts(100, new CachedRelationshipCounter(database));
        verifyCounts(100, new FallbackRelationshipCounter(database));
    }

    @Test
    public void batchTestWithMultipleModulesAndLowerThreshold() {
        ProductionGraphAwareRuntime runtime = new ProductionGraphAwareRuntime(database);
        final RelationshipCountRuntimeModule module1 = new RelationshipCountRuntimeModule("M1", defaultConfiguration().with(new ThresholdBasedCompactionStrategy(4)));
        final RelationshipCountRuntimeModule module2 = new RelationshipCountRuntimeModule("M2", defaultConfiguration().with(new ThresholdBasedCompactionStrategy(4)));
        runtime.registerModule(module1);
        runtime.registerModule(module2);
        runtime.start();

        setUpTwoNodes();

        new SimpleTransactionExecutor(database).executeInTransaction(new VoidReturningCallback() {
            @Override
            protected void doInTx(GraphDatabaseService database) {
                for (int i = 0; i < 20; i++) {
                    simulateUsage();
                }
            }
        });

        verifyCounts(20, new NaiveRelationshipCounter(database, "M1"));
        verifyCompactedCounts(20, new CachedRelationshipCounter(database, "M1"));
        verifyCounts(20, new FallbackRelationshipCounter(database, "M1"));

        verifyCounts(20, new NaiveRelationshipCounter(database, "M2"));
        verifyCompactedCounts(20, new CachedRelationshipCounter(database, "M2"));
        verifyCounts(20, new FallbackRelationshipCounter(database, "M2"));
    }

    @Test
    public void carefullySetupScenarioThatCouldResultInInaccurateCounts() {
        ProductionGraphAwareRuntime runtime = new ProductionGraphAwareRuntime(database);
        final RelationshipCountRuntimeModule module = new RelationshipCountRuntimeModule(defaultConfiguration().with(new ThresholdBasedCompactionStrategy(2)));
        runtime.registerModule(module);
        runtime.start();

        setUpTwoNodes();
        new SimpleTransactionExecutor(database).executeInTransaction(new VoidReturningCallback() {
            @Override
            protected void doInTx(GraphDatabaseService database) {
                Node one = database.getNodeById(1);
                Node two = database.getNodeById(2);

                Relationship r1 = one.createRelationshipTo(two, withName("TEST"));
                r1.setProperty("a", 2);
                r1.setProperty("b", "b");
            }
        });

        try (Transaction tx = database.beginTx()) {
            assertEquals(1, new CachedRelationshipCounter(database).count(database.getNodeById(1), wildcard("TEST", OUTGOING)));
            assertEquals(1, new CachedRelationshipCounter(database).count(database.getNodeById(1), wildcard("TEST", OUTGOING).with("a", equalTo(2))));
            assertEquals(1, new CachedRelationshipCounter(database).count(database.getNodeById(1), wildcard("TEST", OUTGOING).with("b", equalTo("b"))));

            tx.success();
        }

        new SimpleTransactionExecutor(database).executeInTransaction(new VoidReturningCallback() {
            @Override
            protected void doInTx(GraphDatabaseService database) {
                Node one = database.getNodeById(1);
                Node two = database.getNodeById(2);

                Relationship r1 = one.createRelationshipTo(two, withName("TEST"));
                r1.setProperty("a", 1);
                r1.setProperty("b", "c");
            }
        });

        try (Transaction tx = database.beginTx()) {
            assertEquals(2, new CachedRelationshipCounter(database).count(database.getNodeById(1), wildcard("TEST", OUTGOING)));
            assertEquals(1, new CachedRelationshipCounter(database).count(database.getNodeById(1), wildcard("TEST", OUTGOING).with("a", equalTo(1))));
            assertEquals(1, new CachedRelationshipCounter(database).count(database.getNodeById(1), wildcard("TEST", OUTGOING).with("a", equalTo(2))));
            assertEquals(1, new CachedRelationshipCounter(database).count(database.getNodeById(1), wildcard("TEST", OUTGOING).with("b", equalTo("b"))));
            assertEquals(1, new CachedRelationshipCounter(database).count(database.getNodeById(1), wildcard("TEST", OUTGOING).with("b", equalTo("c"))));

            tx.success();
        }

        new SimpleTransactionExecutor(database).executeInTransaction(new VoidReturningCallback() {
            @Override
            protected void doInTx(GraphDatabaseService database) {
                Node one = database.getNodeById(1);
                Node two = database.getNodeById(2);

                Relationship r1 = one.createRelationshipTo(two, withName("TEST"));
                r1.setProperty("a", 3);
                r1.setProperty("b", "c");
            }
        });

        try (Transaction tx = database.beginTx()) {
            //now we should have 2b, *c
            assertEquals(3, new CachedRelationshipCounter(database).count(database.getNodeById(1), wildcard("TEST", OUTGOING)));
            assertEquals(1, new CachedRelationshipCounter(database).count(database.getNodeById(1), wildcard("TEST", OUTGOING).with("b", equalTo("b"))));
            assertEquals(2, new CachedRelationshipCounter(database).count(database.getNodeById(1), wildcard("TEST", OUTGOING).with("b", equalTo("c"))));

            tx.success();
        }

        try (Transaction tx = database.beginTx()) {
            try {
                new CachedRelationshipCounter(database).count(database.getNodeById(1), wildcard("TEST", OUTGOING).with("a", equalTo(2)));
                fail();
            } catch (UnableToCountException e) {
                //ok
            }

            tx.success();
        }

        new SimpleTransactionExecutor(database).executeInTransaction(new VoidReturningCallback() {
            @Override
            protected void doInTx(GraphDatabaseService database) {
                Node one = database.getNodeById(1);
                Node two = database.getNodeById(2);

                Relationship r1 = one.createRelationshipTo(two, withName("TEST"));
                r1.setProperty("a", 2);
                r1.setProperty("b", "d");
            }
        });

        try (Transaction tx = database.beginTx()) {

            //now we should have 2*, *c

            assertEquals(4, new CachedRelationshipCounter(database).count(database.getNodeById(1), wildcard("TEST", OUTGOING)));

            try {
                new CachedRelationshipCounter(database).count(database.getNodeById(1), wildcard("TEST", OUTGOING).with("a", equalTo(2)));
                fail();
            } catch (UnableToCountException e) {
                //ok
            }

            try {
                new CachedRelationshipCounter(database).count(database.getNodeById(1), wildcard("TEST", OUTGOING).with("b", equalTo("c")));
                fail();
            } catch (UnableToCountException e) {
                //ok
            }

            tx.success();
        }

        new SimpleTransactionExecutor(database).executeInTransaction(new VoidReturningCallback() {
            @Override
            protected void doInTx(GraphDatabaseService database) {
                Node one = database.getNodeById(1);
                Node two = database.getNodeById(2);

                Relationship r1 = one.createRelationshipTo(two, withName("TEST"));
                r1.setProperty("a", 2);
                r1.setProperty("b", "c");
            }
        });

        try (Transaction tx = database.beginTx()) {
            assertEquals(5, new CachedRelationshipCounter(database).count(database.getNodeById(1), wildcard("TEST", OUTGOING)));


            try {
                new CachedRelationshipCounter(database).count(database.getNodeById(1), wildcard("TEST", OUTGOING).with("a", equalTo(2)));
                fail();
            } catch (UnableToCountException e) {
                //ok
            }

            try {
                new CachedRelationshipCounter(database).count(database.getNodeById(1), wildcard("TEST", OUTGOING).with("b", equalTo("c")));
                fail();
            } catch (UnableToCountException e) {
                //ok
            }

            tx.success();
        }

        //now add one more that will cause * *
        new SimpleTransactionExecutor(database).executeInTransaction(new VoidReturningCallback() {
            @Override
            protected void doInTx(GraphDatabaseService database) {
                Node one = database.getNodeById(1);
                Node two = database.getNodeById(2);

                Relationship r1 = one.createRelationshipTo(two, withName("TEST"));
                r1.setProperty("a", 3);
                r1.setProperty("b", "d");
            }
        });

        try (Transaction tx = database.beginTx()) {
            assertEquals(6, new CachedRelationshipCounter(database).count(database.getNodeById(1), wildcard("TEST", OUTGOING)));

            try {
                new CachedRelationshipCounter(database).count(database.getNodeById(1), wildcard("TEST", OUTGOING).with("a", equalTo(2)));
                fail();
            } catch (UnableToCountException e) {
                //ok
            }

            try {
                new CachedRelationshipCounter(database).count(database.getNodeById(1), wildcard("TEST", OUTGOING).with("b", equalTo("c")));
                fail();
            } catch (UnableToCountException e) {
                //ok
            }

            tx.success();
        }
    }

    private void verifyCounts(int factor, RelationshipCounter counter) {
        try (Transaction tx = database.beginTx()) {

            Node one = database.getNodeById(1);
            Node two = database.getNodeById(2);

            //Node one incoming

            assertEquals(3 * factor, counter.count(one, wildcard(ONE, INCOMING)));
            assertEquals(0 * factor, counter.count(one, literal(ONE, INCOMING)));

            assertEquals(0 * factor, counter.count(one, wildcard(ONE, INCOMING).with(WEIGHT, equalTo(1))));
            assertEquals(0 * factor, counter.count(one, literal(ONE, INCOMING).with(WEIGHT, equalTo(1))));
            assertEquals(1 * factor, counter.count(one, wildcard(ONE, INCOMING).with(WEIGHT, equalTo(2))));
            assertEquals(0 * factor, counter.count(one, literal(ONE, INCOMING).with(WEIGHT, equalTo(2))));
            assertEquals(0 * factor, counter.count(one, wildcard(ONE, INCOMING).with(WEIGHT, equalTo(3))));
            assertEquals(0 * factor, counter.count(one, literal(ONE, INCOMING).with(WEIGHT, equalTo(3))));
            assertEquals(0 * factor, counter.count(one, wildcard(ONE, INCOMING).with(WEIGHT, equalTo(4))));
            assertEquals(0 * factor, counter.count(one, literal(ONE, INCOMING).with(WEIGHT, equalTo(4))));
            assertEquals(0 * factor, counter.count(one, wildcard(ONE, INCOMING).with(WEIGHT, equalTo(5))));
            assertEquals(0 * factor, counter.count(one, literal(ONE, INCOMING).with(WEIGHT, equalTo(5))));
            assertEquals(1 * factor, counter.count(one, wildcard(ONE, INCOMING).with(WEIGHT, equalTo(7))));
            assertEquals(1 * factor, counter.count(one, literal(ONE, INCOMING).with(WEIGHT, equalTo(7))));

            assertEquals(2 * factor, counter.count(one, wildcard(ONE, INCOMING).with(K1, equalTo("V1"))));
            assertEquals(0 * factor, counter.count(one, literal(ONE, INCOMING).with(K1, equalTo("V1"))));
            assertEquals(0 * factor, counter.count(one, wildcard(ONE, INCOMING).with(K1, equalTo("V2"))));
            assertEquals(0 * factor, counter.count(one, literal(ONE, INCOMING).with(K1, equalTo("V2"))));

            assertEquals(0 * factor, counter.count(one, wildcard(ONE, INCOMING).with(K2, equalTo("V1"))));
            assertEquals(0 * factor, counter.count(one, literal(ONE, INCOMING).with(K2, equalTo("V1"))));
            assertEquals(1 * factor, counter.count(one, wildcard(ONE, INCOMING).with(K2, equalTo("V2"))));
            assertEquals(0 * factor, counter.count(one, literal(ONE, INCOMING).with(K2, equalTo("V2"))));

            assertEquals(1 * factor, counter.count(one, wildcard(ONE, INCOMING).with(K1, equalTo("V1")).with(K2, equalTo("V2"))));
            assertEquals(1 * factor, counter.count(one, literal(ONE, INCOMING).with(K1, equalTo("V1")).with(K2, equalTo("V2"))));
            assertEquals(0 * factor, counter.count(one, wildcard(ONE, INCOMING).with(K1, equalTo("V1")).with(K2, equalTo("V1"))));
            assertEquals(0 * factor, counter.count(one, literal(ONE, INCOMING).with(K1, equalTo("V1")).with(K2, equalTo("V1"))));

            assertEquals(1 * factor, counter.count(one, wildcard(ONE, INCOMING).with(WEIGHT, equalTo(2)).with(K1, equalTo("V1"))));
            assertEquals(1 * factor, counter.count(one, literal(ONE, INCOMING).with(WEIGHT, equalTo(2)).with(K1, equalTo("V1"))));
            assertEquals(0 * factor, counter.count(one, wildcard(ONE, INCOMING).with(WEIGHT, equalTo(2)).with(K2, equalTo("V2"))));
            assertEquals(0 * factor, counter.count(one, literal(ONE, INCOMING).with(WEIGHT, equalTo(2)).with(K2, equalTo("V2"))));

            //Node one outgoing

            assertEquals(7 * factor, counter.count(one, wildcard(ONE, OUTGOING)));
            assertEquals(0 * factor, counter.count(one, literal(ONE, OUTGOING)));

            assertEquals(2 * factor, counter.count(one, wildcard(ONE, OUTGOING).with(WEIGHT, equalTo(1))));
            assertEquals(1 * factor, counter.count(one, literal(ONE, OUTGOING).with(WEIGHT, equalTo(1))));
            assertEquals(1 * factor, counter.count(one, wildcard(ONE, OUTGOING).with(WEIGHT, equalTo(2))));
            assertEquals(0 * factor, counter.count(one, literal(ONE, OUTGOING).with(WEIGHT, equalTo(2))));
            assertEquals(0 * factor, counter.count(one, wildcard(ONE, OUTGOING).with(WEIGHT, equalTo(3))));
            assertEquals(0 * factor, counter.count(one, literal(ONE, OUTGOING).with(WEIGHT, equalTo(3))));
            assertEquals(0 * factor, counter.count(one, wildcard(ONE, OUTGOING).with(WEIGHT, equalTo(4))));
            assertEquals(0 * factor, counter.count(one, literal(ONE, OUTGOING).with(WEIGHT, equalTo(4))));
            assertEquals(0 * factor, counter.count(one, wildcard(ONE, OUTGOING).with(WEIGHT, equalTo(5))));
            assertEquals(0 * factor, counter.count(one, literal(ONE, OUTGOING).with(WEIGHT, equalTo(5))));
            assertEquals(1 * factor, counter.count(one, wildcard(ONE, OUTGOING).with(WEIGHT, equalTo(7))));
            assertEquals(1 * factor, counter.count(one, literal(ONE, OUTGOING).with(WEIGHT, equalTo(7))));

            assertEquals(5 * factor, counter.count(one, wildcard(ONE, OUTGOING).with(K1, equalTo("V1"))));
            assertEquals(3 * factor, counter.count(one, literal(ONE, OUTGOING).with(K1, equalTo("V1"))));
            assertEquals(0 * factor, counter.count(one, wildcard(ONE, OUTGOING).with(K1, equalTo("V2"))));
            assertEquals(0 * factor, counter.count(one, literal(ONE, OUTGOING).with(K1, equalTo("V2"))));

            assertEquals(0 * factor, counter.count(one, wildcard(ONE, OUTGOING).with(K2, equalTo("V1"))));
            assertEquals(0 * factor, counter.count(one, literal(ONE, OUTGOING).with(K2, equalTo("V1"))));
            assertEquals(0 * factor, counter.count(one, wildcard(ONE, OUTGOING).with(K2, equalTo("V2"))));
            assertEquals(0 * factor, counter.count(one, literal(ONE, OUTGOING).with(K2, equalTo("V2"))));

            assertEquals(0 * factor, counter.count(one, wildcard(ONE, OUTGOING).with(K1, equalTo("V1")).with(K2, equalTo("V2"))));
            assertEquals(0 * factor, counter.count(one, literal(ONE, OUTGOING).with(K1, equalTo("V1")).with(K2, equalTo("V2"))));
            assertEquals(0 * factor, counter.count(one, wildcard(ONE, OUTGOING).with(K1, equalTo("V1")).with(K2, equalTo("V1"))));
            assertEquals(0 * factor, counter.count(one, literal(ONE, OUTGOING).with(K1, equalTo("V1")).with(K2, equalTo("V1"))));

            assertEquals(1 * factor, counter.count(one, wildcard(ONE, OUTGOING).with(WEIGHT, equalTo(2)).with(K1, equalTo("V1"))));
            assertEquals(0 * factor, counter.count(one, literal(ONE, OUTGOING).with(WEIGHT, equalTo(2)).with(K1, equalTo("V1"))));
            assertEquals(1 * factor, counter.count(one, wildcard(ONE, OUTGOING).with(WEIGHT, equalTo(1)).with(K1, equalTo("V1"))));
            assertEquals(1 * factor, counter.count(one, literal(ONE, OUTGOING).with(WEIGHT, equalTo(1)).with(K1, equalTo("V1"))));

            //Node one both

            assertEquals(10 * factor, counter.count(one, wildcard(ONE, BOTH)));
            assertEquals(0 * factor, counter.count(one, literal(ONE, BOTH)));

            assertEquals(2 * factor, counter.count(one, wildcard(ONE, BOTH).with(WEIGHT, equalTo(1))));
            assertEquals(1 * factor, counter.count(one, literal(ONE, BOTH).with(WEIGHT, equalTo(1))));
            assertEquals(2 * factor, counter.count(one, wildcard(ONE, BOTH).with(WEIGHT, equalTo(2))));
            assertEquals(0 * factor, counter.count(one, literal(ONE, BOTH).with(WEIGHT, equalTo(2))));
            assertEquals(0 * factor, counter.count(one, wildcard(ONE, BOTH).with(WEIGHT, equalTo(3))));
            assertEquals(0 * factor, counter.count(one, literal(ONE, BOTH).with(WEIGHT, equalTo(3))));
            assertEquals(0 * factor, counter.count(one, wildcard(ONE, BOTH).with(WEIGHT, equalTo(4))));
            assertEquals(0 * factor, counter.count(one, literal(ONE, BOTH).with(WEIGHT, equalTo(4))));
            assertEquals(0 * factor, counter.count(one, wildcard(ONE, BOTH).with(WEIGHT, equalTo(5))));
            assertEquals(0 * factor, counter.count(one, literal(ONE, BOTH).with(WEIGHT, equalTo(5))));
            assertEquals(2 * factor, counter.count(one, wildcard(ONE, BOTH).with(WEIGHT, equalTo(7))));
            assertEquals(2 * factor, counter.count(one, literal(ONE, BOTH).with(WEIGHT, equalTo(7))));

            assertEquals(7 * factor, counter.count(one, wildcard(ONE, BOTH).with(K1, equalTo("V1"))));
            assertEquals(3 * factor, counter.count(one, literal(ONE, BOTH).with(K1, equalTo("V1"))));
            assertEquals(0 * factor, counter.count(one, wildcard(ONE, BOTH).with(K1, equalTo("V2"))));
            assertEquals(0 * factor, counter.count(one, literal(ONE, BOTH).with(K1, equalTo("V2"))));

            assertEquals(0 * factor, counter.count(one, wildcard(ONE, BOTH).with(K2, equalTo("V1"))));
            assertEquals(0 * factor, counter.count(one, literal(ONE, BOTH).with(K2, equalTo("V1"))));
            assertEquals(1 * factor, counter.count(one, wildcard(ONE, BOTH).with(K2, equalTo("V2"))));
            assertEquals(0 * factor, counter.count(one, literal(ONE, BOTH).with(K2, equalTo("V2"))));

            assertEquals(1 * factor, counter.count(one, wildcard(ONE, BOTH).with(K1, equalTo("V1")).with(K2, equalTo("V2"))));
            assertEquals(1 * factor, counter.count(one, literal(ONE, BOTH).with(K1, equalTo("V1")).with(K2, equalTo("V2"))));
            assertEquals(0 * factor, counter.count(one, wildcard(ONE, BOTH).with(K1, equalTo("V1")).with(K2, equalTo("V1"))));
            assertEquals(0 * factor, counter.count(one, literal(ONE, BOTH).with(K1, equalTo("V1")).with(K2, equalTo("V1"))));

            assertEquals(2 * factor, counter.count(one, wildcard(ONE, BOTH).with(WEIGHT, equalTo(2)).with(K1, equalTo("V1"))));
            assertEquals(1 * factor, counter.count(one, literal(ONE, BOTH).with(WEIGHT, equalTo(2)).with(K1, equalTo("V1"))));
            assertEquals(1 * factor, counter.count(one, wildcard(ONE, BOTH).with(WEIGHT, equalTo(1)).with(K1, equalTo("V1"))));
            assertEquals(1 * factor, counter.count(one, literal(ONE, BOTH).with(WEIGHT, equalTo(1)).with(K1, equalTo("V1"))));
            assertEquals(0 * factor, counter.count(one, wildcard(ONE, BOTH).with(WEIGHT, equalTo(2)).with(K2, equalTo("V2"))));
            assertEquals(0 * factor, counter.count(one, literal(ONE, BOTH).with(WEIGHT, equalTo(2)).with(K2, equalTo("V2"))));

            //Node two outgoing

            assertEquals(2 * factor, counter.count(two, wildcard(ONE, OUTGOING)));
            assertEquals(0 * factor, counter.count(two, literal(ONE, OUTGOING)));

            assertEquals(0 * factor, counter.count(two, wildcard(ONE, OUTGOING).with(WEIGHT, equalTo(1))));
            assertEquals(0 * factor, counter.count(two, literal(ONE, OUTGOING).with(WEIGHT, equalTo(1))));
            assertEquals(1 * factor, counter.count(two, wildcard(ONE, OUTGOING).with(WEIGHT, equalTo(2))));
            assertEquals(0 * factor, counter.count(two, literal(ONE, OUTGOING).with(WEIGHT, equalTo(2))));
            assertEquals(0 * factor, counter.count(two, wildcard(ONE, OUTGOING).with(WEIGHT, equalTo(3))));
            assertEquals(0 * factor, counter.count(two, literal(ONE, OUTGOING).with(WEIGHT, equalTo(3))));
            assertEquals(0 * factor, counter.count(two, wildcard(ONE, OUTGOING).with(WEIGHT, equalTo(4))));
            assertEquals(0 * factor, counter.count(two, literal(ONE, OUTGOING).with(WEIGHT, equalTo(4))));
            assertEquals(0 * factor, counter.count(two, wildcard(ONE, OUTGOING).with(WEIGHT, equalTo(5))));
            assertEquals(0 * factor, counter.count(two, literal(ONE, OUTGOING).with(WEIGHT, equalTo(5))));
            assertEquals(0 * factor, counter.count(two, wildcard(ONE, OUTGOING).with(WEIGHT, equalTo(7))));
            assertEquals(0 * factor, counter.count(two, literal(ONE, OUTGOING).with(WEIGHT, equalTo(7))));

            assertEquals(2 * factor, counter.count(two, wildcard(ONE, OUTGOING).with(K1, equalTo("V1"))));
            assertEquals(0 * factor, counter.count(two, literal(ONE, OUTGOING).with(K1, equalTo("V1"))));
            assertEquals(0 * factor, counter.count(two, wildcard(ONE, OUTGOING).with(K1, equalTo("V2"))));
            assertEquals(0 * factor, counter.count(two, literal(ONE, OUTGOING).with(K1, equalTo("V2"))));

            assertEquals(0 * factor, counter.count(two, wildcard(ONE, OUTGOING).with(K2, equalTo("V1"))));
            assertEquals(0 * factor, counter.count(two, literal(ONE, OUTGOING).with(K2, equalTo("V1"))));
            assertEquals(1 * factor, counter.count(two, wildcard(ONE, OUTGOING).with(K2, equalTo("V2"))));
            assertEquals(0 * factor, counter.count(two, literal(ONE, OUTGOING).with(K2, equalTo("V2"))));

            assertEquals(1 * factor, counter.count(two, wildcard(ONE, OUTGOING).with(K1, equalTo("V1")).with(K2, equalTo("V2"))));
            assertEquals(1 * factor, counter.count(two, literal(ONE, OUTGOING).with(K1, equalTo("V1")).with(K2, equalTo("V2"))));
            assertEquals(0 * factor, counter.count(two, wildcard(ONE, OUTGOING).with(K1, equalTo("V1")).with(K2, equalTo("V1"))));
            assertEquals(0 * factor, counter.count(two, literal(ONE, OUTGOING).with(K1, equalTo("V1")).with(K2, equalTo("V1"))));

            assertEquals(1 * factor, counter.count(two, wildcard(ONE, OUTGOING).with(WEIGHT, equalTo(2)).with(K1, equalTo("V1"))));
            assertEquals(1 * factor, counter.count(two, literal(ONE, OUTGOING).with(WEIGHT, equalTo(2)).with(K1, equalTo("V1"))));
            assertEquals(0 * factor, counter.count(two, wildcard(ONE, OUTGOING).with(WEIGHT, equalTo(2)).with(K2, equalTo("V2"))));
            assertEquals(0 * factor, counter.count(two, literal(ONE, OUTGOING).with(WEIGHT, equalTo(2)).with(K2, equalTo("V2"))));

            //Node two incoming

            assertEquals(6 * factor, counter.count(two, wildcard(ONE, INCOMING)));
            assertEquals(0 * factor, counter.count(two, literal(ONE, INCOMING)));

            assertEquals(2 * factor, counter.count(two, wildcard(ONE, INCOMING).with(WEIGHT, equalTo(1))));
            assertEquals(1 * factor, counter.count(two, literal(ONE, INCOMING).with(WEIGHT, equalTo(1))));
            assertEquals(1 * factor, counter.count(two, wildcard(ONE, INCOMING).with(WEIGHT, equalTo(2))));
            assertEquals(0 * factor, counter.count(two, literal(ONE, INCOMING).with(WEIGHT, equalTo(2))));
            assertEquals(0 * factor, counter.count(two, wildcard(ONE, INCOMING).with(WEIGHT, equalTo(3))));
            assertEquals(0 * factor, counter.count(two, literal(ONE, INCOMING).with(WEIGHT, equalTo(3))));
            assertEquals(0 * factor, counter.count(two, wildcard(ONE, INCOMING).with(WEIGHT, equalTo(4))));
            assertEquals(0 * factor, counter.count(two, literal(ONE, INCOMING).with(WEIGHT, equalTo(4))));
            assertEquals(0 * factor, counter.count(two, wildcard(ONE, INCOMING).with(WEIGHT, equalTo(5))));
            assertEquals(0 * factor, counter.count(two, literal(ONE, INCOMING).with(WEIGHT, equalTo(5))));
            assertEquals(0 * factor, counter.count(two, wildcard(ONE, INCOMING).with(WEIGHT, equalTo(7))));
            assertEquals(0 * factor, counter.count(two, literal(ONE, INCOMING).with(WEIGHT, equalTo(7))));

            assertEquals(5 * factor, counter.count(two, wildcard(ONE, INCOMING).with(K1, equalTo("V1"))));
            assertEquals(3 * factor, counter.count(two, literal(ONE, INCOMING).with(K1, equalTo("V1"))));
            assertEquals(0 * factor, counter.count(two, wildcard(ONE, INCOMING).with(K1, equalTo("V2"))));
            assertEquals(0 * factor, counter.count(two, literal(ONE, INCOMING).with(K1, equalTo("V2"))));

            assertEquals(0 * factor, counter.count(two, wildcard(ONE, INCOMING).with(K2, equalTo("V1"))));
            assertEquals(0 * factor, counter.count(two, literal(ONE, INCOMING).with(K2, equalTo("V1"))));
            assertEquals(0 * factor, counter.count(two, wildcard(ONE, INCOMING).with(K2, equalTo("V2"))));
            assertEquals(0 * factor, counter.count(two, literal(ONE, INCOMING).with(K2, equalTo("V2"))));

            assertEquals(0 * factor, counter.count(two, wildcard(ONE, INCOMING).with(K1, equalTo("V1")).with(K2, equalTo("V2"))));
            assertEquals(0 * factor, counter.count(two, literal(ONE, INCOMING).with(K1, equalTo("V1")).with(K2, equalTo("V2"))));
            assertEquals(0 * factor, counter.count(two, wildcard(ONE, INCOMING).with(K1, equalTo("V1")).with(K2, equalTo("V1"))));
            assertEquals(0 * factor, counter.count(two, literal(ONE, INCOMING).with(K1, equalTo("V1")).with(K2, equalTo("V1"))));

            assertEquals(1 * factor, counter.count(two, wildcard(ONE, INCOMING).with(WEIGHT, equalTo(2)).with(K1, equalTo("V1"))));
            assertEquals(0 * factor, counter.count(two, literal(ONE, INCOMING).with(WEIGHT, equalTo(2)).with(K1, equalTo("V1"))));
            assertEquals(1 * factor, counter.count(two, wildcard(ONE, INCOMING).with(WEIGHT, equalTo(1)).with(K1, equalTo("V1"))));
            assertEquals(1 * factor, counter.count(two, literal(ONE, INCOMING).with(WEIGHT, equalTo(1)).with(K1, equalTo("V1"))));

            //Node two both

            assertEquals(8 * factor, counter.count(two, wildcard(ONE, BOTH)));
            assertEquals(0 * factor, counter.count(two, literal(ONE, BOTH)));

            assertEquals(2 * factor, counter.count(two, wildcard(ONE, BOTH).with(WEIGHT, equalTo(1))));
            assertEquals(1 * factor, counter.count(two, literal(ONE, BOTH).with(WEIGHT, equalTo(1))));
            assertEquals(2 * factor, counter.count(two, wildcard(ONE, BOTH).with(WEIGHT, equalTo(2))));
            assertEquals(0 * factor, counter.count(two, literal(ONE, BOTH).with(WEIGHT, equalTo(2))));
            assertEquals(0 * factor, counter.count(two, wildcard(ONE, BOTH).with(WEIGHT, equalTo(3))));
            assertEquals(0 * factor, counter.count(two, literal(ONE, BOTH).with(WEIGHT, equalTo(3))));
            assertEquals(0 * factor, counter.count(two, wildcard(ONE, BOTH).with(WEIGHT, equalTo(4))));
            assertEquals(0 * factor, counter.count(two, literal(ONE, BOTH).with(WEIGHT, equalTo(4))));
            assertEquals(0 * factor, counter.count(two, wildcard(ONE, BOTH).with(WEIGHT, equalTo(5))));
            assertEquals(0 * factor, counter.count(two, literal(ONE, BOTH).with(WEIGHT, equalTo(5))));
            assertEquals(0 * factor, counter.count(two, wildcard(ONE, BOTH).with(WEIGHT, equalTo(7))));
            assertEquals(0 * factor, counter.count(two, literal(ONE, BOTH).with(WEIGHT, equalTo(7))));

            assertEquals(7 * factor, counter.count(two, wildcard(ONE, BOTH).with(K1, equalTo("V1"))));
            assertEquals(3 * factor, counter.count(two, literal(ONE, BOTH).with(K1, equalTo("V1"))));
            assertEquals(0 * factor, counter.count(two, wildcard(ONE, BOTH).with(K1, equalTo("V2"))));
            assertEquals(0 * factor, counter.count(two, literal(ONE, BOTH).with(K1, equalTo("V2"))));

            assertEquals(0 * factor, counter.count(two, wildcard(ONE, BOTH).with(K2, equalTo("V1"))));
            assertEquals(0 * factor, counter.count(two, literal(ONE, BOTH).with(K2, equalTo("V1"))));
            assertEquals(1 * factor, counter.count(two, wildcard(ONE, BOTH).with(K2, equalTo("V2"))));
            assertEquals(0 * factor, counter.count(two, literal(ONE, BOTH).with(K2, equalTo("V2"))));

            assertEquals(1 * factor, counter.count(two, wildcard(ONE, BOTH).with(K1, equalTo("V1")).with(K2, equalTo("V2"))));
            assertEquals(1 * factor, counter.count(two, literal(ONE, BOTH).with(K1, equalTo("V1")).with(K2, equalTo("V2"))));
            assertEquals(0 * factor, counter.count(two, wildcard(ONE, BOTH).with(K1, equalTo("V1")).with(K2, equalTo("V1"))));
            assertEquals(0 * factor, counter.count(two, literal(ONE, BOTH).with(K1, equalTo("V1")).with(K2, equalTo("V1"))));

            assertEquals(2 * factor, counter.count(two, wildcard(ONE, BOTH).with(WEIGHT, equalTo(2)).with(K1, equalTo("V1"))));
            assertEquals(1 * factor, counter.count(two, literal(ONE, BOTH).with(WEIGHT, equalTo(2)).with(K1, equalTo("V1"))));
            assertEquals(1 * factor, counter.count(two, wildcard(ONE, BOTH).with(WEIGHT, equalTo(1)).with(K1, equalTo("V1"))));
            assertEquals(1 * factor, counter.count(two, literal(ONE, BOTH).with(WEIGHT, equalTo(1)).with(K1, equalTo("V1"))));
            assertEquals(0 * factor, counter.count(two, wildcard(ONE, BOTH).with(WEIGHT, equalTo(2)).with(K2, equalTo("V2"))));
            assertEquals(0 * factor, counter.count(two, literal(ONE, BOTH).with(WEIGHT, equalTo(2)).with(K2, equalTo("V2"))));


            tx.success();
        }
    }

    private void verifyWeightedCounts(int factor, RelationshipCounter counter) {
        try (Transaction tx = database.beginTx()) {

            Node one = database.getNodeById(1);
            Node two = database.getNodeById(2);

            //Node one incoming

            assertEquals(10 * factor, counter.count(one, wildcard(ONE, INCOMING)));
            assertEquals(0 * factor, counter.count(one, literal(ONE, INCOMING)));

            assertEquals(0 * factor, counter.count(one, wildcard(ONE, INCOMING).with(WEIGHT, equalTo(1))));
            assertEquals(0 * factor, counter.count(one, literal(ONE, INCOMING).with(WEIGHT, equalTo(1))));
            assertEquals(2 * factor, counter.count(one, wildcard(ONE, INCOMING).with(WEIGHT, equalTo(2))));
            assertEquals(0 * factor, counter.count(one, literal(ONE, INCOMING).with(WEIGHT, equalTo(2))));
            assertEquals(0 * factor, counter.count(one, wildcard(ONE, INCOMING).with(WEIGHT, equalTo(3))));
            assertEquals(0 * factor, counter.count(one, literal(ONE, INCOMING).with(WEIGHT, equalTo(3))));
            assertEquals(0 * factor, counter.count(one, wildcard(ONE, INCOMING).with(WEIGHT, equalTo(4))));
            assertEquals(0 * factor, counter.count(one, literal(ONE, INCOMING).with(WEIGHT, equalTo(4))));
            assertEquals(0 * factor, counter.count(one, wildcard(ONE, INCOMING).with(WEIGHT, equalTo(5))));
            assertEquals(0 * factor, counter.count(one, literal(ONE, INCOMING).with(WEIGHT, equalTo(5))));
            assertEquals(7 * factor, counter.count(one, wildcard(ONE, INCOMING).with(WEIGHT, equalTo(7))));
            assertEquals(7 * factor, counter.count(one, literal(ONE, INCOMING).with(WEIGHT, equalTo(7))));

            assertEquals(3 * factor, counter.count(one, wildcard(ONE, INCOMING).with(K1, equalTo("V1"))));
            assertEquals(0 * factor, counter.count(one, literal(ONE, INCOMING).with(K1, equalTo("V1"))));
            assertEquals(0 * factor, counter.count(one, wildcard(ONE, INCOMING).with(K1, equalTo("V2"))));
            assertEquals(0 * factor, counter.count(one, literal(ONE, INCOMING).with(K1, equalTo("V2"))));

            assertEquals(0 * factor, counter.count(one, wildcard(ONE, INCOMING).with(K2, equalTo("V1"))));
            assertEquals(0 * factor, counter.count(one, literal(ONE, INCOMING).with(K2, equalTo("V1"))));
            assertEquals(1 * factor, counter.count(one, wildcard(ONE, INCOMING).with(K2, equalTo("V2"))));
            assertEquals(0 * factor, counter.count(one, literal(ONE, INCOMING).with(K2, equalTo("V2"))));

            assertEquals(1 * factor, counter.count(one, wildcard(ONE, INCOMING).with(K1, equalTo("V1")).with(K2, equalTo("V2"))));
            assertEquals(1 * factor, counter.count(one, literal(ONE, INCOMING).with(K1, equalTo("V1")).with(K2, equalTo("V2"))));
            assertEquals(0 * factor, counter.count(one, wildcard(ONE, INCOMING).with(K1, equalTo("V1")).with(K2, equalTo("V1"))));
            assertEquals(0 * factor, counter.count(one, literal(ONE, INCOMING).with(K1, equalTo("V1")).with(K2, equalTo("V1"))));

            assertEquals(2 * factor, counter.count(one, wildcard(ONE, INCOMING).with(WEIGHT, equalTo(2)).with(K1, equalTo("V1"))));
            assertEquals(2 * factor, counter.count(one, literal(ONE, INCOMING).with(WEIGHT, equalTo(2)).with(K1, equalTo("V1"))));
            assertEquals(0 * factor, counter.count(one, wildcard(ONE, INCOMING).with(WEIGHT, equalTo(2)).with(K2, equalTo("V2"))));
            assertEquals(0 * factor, counter.count(one, literal(ONE, INCOMING).with(WEIGHT, equalTo(2)).with(K2, equalTo("V2"))));

            //Node one outgoing

            assertEquals(14 * factor, counter.count(one, wildcard(ONE, OUTGOING)));
            assertEquals(0 * factor, counter.count(one, literal(ONE, OUTGOING)));

            assertEquals(1 * factor, counter.count(one, wildcard(TWO, OUTGOING)));

            assertEquals(2 * factor, counter.count(one, wildcard(ONE, OUTGOING).with(WEIGHT, equalTo(1))));
            assertEquals(1 * factor, counter.count(one, literal(ONE, OUTGOING).with(WEIGHT, equalTo(1))));
            assertEquals(2 * factor, counter.count(one, wildcard(ONE, OUTGOING).with(WEIGHT, equalTo(2))));
            assertEquals(0 * factor, counter.count(one, literal(ONE, OUTGOING).with(WEIGHT, equalTo(2))));
            assertEquals(0 * factor, counter.count(one, wildcard(ONE, OUTGOING).with(WEIGHT, equalTo(3))));
            assertEquals(0 * factor, counter.count(one, literal(ONE, OUTGOING).with(WEIGHT, equalTo(3))));
            assertEquals(0 * factor, counter.count(one, wildcard(ONE, OUTGOING).with(WEIGHT, equalTo(4))));
            assertEquals(0 * factor, counter.count(one, literal(ONE, OUTGOING).with(WEIGHT, equalTo(4))));
            assertEquals(0 * factor, counter.count(one, wildcard(ONE, OUTGOING).with(WEIGHT, equalTo(5))));
            assertEquals(0 * factor, counter.count(one, literal(ONE, OUTGOING).with(WEIGHT, equalTo(5))));
            assertEquals(7 * factor, counter.count(one, wildcard(ONE, OUTGOING).with(WEIGHT, equalTo(7))));
            assertEquals(7 * factor, counter.count(one, literal(ONE, OUTGOING).with(WEIGHT, equalTo(7))));

            assertEquals(6 * factor, counter.count(one, wildcard(ONE, OUTGOING).with(K1, equalTo("V1"))));
            assertEquals(3 * factor, counter.count(one, literal(ONE, OUTGOING).with(K1, equalTo("V1"))));
            assertEquals(0 * factor, counter.count(one, wildcard(ONE, OUTGOING).with(K1, equalTo("V2"))));
            assertEquals(0 * factor, counter.count(one, literal(ONE, OUTGOING).with(K1, equalTo("V2"))));

            assertEquals(0 * factor, counter.count(one, wildcard(ONE, OUTGOING).with(K2, equalTo("V1"))));
            assertEquals(0 * factor, counter.count(one, literal(ONE, OUTGOING).with(K2, equalTo("V1"))));
            assertEquals(0 * factor, counter.count(one, wildcard(ONE, OUTGOING).with(K2, equalTo("V2"))));
            assertEquals(0 * factor, counter.count(one, literal(ONE, OUTGOING).with(K2, equalTo("V2"))));

            assertEquals(0 * factor, counter.count(one, wildcard(ONE, OUTGOING).with(K1, equalTo("V1")).with(K2, equalTo("V2"))));
            assertEquals(0 * factor, counter.count(one, literal(ONE, OUTGOING).with(K1, equalTo("V1")).with(K2, equalTo("V2"))));
            assertEquals(0 * factor, counter.count(one, wildcard(ONE, OUTGOING).with(K1, equalTo("V1")).with(K2, equalTo("V1"))));
            assertEquals(0 * factor, counter.count(one, literal(ONE, OUTGOING).with(K1, equalTo("V1")).with(K2, equalTo("V1"))));

            assertEquals(2 * factor, counter.count(one, wildcard(ONE, OUTGOING).with(WEIGHT, equalTo(2)).with(K1, equalTo("V1"))));
            assertEquals(0 * factor, counter.count(one, literal(ONE, OUTGOING).with(WEIGHT, equalTo(2)).with(K1, equalTo("V1"))));
            assertEquals(1 * factor, counter.count(one, wildcard(ONE, OUTGOING).with(WEIGHT, equalTo(1)).with(K1, equalTo("V1"))));
            assertEquals(1 * factor, counter.count(one, literal(ONE, OUTGOING).with(WEIGHT, equalTo(1)).with(K1, equalTo("V1"))));

            //Node one both

            assertEquals(24 * factor, counter.count(one, wildcard(ONE, BOTH)));
            assertEquals(0 * factor, counter.count(one, literal(ONE, BOTH)));

            assertEquals(2 * factor, counter.count(one, wildcard(ONE, BOTH).with(WEIGHT, equalTo(1))));
            assertEquals(1 * factor, counter.count(one, literal(ONE, BOTH).with(WEIGHT, equalTo(1))));
            assertEquals(4 * factor, counter.count(one, wildcard(ONE, BOTH).with(WEIGHT, equalTo(2))));
            assertEquals(0 * factor, counter.count(one, literal(ONE, BOTH).with(WEIGHT, equalTo(2))));
            assertEquals(0 * factor, counter.count(one, wildcard(ONE, BOTH).with(WEIGHT, equalTo(3))));
            assertEquals(0 * factor, counter.count(one, literal(ONE, BOTH).with(WEIGHT, equalTo(3))));
            assertEquals(0 * factor, counter.count(one, wildcard(ONE, BOTH).with(WEIGHT, equalTo(4))));
            assertEquals(0 * factor, counter.count(one, literal(ONE, BOTH).with(WEIGHT, equalTo(4))));
            assertEquals(0 * factor, counter.count(one, wildcard(ONE, BOTH).with(WEIGHT, equalTo(5))));
            assertEquals(0 * factor, counter.count(one, literal(ONE, BOTH).with(WEIGHT, equalTo(5))));
            assertEquals(14 * factor, counter.count(one, wildcard(ONE, BOTH).with(WEIGHT, equalTo(7))));
            assertEquals(14 * factor, counter.count(one, literal(ONE, BOTH).with(WEIGHT, equalTo(7))));

            assertEquals(9 * factor, counter.count(one, wildcard(ONE, BOTH).with(K1, equalTo("V1"))));
            assertEquals(3 * factor, counter.count(one, literal(ONE, BOTH).with(K1, equalTo("V1"))));
            assertEquals(0 * factor, counter.count(one, wildcard(ONE, BOTH).with(K1, equalTo("V2"))));
            assertEquals(0 * factor, counter.count(one, literal(ONE, BOTH).with(K1, equalTo("V2"))));

            assertEquals(0 * factor, counter.count(one, wildcard(ONE, BOTH).with(K2, equalTo("V1"))));
            assertEquals(0 * factor, counter.count(one, literal(ONE, BOTH).with(K2, equalTo("V1"))));
            assertEquals(1 * factor, counter.count(one, wildcard(ONE, BOTH).with(K2, equalTo("V2"))));
            assertEquals(0 * factor, counter.count(one, literal(ONE, BOTH).with(K2, equalTo("V2"))));

            assertEquals(1 * factor, counter.count(one, wildcard(ONE, BOTH).with(K1, equalTo("V1")).with(K2, equalTo("V2"))));
            assertEquals(1 * factor, counter.count(one, literal(ONE, BOTH).with(K1, equalTo("V1")).with(K2, equalTo("V2"))));
            assertEquals(0 * factor, counter.count(one, wildcard(ONE, BOTH).with(K1, equalTo("V1")).with(K2, equalTo("V1"))));
            assertEquals(0 * factor, counter.count(one, literal(ONE, BOTH).with(K1, equalTo("V1")).with(K2, equalTo("V1"))));

            assertEquals(4 * factor, counter.count(one, wildcard(ONE, BOTH).with(WEIGHT, equalTo(2)).with(K1, equalTo("V1"))));
            assertEquals(2 * factor, counter.count(one, literal(ONE, BOTH).with(WEIGHT, equalTo(2)).with(K1, equalTo("V1"))));
            assertEquals(1 * factor, counter.count(one, wildcard(ONE, BOTH).with(WEIGHT, equalTo(1)).with(K1, equalTo("V1"))));
            assertEquals(1 * factor, counter.count(one, literal(ONE, BOTH).with(WEIGHT, equalTo(1)).with(K1, equalTo("V1"))));
            assertEquals(0 * factor, counter.count(one, wildcard(ONE, BOTH).with(WEIGHT, equalTo(2)).with(K2, equalTo("V2"))));
            assertEquals(0 * factor, counter.count(one, literal(ONE, BOTH).with(WEIGHT, equalTo(2)).with(K2, equalTo("V2"))));

            //Node two outgoing

            assertEquals(3 * factor, counter.count(two, wildcard(ONE, OUTGOING)));
            assertEquals(0 * factor, counter.count(two, literal(ONE, OUTGOING)));

            assertEquals(0 * factor, counter.count(two, wildcard(ONE, OUTGOING).with(WEIGHT, equalTo(1))));
            assertEquals(0 * factor, counter.count(two, literal(ONE, OUTGOING).with(WEIGHT, equalTo(1))));
            assertEquals(2 * factor, counter.count(two, wildcard(ONE, OUTGOING).with(WEIGHT, equalTo(2))));
            assertEquals(0 * factor, counter.count(two, literal(ONE, OUTGOING).with(WEIGHT, equalTo(2))));
            assertEquals(0 * factor, counter.count(two, wildcard(ONE, OUTGOING).with(WEIGHT, equalTo(3))));
            assertEquals(0 * factor, counter.count(two, literal(ONE, OUTGOING).with(WEIGHT, equalTo(3))));
            assertEquals(0 * factor, counter.count(two, wildcard(ONE, OUTGOING).with(WEIGHT, equalTo(4))));
            assertEquals(0 * factor, counter.count(two, literal(ONE, OUTGOING).with(WEIGHT, equalTo(4))));
            assertEquals(0 * factor, counter.count(two, wildcard(ONE, OUTGOING).with(WEIGHT, equalTo(5))));
            assertEquals(0 * factor, counter.count(two, literal(ONE, OUTGOING).with(WEIGHT, equalTo(5))));
            assertEquals(0 * factor, counter.count(two, wildcard(ONE, OUTGOING).with(WEIGHT, equalTo(7))));
            assertEquals(0 * factor, counter.count(two, literal(ONE, OUTGOING).with(WEIGHT, equalTo(7))));

            assertEquals(3 * factor, counter.count(two, wildcard(ONE, OUTGOING).with(K1, equalTo("V1"))));
            assertEquals(0 * factor, counter.count(two, literal(ONE, OUTGOING).with(K1, equalTo("V1"))));
            assertEquals(0 * factor, counter.count(two, wildcard(ONE, OUTGOING).with(K1, equalTo("V2"))));
            assertEquals(0 * factor, counter.count(two, literal(ONE, OUTGOING).with(K1, equalTo("V2"))));

            assertEquals(0 * factor, counter.count(two, wildcard(ONE, OUTGOING).with(K2, equalTo("V1"))));
            assertEquals(0 * factor, counter.count(two, literal(ONE, OUTGOING).with(K2, equalTo("V1"))));
            assertEquals(1 * factor, counter.count(two, wildcard(ONE, OUTGOING).with(K2, equalTo("V2"))));
            assertEquals(0 * factor, counter.count(two, literal(ONE, OUTGOING).with(K2, equalTo("V2"))));

            assertEquals(1 * factor, counter.count(two, wildcard(ONE, OUTGOING).with(K1, equalTo("V1")).with(K2, equalTo("V2"))));
            assertEquals(1 * factor, counter.count(two, literal(ONE, OUTGOING).with(K1, equalTo("V1")).with(K2, equalTo("V2"))));
            assertEquals(0 * factor, counter.count(two, wildcard(ONE, OUTGOING).with(K1, equalTo("V1")).with(K2, equalTo("V1"))));
            assertEquals(0 * factor, counter.count(two, literal(ONE, OUTGOING).with(K1, equalTo("V1")).with(K2, equalTo("V1"))));

            assertEquals(2 * factor, counter.count(two, wildcard(ONE, OUTGOING).with(WEIGHT, equalTo(2)).with(K1, equalTo("V1"))));
            assertEquals(2 * factor, counter.count(two, literal(ONE, OUTGOING).with(WEIGHT, equalTo(2)).with(K1, equalTo("V1"))));
            assertEquals(0 * factor, counter.count(two, wildcard(ONE, OUTGOING).with(WEIGHT, equalTo(2)).with(K2, equalTo("V2"))));
            assertEquals(0 * factor, counter.count(two, literal(ONE, OUTGOING).with(WEIGHT, equalTo(2)).with(K2, equalTo("V2"))));

            //Node two incoming

            assertEquals(7 * factor, counter.count(two, wildcard(ONE, INCOMING)));
            assertEquals(0 * factor, counter.count(two, literal(ONE, INCOMING)));

            assertEquals(2 * factor, counter.count(two, wildcard(ONE, INCOMING).with(WEIGHT, equalTo(1))));
            assertEquals(1 * factor, counter.count(two, literal(ONE, INCOMING).with(WEIGHT, equalTo(1))));
            assertEquals(2 * factor, counter.count(two, wildcard(ONE, INCOMING).with(WEIGHT, equalTo(2))));
            assertEquals(0 * factor, counter.count(two, literal(ONE, INCOMING).with(WEIGHT, equalTo(2))));
            assertEquals(0 * factor, counter.count(two, wildcard(ONE, INCOMING).with(WEIGHT, equalTo(3))));
            assertEquals(0 * factor, counter.count(two, literal(ONE, INCOMING).with(WEIGHT, equalTo(3))));
            assertEquals(0 * factor, counter.count(two, wildcard(ONE, INCOMING).with(WEIGHT, equalTo(4))));
            assertEquals(0 * factor, counter.count(two, literal(ONE, INCOMING).with(WEIGHT, equalTo(4))));
            assertEquals(0 * factor, counter.count(two, wildcard(ONE, INCOMING).with(WEIGHT, equalTo(5))));
            assertEquals(0 * factor, counter.count(two, literal(ONE, INCOMING).with(WEIGHT, equalTo(5))));
            assertEquals(0 * factor, counter.count(two, wildcard(ONE, INCOMING).with(WEIGHT, equalTo(7))));
            assertEquals(0 * factor, counter.count(two, literal(ONE, INCOMING).with(WEIGHT, equalTo(7))));

            assertEquals(6 * factor, counter.count(two, wildcard(ONE, INCOMING).with(K1, equalTo("V1"))));
            assertEquals(3 * factor, counter.count(two, literal(ONE, INCOMING).with(K1, equalTo("V1"))));
            assertEquals(0 * factor, counter.count(two, wildcard(ONE, INCOMING).with(K1, equalTo("V2"))));
            assertEquals(0 * factor, counter.count(two, literal(ONE, INCOMING).with(K1, equalTo("V2"))));

            assertEquals(0 * factor, counter.count(two, wildcard(ONE, INCOMING).with(K2, equalTo("V1"))));
            assertEquals(0 * factor, counter.count(two, literal(ONE, INCOMING).with(K2, equalTo("V1"))));
            assertEquals(0 * factor, counter.count(two, wildcard(ONE, INCOMING).with(K2, equalTo("V2"))));
            assertEquals(0 * factor, counter.count(two, literal(ONE, INCOMING).with(K2, equalTo("V2"))));

            assertEquals(0 * factor, counter.count(two, wildcard(ONE, INCOMING).with(K1, equalTo("V1")).with(K2, equalTo("V2"))));
            assertEquals(0 * factor, counter.count(two, literal(ONE, INCOMING).with(K1, equalTo("V1")).with(K2, equalTo("V2"))));
            assertEquals(0 * factor, counter.count(two, wildcard(ONE, INCOMING).with(K1, equalTo("V1")).with(K2, equalTo("V1"))));
            assertEquals(0 * factor, counter.count(two, literal(ONE, INCOMING).with(K1, equalTo("V1")).with(K2, equalTo("V1"))));

            assertEquals(2 * factor, counter.count(two, wildcard(ONE, INCOMING).with(WEIGHT, equalTo(2)).with(K1, equalTo("V1"))));
            assertEquals(0 * factor, counter.count(two, literal(ONE, INCOMING).with(WEIGHT, equalTo(2)).with(K1, equalTo("V1"))));
            assertEquals(1 * factor, counter.count(two, wildcard(ONE, INCOMING).with(WEIGHT, equalTo(1)).with(K1, equalTo("V1"))));
            assertEquals(1 * factor, counter.count(two, literal(ONE, INCOMING).with(WEIGHT, equalTo(1)).with(K1, equalTo("V1"))));

            //Node two both

            assertEquals(10 * factor, counter.count(two, wildcard(ONE, BOTH)));
            assertEquals(0 * factor, counter.count(two, literal(ONE, BOTH)));

            assertEquals(2 * factor, counter.count(two, wildcard(ONE, BOTH).with(WEIGHT, equalTo(1))));
            assertEquals(1 * factor, counter.count(two, literal(ONE, BOTH).with(WEIGHT, equalTo(1))));
            assertEquals(4 * factor, counter.count(two, wildcard(ONE, BOTH).with(WEIGHT, equalTo(2))));
            assertEquals(0 * factor, counter.count(two, literal(ONE, BOTH).with(WEIGHT, equalTo(2))));
            assertEquals(0 * factor, counter.count(two, wildcard(ONE, BOTH).with(WEIGHT, equalTo(3))));
            assertEquals(0 * factor, counter.count(two, literal(ONE, BOTH).with(WEIGHT, equalTo(3))));
            assertEquals(0 * factor, counter.count(two, wildcard(ONE, BOTH).with(WEIGHT, equalTo(4))));
            assertEquals(0 * factor, counter.count(two, literal(ONE, BOTH).with(WEIGHT, equalTo(4))));
            assertEquals(0 * factor, counter.count(two, wildcard(ONE, BOTH).with(WEIGHT, equalTo(5))));
            assertEquals(0 * factor, counter.count(two, literal(ONE, BOTH).with(WEIGHT, equalTo(5))));
            assertEquals(0 * factor, counter.count(two, wildcard(ONE, BOTH).with(WEIGHT, equalTo(7))));
            assertEquals(0 * factor, counter.count(two, literal(ONE, BOTH).with(WEIGHT, equalTo(7))));

            assertEquals(9 * factor, counter.count(two, wildcard(ONE, BOTH).with(K1, equalTo("V1"))));
            assertEquals(3 * factor, counter.count(two, literal(ONE, BOTH).with(K1, equalTo("V1"))));
            assertEquals(0 * factor, counter.count(two, wildcard(ONE, BOTH).with(K1, equalTo("V2"))));
            assertEquals(0 * factor, counter.count(two, literal(ONE, BOTH).with(K1, equalTo("V2"))));

            assertEquals(0 * factor, counter.count(two, wildcard(ONE, BOTH).with(K2, equalTo("V1"))));
            assertEquals(0 * factor, counter.count(two, literal(ONE, BOTH).with(K2, equalTo("V1"))));
            assertEquals(1 * factor, counter.count(two, wildcard(ONE, BOTH).with(K2, equalTo("V2"))));
            assertEquals(0 * factor, counter.count(two, literal(ONE, BOTH).with(K2, equalTo("V2"))));

            assertEquals(1 * factor, counter.count(two, wildcard(ONE, BOTH).with(K1, equalTo("V1")).with(K2, equalTo("V2"))));
            assertEquals(1 * factor, counter.count(two, literal(ONE, BOTH).with(K1, equalTo("V1")).with(K2, equalTo("V2"))));
            assertEquals(0 * factor, counter.count(two, wildcard(ONE, BOTH).with(K1, equalTo("V1")).with(K2, equalTo("V1"))));
            assertEquals(0 * factor, counter.count(two, literal(ONE, BOTH).with(K1, equalTo("V1")).with(K2, equalTo("V1"))));

            assertEquals(4 * factor, counter.count(two, wildcard(ONE, BOTH).with(WEIGHT, equalTo(2)).with(K1, equalTo("V1"))));
            assertEquals(2 * factor, counter.count(two, literal(ONE, BOTH).with(WEIGHT, equalTo(2)).with(K1, equalTo("V1"))));
            assertEquals(1 * factor, counter.count(two, wildcard(ONE, BOTH).with(WEIGHT, equalTo(1)).with(K1, equalTo("V1"))));
            assertEquals(1 * factor, counter.count(two, literal(ONE, BOTH).with(WEIGHT, equalTo(1)).with(K1, equalTo("V1"))));
            assertEquals(0 * factor, counter.count(two, wildcard(ONE, BOTH).with(WEIGHT, equalTo(2)).with(K2, equalTo("V2"))));
            assertEquals(0 * factor, counter.count(two, literal(ONE, BOTH).with(WEIGHT, equalTo(2)).with(K2, equalTo("V2"))));

            tx.success();
        }
    }

    private void verifyCompactedCounts(int factor, RelationshipCounter counter) {
        try (Transaction tx = database.beginTx()) {

            Node one = database.getNodeById(1);

            //Node one incoming

            assertEquals(3 * factor, counter.count(one, wildcard(ONE, INCOMING)));

            try {
                counter.count(one, literal(ONE, INCOMING));
                fail();
            } catch (UnableToCountException e) {
            }

            try {
                counter.count(one, wildcard(ONE, INCOMING).with(WEIGHT, equalTo(1)));
                fail();
            } catch (UnableToCountException e) {
            }

            try {
                counter.count(one, literal(ONE, INCOMING).with(WEIGHT, equalTo(1)));
                fail();
            } catch (UnableToCountException e) {
            }

            try {
                counter.count(one, wildcard(ONE, INCOMING).with(WEIGHT, equalTo(2)));

                fail();
            } catch (UnableToCountException e) {
            }

            try {
                counter.count(one, literal(ONE, INCOMING).with(WEIGHT, equalTo(2)));

                fail();
            } catch (UnableToCountException e) {
            }

            try {
                counter.count(one, wildcard(ONE, INCOMING).with(WEIGHT, equalTo(7)));

                fail();
            } catch (UnableToCountException e) {
            }

            try {
                counter.count(one, literal(ONE, INCOMING).with(WEIGHT, equalTo(7)));
                fail();
            } catch (UnableToCountException e) {
            }

            try {
                counter.count(one, wildcard(ONE, INCOMING).with(K1, equalTo("V1")));
                fail();
            } catch (UnableToCountException e) {
            }

            try {
                counter.count(one, literal(ONE, INCOMING).with(K1, equalTo("V1")));
                fail();
            } catch (UnableToCountException e) {
            }

            try {
                counter.count(one, wildcard(ONE, INCOMING).with(K2, equalTo("V1")));
                fail();
            } catch (UnableToCountException e) {
            }

            try {
                counter.count(one, literal(ONE, INCOMING).with(K2, equalTo("V1")));
                fail();
            } catch (UnableToCountException e) {
            }

            try {
                counter.count(one, wildcard(ONE, INCOMING).with(WEIGHT, equalTo(2)).with(K1, equalTo("V1")));
                fail();
            } catch (UnableToCountException e) {
            }

            try {
                counter.count(one, literal(ONE, INCOMING).with(WEIGHT, equalTo(2)).with(K1, equalTo("V1")));
                fail();
            } catch (UnableToCountException e) {
            }

            try {
                counter.count(one, wildcard(ONE, INCOMING).with(WEIGHT, equalTo(2)).with(K2, equalTo("V2")));
                fail();
            } catch (UnableToCountException e) {
            }

            try {
                counter.count(one, literal(ONE, INCOMING).with(WEIGHT, equalTo(2)).with(K2, equalTo("V2")));
                fail();
            } catch (UnableToCountException e) {
            }

            //Node one both

            assertEquals(10 * factor, counter.count(one, wildcard(ONE, BOTH)));

            try {
                counter.count(one, literal(ONE, BOTH));
                fail();
            } catch (UnableToCountException e) {
            }

            try {
                counter.count(one, wildcard(ONE, BOTH).with(WEIGHT, equalTo(1)));
                fail();
            } catch (UnableToCountException e) {
            }

            try {
                counter.count(one, literal(ONE, BOTH).with(WEIGHT, equalTo(1)));
                fail();
            } catch (UnableToCountException e) {
            }

            try {
                counter.count(one, wildcard(ONE, BOTH).with(WEIGHT, equalTo(7)));
                fail();
            } catch (UnableToCountException e) {
            }

            try {
                counter.count(one, literal(ONE, BOTH).with(WEIGHT, equalTo(7)));
                fail();
            } catch (UnableToCountException e) {
            }

            try {
                counter.count(one, wildcard(ONE, BOTH).with(K1, equalTo("V1")));
                fail();
            } catch (UnableToCountException e) {
            }

            try {
                counter.count(one, literal(ONE, BOTH).with(K1, equalTo("V1")));
                fail();
            } catch (UnableToCountException e) {
            }

            tx.success();
        }
    }

    private void simulateUsage() {
        new SimpleTransactionExecutor(database).executeInTransaction(new VoidReturningCallback() {
            @Override
            protected void doInTx(GraphDatabaseService database) {
                Node one = database.getNodeById(1);
                Node two = database.getNodeById(2);

                Relationship cycle = one.createRelationshipTo(one, ONE);
                cycle.setProperty(WEIGHT, 2);

                Relationship oneToTwo = one.createRelationshipTo(two, ONE);
                oneToTwo.setProperty(WEIGHT, 2);
                oneToTwo.setProperty(TIMESTAMP, 123L);
                oneToTwo.setProperty(K1, "V1");

                oneToTwo = one.createRelationshipTo(two, ONE);
                oneToTwo.setProperty(WEIGHT, 1);
                oneToTwo.setProperty(K1, "V1");
            }
        });

        new SimpleTransactionExecutor(database).executeInTransaction(new VoidReturningCallback() {
            @Override
            protected void doInTx(GraphDatabaseService database) {
                Node one = database.getNodeById(1);
                Node two = database.getNodeById(2);

                Relationship oneToTwo = one.createRelationshipTo(two, ONE);
                oneToTwo.setProperty(K1, "V1");

                oneToTwo = one.createRelationshipTo(two, ONE);
                oneToTwo.setProperty(K1, "V1");

                oneToTwo = one.createRelationshipTo(two, ONE);
                oneToTwo.setProperty(K1, "V1");

                oneToTwo = one.createRelationshipTo(two, ONE);
                oneToTwo.setProperty(WEIGHT, 1);

                oneToTwo = one.createRelationshipTo(two, TWO);
                oneToTwo.setProperty(K1, "V1");
                oneToTwo.setProperty(K2, "V1");
            }
        });

        new SimpleTransactionExecutor(database).executeInTransaction(new VoidReturningCallback() {
            @Override
            protected void doInTx(GraphDatabaseService database) {
                Node one = database.getNodeById(1);
                Node two = database.getNodeById(2);

                Relationship twoToOne = two.createRelationshipTo(one, ONE);
                twoToOne.setProperty(K1, "V1");
                twoToOne.setProperty(WEIGHT, 5);

                twoToOne = two.createRelationshipTo(one, ONE);
                twoToOne.setProperty(WEIGHT, 3);
                twoToOne.setProperty("something long", "Some incredibly long text with many characters )(*&^%@£, we hope it's not gonna break the system. \n Just in case, we're also gonna check a long byte array as the next property.");
                twoToOne.setProperty("bytearray", ByteBuffer.allocate(8).putLong(1242352145243231L).array());

                twoToOne = two.createRelationshipTo(one, ONE);
                twoToOne.setProperty(K1, "V1");
                twoToOne.setProperty(K2, "V2");
            }
        });

        new SimpleTransactionExecutor(database).executeInTransaction(new VoidReturningCallback() {
            @Override
            protected void doInTx(GraphDatabaseService database) {
                Node one = database.getNodeById(1);

                for (Relationship r : one.getRelationships(ONE, INCOMING)) {
                    if (r.getProperty(WEIGHT, 0).equals(3)) {
                        r.delete();
                        continue;
                    }
                    if (r.getProperty(WEIGHT, 0).equals(5)) {
                        r.setProperty(WEIGHT, 2);
                    }
                    if (r.getStartNode().equals(r.getEndNode())) {
                        r.setProperty(WEIGHT, 7);
                    }
                }
            }
        });
    }

    private void setUpTwoNodes() {
        new SimpleTransactionExecutor(database).executeInTransaction(new VoidReturningCallback() {
            @Override
            protected void doInTx(GraphDatabaseService database) {
                Node one = database.createNode();
                one.setProperty(NAME, "One");
                one.setProperty(WEIGHT, 1);

                Node two = database.createNode();
                two.setProperty(NAME, "Two");
                two.setProperty(WEIGHT, 2);
            }
        });
    }
}
