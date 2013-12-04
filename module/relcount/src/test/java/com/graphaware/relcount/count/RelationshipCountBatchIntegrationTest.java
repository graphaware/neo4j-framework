package com.graphaware.relcount.count;

import com.graphaware.common.strategy.RelationshipInclusionStrategy;
import com.graphaware.common.strategy.RelationshipPropertyInclusionStrategy;
import com.graphaware.runtime.BatchGraphAwareRuntime;
import com.graphaware.relcount.compact.ThresholdBasedCompactionStrategy;
import com.graphaware.relcount.module.RelationshipCountRuntimeModule;
import com.graphaware.relcount.module.RelationshipCountStrategiesImpl;
import com.graphaware.tx.event.batch.api.TransactionSimulatingBatchInserter;
import com.graphaware.tx.event.batch.api.TransactionSimulatingBatchInserterImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.unsafe.batchinsert.BatchInserters;
import org.neo4j.unsafe.batchinsert.BatchRelationship;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.graphaware.common.description.predicate.Predicates.equalTo;
import static com.graphaware.common.description.relationship.RelationshipDescriptionFactory.literal;
import static com.graphaware.common.description.relationship.RelationshipDescriptionFactory.wildcard;
import static com.graphaware.relcount.count.RelationshipCountBatchIntegrationTest.RelationshipTypes.ONE;
import static com.graphaware.relcount.count.RelationshipCountBatchIntegrationTest.RelationshipTypes.TWO;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.neo4j.graphdb.Direction.*;

/**
 * Integration test for relationship counting with batch inserter.
 */
@SuppressWarnings("PointlessArithmeticExpression")
public class RelationshipCountBatchIntegrationTest {

    public static final String WEIGHT = "weight";
    public static final String NAME = "name";
    public static final String TIMESTAMP = "timestamp";
    public static final String K1 = "K1";
    public static final String K2 = "K2";

    public enum RelationshipTypes implements RelationshipType {
        ONE,
        TWO
    }

    protected final TemporaryFolder temporaryFolder = new TemporaryFolder();

    protected GraphDatabaseService database;
    protected TransactionSimulatingBatchInserter batchInserter;


    @Before
    public void setUp() throws IOException {
        temporaryFolder.create();
        batchInserter = new TransactionSimulatingBatchInserterImpl(BatchInserters.inserter(temporaryFolder.getRoot().getAbsolutePath()));
    }

    @After
    public void tearDown() {
        database.shutdown();
        temporaryFolder.delete();
    }

    private void startDatabase() {
        batchInserter.shutdown();
        database = new GraphDatabaseFactory().newEmbeddedDatabase(temporaryFolder.getRoot().getAbsolutePath());
    }

    @Test
    public void noFramework() {
        setUpTwoNodes();
        simulateInserts();
        startDatabase();

        verifyCounts(1, new NaiveRelationshipCounter());
        verifyCounts(0, new CachedRelationshipCounter());
        verifyCounts(0, new FallbackRelationshipCounter());
    }

    @Test
    public void noFramework2() {
        setUpTwoNodes();
        simulateInserts();
        simulateInserts();
        startDatabase();

        verifyCounts(2, new NaiveRelationshipCounter());
        verifyCounts(0, new CachedRelationshipCounter());
        verifyCounts(0, new FallbackRelationshipCounter());
    }

    @Test
    public void cachedCountsCanBeRebuilt() {
        BatchGraphAwareRuntime framework = new BatchGraphAwareRuntime(batchInserter);
        RelationshipCountRuntimeModule module = new RelationshipCountRuntimeModule();
        framework.registerModule(module);
        framework.start();

        setUpTwoNodes();
        simulateInserts();

        batchInserter.shutdown();

        batchInserter = new TransactionSimulatingBatchInserterImpl(BatchInserters.inserter(temporaryFolder.getRoot().getAbsolutePath()));
        framework = new BatchGraphAwareRuntime(batchInserter);
        module = new RelationshipCountRuntimeModule();
        framework.registerModule(module);
        framework.start();

        module.reinitialize(batchInserter);

        startDatabase();

        verifyCounts(1, module.naiveCounter());
        verifyCounts(1, module.cachedCounter());
        verifyCounts(1, module.fallbackCounter());
    }

