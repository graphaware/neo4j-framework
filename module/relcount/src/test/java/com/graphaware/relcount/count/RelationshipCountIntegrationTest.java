package com.graphaware.relcount.count;

import com.graphaware.common.strategy.RelationshipInclusionStrategy;
import com.graphaware.common.strategy.RelationshipPropertyInclusionStrategy;
import com.graphaware.runtime.GraphAwareRuntime;
import com.graphaware.relcount.compact.ThresholdBasedCompactionStrategy;
import com.graphaware.relcount.module.RelationshipCountRuntimeModule;
import com.graphaware.tx.executor.single.SimpleTransactionExecutor;
import com.graphaware.tx.executor.single.VoidReturningCallback;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.test.TestGraphDatabaseFactory;

import java.io.IOException;
import java.nio.ByteBuffer;

import static com.graphaware.common.description.predicate.Predicates.equalTo;
import static com.graphaware.common.description.relationship.RelationshipDescriptionFactory.literal;
import static com.graphaware.common.description.relationship.RelationshipDescriptionFactory.wildcard;
import static com.graphaware.relcount.count.RelationshipCountIntegrationTest.RelationshipTypes.ONE;
import static com.graphaware.relcount.count.RelationshipCountIntegrationTest.RelationshipTypes.TWO;
import static com.graphaware.relcount.module.RelationshipCountStrategiesImpl.defaultStrategies;
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
    }

    @After
    public void tearDown() {
        database.shutdown();
    }

    @Test
    public void noFramework() {
        setUpTwoNodes();
        simulateUsage();

        verifyCounts(1, new NaiveRelationshipCounter());
        verifyCounts(0, new CachedRelationshipCounter());
        verifyCounts(0, new FallbackRelationshipCounter());
    }

    @Test
    public void noFramework2() {
        setUpTwoNodes();
        simulateUsage();
        simulateUsage();

        verifyCounts(2, new NaiveRelationshipCounter());
        verifyCounts(0, new CachedRelationshipCounter());
        verifyCounts(0, new FallbackRelationshipCounter());
    }

    @Test
    public void cachedCountsCanBeRebuilt() {
        GraphAwareRuntime framework = new GraphAwareRuntime(database);
        final RelationshipCountRuntimeModule module = new RelationshipCountRuntimeModule();
        framework.registerModule(module);
        framework.start();

        setUpTwoNodes();
        simulateUsage();

        module.reinitialize(database);

        verifyCounts(1, module.naiveCounter());
        verifyCounts(1, module.cachedCounter());
        verifyCounts(1, module.fallbackCounter());
    }

    @Test
    public void defaultFrameworkOnNewDatabase() {
        GraphAwareRuntime framework = new GraphAwareRuntime(database);
        final RelationshipCountRuntimeModule module = new RelationshipCountRuntimeModule();
        framework.registerModule(module);
        framework.start();

        setUpTwoNodes();
        simulateUsage();

        verifyCounts(1, module.naiveCounter());
        verifyCounts(1, module.cachedCounter());
        verifyCounts(1, module.fallbackCounter());
    }

    @Test
    public void defaultFrameworkWithChangedModule() throws IOException {
        TemporaryFolder temporaryFolder = new TemporaryFolder();
        temporaryFolder.create();
        database = new GraphDatabaseFactory().newEmbeddedDatabase(temporaryFolder.getRoot().getAbsolutePath());

        GraphAwareRuntime framework = new GraphAwareRuntime(database);
        RelationshipCountRuntimeModule module = new RelationshipCountRuntimeModule();
        framework.registerModule(module);
        framework.start();

        setUpTwoNodes();
        simulateUsage();

        verifyCounts(1, module.naiveCounter());
        verifyCounts(1, module.cachedCounter());
        verifyCounts(1, module.fallbackCounter());

        database.shutdown();

        database = new GraphDatabaseFactory().newEmbeddedDatabase(temporaryFolder.getRoot().getAbsolutePath());

        framework = new GraphAwareRuntime(database);
        module = new RelationshipCountRuntimeModule(defaultStrategies().with(new ThresholdBasedCompactionStrategy(4)));
        framework.registerModule(module);
        framework.start();

        verifyCounts(1, module.naiveCounter());
        verifyCompactedCounts(1, module.cachedCounter());
        verifyCounts(1, module.fallbackCounter());

        database.shutdown();

        database = new GraphDatabaseFactory().newEmbeddedDatabase(temporaryFolder.getRoot().getAbsolutePath());

        framework = new GraphAwareRuntime(database);
        module = new RelationshipCountRuntimeModule(defaultStrategies().with(new ThresholdBasedCompactionStrategy(20)));
        framework.registerModule(module);
        framework.start();

        verifyCounts(1, module.naiveCounter());
        verifyCounts(1, module.cachedCounter());
        verifyCounts(1, module.fallbackCounter());
    }

    @Test
    public void defaultFrameworkOnExistingDatabase() {
        setUpTwoNodes();
        simulateUsage();

        GraphAwareRuntime framework = new GraphAwareRuntime(database);
        final RelationshipCountRuntimeModule module = new RelationshipCountRuntimeModule();
        framework.registerModule(module);
        framework.start();

        verifyCounts(1, module.naiveCounter());
        verifyCounts(1, module.cachedCounter());
        verifyCounts(1, module.fallbackCounter());
    }

    @Test
    public void customFrameworkOnNewDatabase() {
        GraphAwareRuntime framework = new GraphAwareRuntime(database);
        final RelationshipCountRuntimeModule module = new RelationshipCountRuntimeModule();
        framework.registerModule(module);
        framework.start();

        setUpTwoNodes();
        simulateUsage();

        verifyCounts(1, module.naiveCounter());
        verifyCounts(1, module.cachedCounter());
        verifyCounts(1, module.fallbackCounter());
    }

    @Test
    public void customFrameworkOnExistingDatabase() {
        setUpTwoNodes();
        simulateUsage();

        GraphAwareRuntime framework = new GraphAwareRuntime(database);
        final RelationshipCountRuntimeModule module = new RelationshipCountRuntimeModule();
        framework.registerModule(module);
        framework.start();

        verifyCounts(1, module.naiveCounter());
        verifyCounts(1, module.cachedCounter());
        verifyCounts(1, module.fallbackCounter());
    }

    @Test
    public void weightedRelationships() {
        for (int numberOfRounds = 1; numberOfRounds <= 10; numberOfRounds++) {
            setUp();

            GraphAwareRuntime framework = new GraphAwareRuntime(database);
            final RelationshipCountRuntimeModule module = new RelationshipCountRuntimeModule(
                    defaultStrategies()
                            .with(new WeighingStrategy() {
                                @Override
                                public int getRelationshipWeight(Relationship relationship, Node pointOfView) {
                                    return (int) relationship.getProperty(WEIGHT, 1);
                                }

                                @Override
                                public String asString() {
                                    return "custom";
                                }
                            }));

            framework.registerModule(module);
            framework.start();

            setUpTwoNodes();

            for (int i = 0; i < numberOfRounds; i++) {
                simulateUsage();
            }

            verifyWeightedCounts(numberOfRounds, module.naiveCounter());
            verifyWeightedCounts(numberOfRounds, module.cachedCounter());
            verifyWeightedCounts(numberOfRounds, module.fallbackCounter());

            tearDown();
        }
    }

    @Test
    public void defaultStrategiesWithLowerThreshold() {
        GraphAwareRuntime framework = new GraphAwareRuntime(database);
        final RelationshipCountRuntimeModule module = new RelationshipCountRuntimeModule(
                defaultStrategies().with(new ThresholdBasedCompactionStrategy(4))
        );
        framework.registerModule(module);
        framework.start();

        setUpTwoNodes();
        simulateUsage();

        verifyCounts(1, module.naiveCounter());
        verifyCompactedCounts(1, module.cachedCounter());
        verifyCounts(1, module.fallbackCounter());
    }

    @Test
    public void defaultStrategiesWithLowerThreshold2() {
        GraphAwareRuntime framework = new GraphAwareRuntime(database);
        final RelationshipCountRuntimeModule module = new RelationshipCountRuntimeModule(
                defaultStrategies().with(new ThresholdBasedCompactionStrategy(4))
        );
        framework.registerModule(module);
        framework.start();

        setUpTwoNodes();
        simulateUsage();
        simulateUsage();

        verifyCounts(2, module.naiveCounter());
        verifyCompactedCounts(2, module.cachedCounter());
        verifyCounts(2, module.fallbackCounter());
    }

    @Test
    public void defaultStrategiesWithLowerThreshold3() {
        GraphAwareRuntime framework = new GraphAwareRuntime(database);
        final RelationshipCountRuntimeModule module = new RelationshipCountRuntimeModule(
                defaultStrategies().with(new ThresholdBasedCompactionStrategy(3))
        );
        framework.registerModule(module);
        framework.start();

        setUpTwoNodes();
        simulateUsage();

        try {
            module.cachedCounter().count(database.getNodeById(1), wildcard(ONE, OUTGOING).with(WEIGHT, equalTo(2)).with(TIMESTAMP, equalTo("123")).with(K1, equalTo("V1")));
            fail();
        } catch (UnableToCountException e) {
            //OK
        }
    }

    @Test
    public void defaultStrategiesWithLowerThreshold20() {
        for (int threshold = 3; threshold <= 20; threshold++) {
            setUp();

            GraphAwareRuntime framework = new GraphAwareRuntime(database);
            final RelationshipCountRuntimeModule module = new RelationshipCountRuntimeModule(
                    defaultStrategies().with(new ThresholdBasedCompactionStrategy(threshold))
            );
            framework.registerModule(module);
            framework.start();

            setUpTwoNodes();
            simulateUsage();
            simulateUsage();
            simulateUsage();

            verifyCounts(3, module.fallbackCounter());
            verifyCounts(3, module.naiveCounter());

            tearDown();
        }
    }

    @Test
    public void weightedRelationshipsWithCompaction() {
        GraphAwareRuntime framework = new GraphAwareRuntime(database);
        final RelationshipCountRuntimeModule module = new RelationshipCountRuntimeModule(
                defaultStrategies()
                        .with(new WeighingStrategy() {
                            @Override
                            public int getRelationshipWeight(Relationship relationship, Node pointOfView) {
                                return (int) relationship.getProperty(WEIGHT, 1);
                            }

                            @Override
                            public String asString() {
                                return "custom";
                            }
                        })
                        .with(new ThresholdBasedCompactionStrategy(10)));

        framework.registerModule(module);
        framework.start();

        setUpTwoNodes();
        simulateUsage();
        simulateUsage();
        simulateUsage();
        simulateUsage();

        verifyWeightedCounts(4, module.fallbackCounter());
        verifyWeightedCounts(4, module.naiveCounter());
    }

    @Test
    public void twoSimultaneousModules() {
        GraphAwareRuntime framework = new GraphAwareRuntime(database);
        final RelationshipCountRuntimeModule module1 = new RelationshipCountRuntimeModule("M1", defaultStrategies());
        final RelationshipCountRuntimeModule module2 = new RelationshipCountRuntimeModule("M2",
                defaultStrategies()
                        .with(new WeighingStrategy() {
                            @Override
                            public int getRelationshipWeight(Relationship relationship, Node pointOfView) {
                                return (int) relationship.getProperty(WEIGHT, 1);
                            }

                            @Override
                            public String asString() {
                                return "custom";
                            }
                        }));

        framework.registerModule(module1);
        framework.registerModule(module2);
        framework.start();

        setUpTwoNodes();
        simulateUsage();
        simulateUsage();

        verifyCounts(2, module1.naiveCounter());
        verifyCounts(2, module1.cachedCounter());
        verifyCounts(2, module1.fallbackCounter());

        verifyWeightedCounts(2, module2.naiveCounter());
        verifyWeightedCounts(2, module2.cachedCounter());
        verifyWeightedCounts(2, module2.fallbackCounter());
    }

    @Test
    public void customRelationshipInclusionStrategy() {
        GraphAwareRuntime framework = new GraphAwareRuntime(database);
        final RelationshipCountRuntimeModule module = new RelationshipCountRuntimeModule(
                defaultStrategies()
                        .with(new RelationshipInclusionStrategy() {
                            @Override
                            public boolean include(Relationship relationship) {
                                return !relationship.isType(TWO);
                            }

                            @Override
                            public String asString() {
                                return "custom";
                            }
                        }));

        framework.registerModule(module);
        framework.start();

        setUpTwoNodes();
        simulateUsage();
        simulateUsage();

        //naive doesn't care about this strategy
        assertEquals(2, module.naiveCounter().count(database.getNodeById(1), wildcard(TWO, OUTGOING)));
        assertEquals(0, module.fallbackCounter().count(database.getNodeById(1), wildcard(TWO, OUTGOING)));
        assertEquals(0, module.cachedCounter().count(database.getNodeById(1), wildcard(TWO, OUTGOING)));
    }

    @Test
    public void customRelationshipPropertiesInclusionStrategy() {
        GraphAwareRuntime framework = new GraphAwareRuntime(database);
        final RelationshipCountRuntimeModule module = new RelationshipCountRuntimeModule(
                defaultStrategies()
                        .with(new RelationshipPropertyInclusionStrategy() {
                            @Override
                            public boolean include(String key, Relationship propertyContainer) {
                                return !WEIGHT.equals(key);
                            }

                            @Override
                            public String asString() {
                                return "custom";
                            }
                        }));

        framework.registerModule(module);
        framework.start();

        setUpTwoNodes();
        simulateUsage();
        simulateUsage();

        //naive doesn't care about this strategy
        assertEquals(2, module.naiveCounter().count(database.getNodeById(1), wildcard(ONE, OUTGOING).with(WEIGHT, equalTo(7))));
        assertEquals(2, module.naiveCounter().count(database.getNodeById(1), literal(ONE, OUTGOING).with(WEIGHT, equalTo(7))));
        assertEquals(0, module.fallbackCounter().count(database.getNodeById(1), wildcard(ONE, OUTGOING).with(WEIGHT, equalTo(7))));
        assertEquals(0, module.fallbackCounter().count(database.getNodeById(1), literal(ONE, OUTGOING).with(WEIGHT, equalTo(7))));
        assertEquals(0, module.cachedCounter().count(database.getNodeById(1), wildcard(ONE, OUTGOING).with(WEIGHT, equalTo(7))));
        assertEquals(0, module.cachedCounter().count(database.getNodeById(1), literal(ONE, OUTGOING).with(WEIGHT, equalTo(7))));
    }

    @Test
    public void batchTest() {
        GraphAwareRuntime framework = new GraphAwareRuntime(database);
        final RelationshipCountRuntimeModule module = new RelationshipCountRuntimeModule();
        framework.registerModule(module);
        framework.start();

        setUpTwoNodes();

        new SimpleTransactionExecutor(database).executeInTransaction(new VoidReturningCallback() {
            @Override
            protected void doInTx(GraphDatabaseService database) {
                for (int i = 0; i < 100; i++) {
                    simulateUsage();
                }
            }
        });

        verifyCounts(100, module.naiveCounter());
        verifyCounts(100, module.cachedCounter());
        verifyCounts(100, module.fallbackCounter());
    }

    @Test
    public void batchTestWithMultipleModulesAndLowerThreshold() {
        GraphAwareRuntime framework = new GraphAwareRuntime(database);
        final RelationshipCountRuntimeModule module1 = new RelationshipCountRuntimeModule("M1", defaultStrategies().with(new ThresholdBasedCompactionStrategy(4)));
        final RelationshipCountRuntimeModule module2 = new RelationshipCountRuntimeModule("M2", defaultStrategies().with(new ThresholdBasedCompactionStrategy(4)));
        framework.registerModule(module1);
        framework.registerModule(module2);
        framework.start();

        setUpTwoNodes();

        new SimpleTransactionExecutor(database).executeInTransaction(new VoidReturningCallback() {
            @Override
            protected void doInTx(GraphDatabaseService database) {
                for (int i = 0; i < 20; i++) {
                    simulateUsage();
                }
            }
        });

        verifyCounts(20, module1.naiveCounter());
        verifyCompactedCounts(20, module1.cachedCounter());
        verifyCounts(20, module1.fallbackCounter());

        verifyCounts(20, module2.naiveCounter());
        verifyCompactedCounts(20, module2.cachedCounter());
        verifyCounts(20, module2.fallbackCounter());
    }

    @Test
    public void carefullySetupScenarioThatCouldResultInInaccurateCounts() {
        GraphAwareRuntime framework = new GraphAwareRuntime(database);
        final RelationshipCountRuntimeModule module = new RelationshipCountRuntimeModule(defaultStrategies().with(new ThresholdBasedCompactionStrategy(2)));
        framework.registerModule(module);
        framework.start();

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

        assertEquals(1, module.cachedCounter().count(database.getNodeById(1), wildcard("TEST", OUTGOING)));
        assertEquals(1, module.cachedCounter().count(database.getNodeById(1), wildcard("TEST", OUTGOING).with("a", equalTo(2))));
        assertEquals(1, module.cachedCounter().count(database.getNodeById(1), wildcard("TEST", OUTGOING).with("b", equalTo("b"))));

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

        assertEquals(2, module.cachedCounter().count(database.getNodeById(1), wildcard("TEST", OUTGOING)));
        assertEquals(1, module.cachedCounter().count(database.getNodeById(1), wildcard("TEST", OUTGOING).with("a", equalTo(1))));
        assertEquals(1, module.cachedCounter().count(database.getNodeById(1), wildcard("TEST", OUTGOING).with("a", equalTo(2))));
        assertEquals(1, module.cachedCounter().count(database.getNodeById(1), wildcard("TEST", OUTGOING).with("b", equalTo("b"))));
        assertEquals(1, module.cachedCounter().count(database.getNodeById(1), wildcard("TEST", OUTGOING).with("b", equalTo("c"))));

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

        //now we should have 2b, *c
        assertEquals(3, module.cachedCounter().count(database.getNodeById(1), wildcard("TEST", OUTGOING)));
        assertEquals(1, module.cachedCounter().count(database.getNodeById(1), wildcard("TEST", OUTGOING).with("b", equalTo("b"))));
        assertEquals(2, module.cachedCounter().count(database.getNodeById(1), wildcard("TEST", OUTGOING).with("b", equalTo("c"))));

        try {
            module.cachedCounter().count(database.getNodeById(1), wildcard("TEST", OUTGOING).with("a", equalTo(2)));
            fail();
        } catch (UnableToCountException e) {
            //ok
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

        //now we should have 2*, *c

        assertEquals(4, module.cachedCounter().count(database.getNodeById(1), wildcard("TEST", OUTGOING)));

        try {
            module.cachedCounter().count(database.getNodeById(1), wildcard("TEST", OUTGOING).with("a", equalTo(2)));
            fail();
        } catch (UnableToCountException e) {
            //ok
        }

        try {
            module.cachedCounter().count(database.getNodeById(1), wildcard("TEST", OUTGOING).with("b", equalTo("c")));
            fail();
        } catch (UnableToCountException e) {
            //ok
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

        assertEquals(5, module.cachedCounter().count(database.getNodeById(1), wildcard("TEST", OUTGOING)));

        try {
            module.cachedCounter().count(database.getNodeById(1), wildcard("TEST", OUTGOING).with("a", equalTo(2)));
            fail();
        } catch (UnableToCountException e) {
            //ok
        }

        try {
            module.cachedCounter().count(database.getNodeById(1), wildcard("TEST", OUTGOING).with("b", equalTo("c")));
            fail();
        } catch (UnableToCountException e) {
            //ok
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

        assertEquals(6, module.cachedCounter().count(database.getNodeById(1), wildcard("TEST", OUTGOING)));

        try {
            module.cachedCounter().count(database.getNodeById(1), wildcard("TEST", OUTGOING).with("a", equalTo(2)));
            fail();
        } catch (UnableToCountException e) {
            //ok
        }

        try {
            module.cachedCounter().count(database.getNodeById(1), wildcard("TEST", OUTGOING).with("b", equalTo("c")));
            fail();
        } catch (UnableToCountException e) {
            //ok
        }
    }

    private void verifyCounts(int factor, RelationshipCounter counter) {
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
    }

    private void verifyWeightedCounts(int factor, RelationshipCounter counter) {
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
    }

    private void verifyCompactedCounts(int factor, RelationshipCounter counter) {
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