    @Test
    public void defaultFrameworkOnNewDatabase() {
        BatchGraphAwareRuntime framework = new BatchGraphAwareRuntime(batchInserter);
        final RelationshipCountRuntimeModule module = new RelationshipCountRuntimeModule();
        framework.registerModule(module);
        framework.start();

        setUpTwoNodes();
        simulateInserts();
        startDatabase();

        verifyCounts(1, module.naiveCounter());
        verifyCounts(1, module.cachedCounter());
        verifyCounts(1, module.fallbackCounter());
    }

    @Test
    public void defaultFrameworkWithChangedModule() throws IOException {
        BatchGraphAwareRuntime framework = new BatchGraphAwareRuntime(batchInserter);
        RelationshipCountRuntimeModule module = new RelationshipCountRuntimeModule();
        framework.registerModule(module);
        framework.start();

        setUpTwoNodes();
        simulateInserts();
        startDatabase();

        verifyCounts(1, module.naiveCounter());
        verifyCounts(1, module.cachedCounter());
        verifyCounts(1, module.fallbackCounter());

        database.shutdown();

        batchInserter = new TransactionSimulatingBatchInserterImpl(BatchInserters.inserter(temporaryFolder.getRoot().getAbsolutePath()));

        framework = new BatchGraphAwareRuntime(batchInserter);
        module = new RelationshipCountRuntimeModule(RelationshipCountStrategiesImpl.defaultStrategies().with(new ThresholdBasedCompactionStrategy(4)));
        framework.registerModule(module);
        framework.start();

        startDatabase();

        verifyCounts(1, module.naiveCounter());
        verifyCompactedCounts(1, module.cachedCounter());
        verifyCounts(1, module.fallbackCounter());

        database.shutdown();

        batchInserter = new TransactionSimulatingBatchInserterImpl(BatchInserters.inserter(temporaryFolder.getRoot().getAbsolutePath()));

        framework = new BatchGraphAwareRuntime(batchInserter);
        module = new RelationshipCountRuntimeModule(RelationshipCountStrategiesImpl.defaultStrategies().withThreshold(20));
        framework.registerModule(module);
        framework.start();

        startDatabase();

        verifyCounts(1, module.naiveCounter());
        verifyCounts(1, module.cachedCounter());
        verifyCounts(1, module.fallbackCounter());
    }

    @Test
    public void defaultFrameworkOnExistingDatabase() {
        setUpTwoNodes();
        simulateInserts();

        BatchGraphAwareRuntime framework = new BatchGraphAwareRuntime(batchInserter);
        final RelationshipCountRuntimeModule module = new RelationshipCountRuntimeModule();
        framework.registerModule(module);
        framework.start();

        startDatabase();

        verifyCounts(1, module.naiveCounter());
        verifyCounts(1, module.cachedCounter());
        verifyCounts(1, module.fallbackCounter());
    }

    @Test
    public void customFrameworkOnNewDatabase() {
        BatchGraphAwareRuntime framework = new BatchGraphAwareRuntime(batchInserter);
        final RelationshipCountRuntimeModule module = new RelationshipCountRuntimeModule();
        framework.registerModule(module);
        framework.start();

        setUpTwoNodes();
        simulateInserts();
        startDatabase();

        verifyCounts(1, module.naiveCounter());
        verifyCounts(1, module.cachedCounter());
        verifyCounts(1, module.fallbackCounter());
    }

    @Test
    public void customFrameworkOnExistingDatabase() {
        setUpTwoNodes();
        simulateInserts();

        BatchGraphAwareRuntime framework = new BatchGraphAwareRuntime(batchInserter);
        final RelationshipCountRuntimeModule module = new RelationshipCountRuntimeModule();
        framework.registerModule(module);
        framework.start();

        startDatabase();

        verifyCounts(1, module.naiveCounter());
        verifyCounts(1, module.cachedCounter());
        verifyCounts(1, module.fallbackCounter());
    }

    @Test
    public void weightedRelationships() {
        BatchGraphAwareRuntime framework = new BatchGraphAwareRuntime(batchInserter);
        final RelationshipCountRuntimeModule module = new RelationshipCountRuntimeModule(
                RelationshipCountStrategiesImpl.defaultStrategies()
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
        simulateInserts();
        startDatabase();

        verifyWeightedCounts(1, module.naiveCounter());
        verifyWeightedCounts(1, module.cachedCounter());
        verifyWeightedCounts(1, module.fallbackCounter());
    }


    @Test
    public void defaultStrategiesWithLowerThreshold() {
        BatchGraphAwareRuntime framework = new BatchGraphAwareRuntime(batchInserter);
        final RelationshipCountRuntimeModule module = new RelationshipCountRuntimeModule(
                RelationshipCountStrategiesImpl.defaultStrategies().with(new ThresholdBasedCompactionStrategy(4))
        );
        framework.registerModule(module);
        framework.start();

        setUpTwoNodes();
        simulateInserts();
        startDatabase();

        verifyCounts(1, module.naiveCounter());
        verifyCompactedCounts(1, module.cachedCounter());
        verifyCounts(1, module.fallbackCounter());
    }

    @Test
    public void defaultStrategiesWithLowerThreshold2() {
        BatchGraphAwareRuntime framework = new BatchGraphAwareRuntime(batchInserter);
        final RelationshipCountRuntimeModule module = new RelationshipCountRuntimeModule(
                RelationshipCountStrategiesImpl.defaultStrategies().with(new ThresholdBasedCompactionStrategy(4))
        );
        framework.registerModule(module);
        framework.start();

        setUpTwoNodes();
        simulateInserts();
        simulateInserts();
        startDatabase();

        verifyCounts(2, module.naiveCounter());
        verifyCompactedCounts(2, module.cachedCounter());
        verifyCounts(2, module.fallbackCounter());
    }

    @Test
    public void defaultStrategiesWithLowerThreshold3() {
        BatchGraphAwareRuntime framework = new BatchGraphAwareRuntime(batchInserter);
        final RelationshipCountRuntimeModule module = new RelationshipCountRuntimeModule(
                RelationshipCountStrategiesImpl.defaultStrategies().with(new ThresholdBasedCompactionStrategy(3))
        );
        framework.registerModule(module);
        framework.start();

        setUpTwoNodes();
        simulateInserts();
        startDatabase();

        try (Transaction tx = database.beginTx()) {

            try {
                module.cachedCounter().count(database.getNodeById(1), wildcard(ONE, OUTGOING).with(WEIGHT, equalTo(2)).with(TIMESTAMP, equalTo("123")).with(K1, equalTo("V1")));
                fail();
            } catch (UnableToCountException e) {
                //OK
            }

            tx.success();
        }
    }

    @Test
    public void weightedRelationshipsWithCompaction() {
        BatchGraphAwareRuntime framework = new BatchGraphAwareRuntime(batchInserter);
        final RelationshipCountRuntimeModule module = new RelationshipCountRuntimeModule(
                RelationshipCountStrategiesImpl.defaultStrategies()
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
        simulateInserts();
        simulateInserts();
        simulateInserts();
        simulateInserts();
        startDatabase();

        verifyWeightedCounts(4, module.fallbackCounter());
        verifyWeightedCounts(4, module.naiveCounter());
    }

    @Test
    public void twoSimultaneousModules() {
        BatchGraphAwareRuntime framework = new BatchGraphAwareRuntime(batchInserter);
        final RelationshipCountRuntimeModule module1 = new RelationshipCountRuntimeModule("M1", RelationshipCountStrategiesImpl.defaultStrategies());
        final RelationshipCountRuntimeModule module2 = new RelationshipCountRuntimeModule("M2",
                RelationshipCountStrategiesImpl.defaultStrategies()
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
        simulateInserts();
        simulateInserts();
        startDatabase();

        verifyCounts(2, module1.naiveCounter());
        verifyCounts(2, module1.cachedCounter());
        verifyCounts(2, module1.fallbackCounter());

        verifyWeightedCounts(2, module2.naiveCounter());
        verifyWeightedCounts(2, module2.cachedCounter());
        verifyWeightedCounts(2, module2.fallbackCounter());
    }

    @Test
    public void customRelationshipInclusionStrategy() {
        BatchGraphAwareRuntime framework = new BatchGraphAwareRuntime(batchInserter);
        final RelationshipCountRuntimeModule module = new RelationshipCountRuntimeModule(
                RelationshipCountStrategiesImpl.defaultStrategies()
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
        simulateInserts();
        simulateInserts();
        startDatabase();

        try (Transaction tx = database.beginTx()) {

            //naive doesn't care about this strategy
            assertEquals(2, module.naiveCounter().count(database.getNodeById(1), wildcard(TWO, OUTGOING)));
            assertEquals(0, module.fallbackCounter().count(database.getNodeById(1), wildcard(TWO, OUTGOING)));
            assertEquals(0, module.cachedCounter().count(database.getNodeById(1), wildcard(TWO, OUTGOING)));

            tx.success();
        }

    }

    @Test
    public void customRelationshipPropertiesInclusionStrategy() {
        BatchGraphAwareRuntime framework = new BatchGraphAwareRuntime(batchInserter);
        final RelationshipCountRuntimeModule module = new RelationshipCountRuntimeModule(
                RelationshipCountStrategiesImpl.defaultStrategies()
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
        simulateInserts();
        simulateInserts();
        startDatabase();

        try (Transaction tx = database.beginTx()) {

            //naive doesn't care about this strategy
            assertEquals(2, module.naiveCounter().count(database.getNodeById(1), wildcard(ONE, OUTGOING).with(WEIGHT, equalTo(7))));
            assertEquals(2, module.naiveCounter().count(database.getNodeById(1), literal(ONE, OUTGOING).with(WEIGHT, equalTo(7))));
            assertEquals(0, module.fallbackCounter().count(database.getNodeById(1), wildcard(ONE, OUTGOING).with(WEIGHT, equalTo(7))));
            assertEquals(0, module.fallbackCounter().count(database.getNodeById(1), literal(ONE, OUTGOING).with(WEIGHT, equalTo(7))));
            assertEquals(0, module.cachedCounter().count(database.getNodeById(1), wildcard(ONE, OUTGOING).with(WEIGHT, equalTo(7))));
            assertEquals(0, module.cachedCounter().count(database.getNodeById(1), literal(ONE, OUTGOING).with(WEIGHT, equalTo(7))));

            tx.success();
        }
    }

    @Test
    @Ignore("bug in neo4j") //https://github.com/neo4j/neo4j/issues/1304
    public void batchTest() {
        BatchGraphAwareRuntime framework = new BatchGraphAwareRuntime(batchInserter);
        final RelationshipCountRuntimeModule module = new RelationshipCountRuntimeModule();
        framework.registerModule(module);
        framework.start();

        setUpTwoNodes();

        for (int i = 0; i < 100; i++) {
            simulateInserts();
        }

        startDatabase();

        verifyCounts(100, module.naiveCounter());
        verifyCounts(100, module.cachedCounter());
        verifyCounts(100, module.fallbackCounter());
    }

    @Test
    public void batchTestWithMultipleModulesAndLowerThreshold() {
        BatchGraphAwareRuntime framework = new BatchGraphAwareRuntime(batchInserter);
        final RelationshipCountRuntimeModule module1 = new RelationshipCountRuntimeModule("M1", RelationshipCountStrategiesImpl.defaultStrategies().with(new ThresholdBasedCompactionStrategy(4)));
        final RelationshipCountRuntimeModule module2 = new RelationshipCountRuntimeModule("M2", RelationshipCountStrategiesImpl.defaultStrategies().with(new ThresholdBasedCompactionStrategy(4)));
        framework.registerModule(module1);
        framework.registerModule(module2);
        framework.start();

        setUpTwoNodes();

        for (int i = 0; i < 20; i++) {
            simulateInserts();
        }

        startDatabase();

        verifyCounts(20, module1.naiveCounter());
        verifyCompactedCounts(20, module1.cachedCounter());
        verifyCounts(20, module1.fallbackCounter());

        verifyCounts(20, module2.naiveCounter());
        verifyCompactedCounts(20, module2.cachedCounter());
        verifyCounts(20, module2.fallbackCounter());
    }

    private void verifyCounts(int factor, RelationshipCounter counter) {
        try (Transaction tx = database.beginTx()) {

            Node one = database.getNodeById(1);
            Node two = database.getNodeById(2);

            //Node one incoming

            assertEquals(3 * factor, counter.count(one, wildcard(ONE, INCOMING)));


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

    private void simulateInserts() {
        Map<String, Object> props = new HashMap<>();
        props.put(WEIGHT, 2);

        batchInserter.createRelationship(1, 1, RelationshipCountIntegrationTest.RelationshipTypes.ONE, props);

        props = new HashMap<>();
        props.put(WEIGHT, 2);
        props.put(TIMESTAMP, 123L);
        props.put(K1, "V1");
        batchInserter.createRelationship(1, 2, RelationshipCountIntegrationTest.RelationshipTypes.ONE, props);

        props = new HashMap<>();
        props.put(WEIGHT, 1);
        props.put(K1, "V1");
        batchInserter.createRelationship(1, 2, RelationshipCountIntegrationTest.RelationshipTypes.ONE, props);

        props = new HashMap<>();
        props.put(K1, "V1");
        batchInserter.createRelationship(1, 2, RelationshipCountIntegrationTest.RelationshipTypes.ONE, props);
        batchInserter.createRelationship(1, 2, RelationshipCountIntegrationTest.RelationshipTypes.ONE, props);
        batchInserter.createRelationship(1, 2, RelationshipCountIntegrationTest.RelationshipTypes.ONE, props);

        props = new HashMap<>();
        props.put(WEIGHT, 1);
        batchInserter.createRelationship(1, 2, RelationshipCountIntegrationTest.RelationshipTypes.ONE, props);

        props = new HashMap<>();
        props.put(K1, "V1");
        props.put(K2, "V1");
        batchInserter.createRelationship(1, 2, RelationshipCountIntegrationTest.RelationshipTypes.TWO, props);

        props = new HashMap<>();
        props.put(K1, "V1");
        props.put(WEIGHT, 5);
        batchInserter.createRelationship(2, 1, RelationshipCountIntegrationTest.RelationshipTypes.ONE, props);

        props = new HashMap<>();
        props.put(K1, "V1");
        props.put(K2, "V2");
        batchInserter.createRelationship(2, 1, RelationshipCountIntegrationTest.RelationshipTypes.ONE, props);

        for (BatchRelationship r : batchInserter.getRelationships(1)) {
            if (r.getStartNode() == 1 && r.getEndNode() != 1) {
                continue;
            }
            if (((Integer) 5).equals(batchInserter.getRelationshipProperties(r.getId()).get(WEIGHT)) && 1 == r.getEndNode()) {
                batchInserter.setRelationshipProperty(r.getId(), WEIGHT, 2);
            }
            if (r.getStartNode() == r.getEndNode()) {
                batchInserter.setRelationshipProperty(r.getId(), WEIGHT, 7);
            }
        }
    }

    private void setUpTwoNodes() {
        Map<String, Object> props = new HashMap<>();
        props.put(NAME, "One");
        props.put(WEIGHT, 1);
        batchInserter.createNode(1, props);

        props = new HashMap<>();
        props.put(NAME, "Two");
        props.put(WEIGHT, 2);
        batchInserter.createNode(2, props);
    }
}
