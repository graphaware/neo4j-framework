package com.graphaware.module.relcount.count;

import com.graphaware.common.strategy.RelationshipInclusionStrategy;
import com.graphaware.common.strategy.RelationshipPropertyInclusionStrategy;
import com.graphaware.module.relcount.RelationshipCountConfigurationImpl;
import com.graphaware.module.relcount.RelationshipCountRuntimeModule;
import com.graphaware.module.relcount.compact.ThresholdBasedCompactionStrategy;
import com.graphaware.runtime.BatchGraphAwareRuntime;
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
import static com.graphaware.module.relcount.count.RelationshipCountBatchIntegrationTest.RelationshipTypes.ONE;
import static com.graphaware.module.relcount.count.RelationshipCountBatchIntegrationTest.RelationshipTypes.TWO;
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
    public void naiveCounterShouldWorkWithoutRuntime() {
        setUpTwoNodes();
        simulateInserts();
        startDatabase();

        verifyCounts(1, new NaiveRelationshipCounter(database));
    }

    @Test
    public void naiveCounterShouldRespectWeighingWithoutRuntime() {
        setUpTwoNodes();
        simulateInserts();
        startDatabase();

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
        simulateInserts();
        startDatabase();

        verifyCounts(0, new CachedRelationshipCounter(database));
    }

    @Test(expected = IllegalStateException.class)
    public void fallBackCounterShouldNotWorkWithoutRuntime() {
        setUpTwoNodes();
        simulateInserts();
        startDatabase();

        verifyCounts(0, new FallbackRelationshipCounter(database));
    }

    @Test
    public void cachedCountsCanBeRebuilt() {
        BatchGraphAwareRuntime runtime = new BatchGraphAwareRuntime(batchInserter);
        RelationshipCountRuntimeModule module = new RelationshipCountRuntimeModule();
        runtime.registerModule(module);
        runtime.start();

        setUpTwoNodes();
        simulateInserts();

        batchInserter.shutdown();

        batchInserter = new TransactionSimulatingBatchInserterImpl(BatchInserters.inserter(temporaryFolder.getRoot().getAbsolutePath()));
        runtime = new BatchGraphAwareRuntime(batchInserter);
        module = new RelationshipCountRuntimeModule();
        runtime.registerModule(module);
        runtime.start();

        module.reinitialize(batchInserter);

        startDatabase();

        verifyCounts(1, new NaiveRelationshipCounter(database));
        verifyCounts(1, new CachedRelationshipCounter(database));
        verifyCounts(1, new FallbackRelationshipCounter(database));
    }

    @Test
    public void defaultRuntimeOnNewDatabase() {
        BatchGraphAwareRuntime runtime = new BatchGraphAwareRuntime(batchInserter);
        final RelationshipCountRuntimeModule module = new RelationshipCountRuntimeModule();
        runtime.registerModule(module);
        runtime.start();

        setUpTwoNodes();
        simulateInserts();
        startDatabase();

        verifyCounts(1, new NaiveRelationshipCounter(database));
        verifyCounts(1, new CachedRelationshipCounter(database));
        verifyCounts(1, new FallbackRelationshipCounter(database));
    }

    @Test
    public void defaultRuntimeWithChangedModule() throws IOException {
        BatchGraphAwareRuntime runtime = new BatchGraphAwareRuntime(batchInserter);
        RelationshipCountRuntimeModule module = new RelationshipCountRuntimeModule();
        runtime.registerModule(module);
        runtime.start();

        setUpTwoNodes();
        simulateInserts();
        startDatabase();

        verifyCounts(1, new NaiveRelationshipCounter(database));
        verifyCounts(1, new CachedRelationshipCounter(database));
        verifyCounts(1, new FallbackRelationshipCounter(database));

        database.shutdown();

        batchInserter = new TransactionSimulatingBatchInserterImpl(BatchInserters.inserter(temporaryFolder.getRoot().getAbsolutePath()));

        runtime = new BatchGraphAwareRuntime(batchInserter);
        module = new RelationshipCountRuntimeModule(RelationshipCountConfigurationImpl.defaultConfiguration().with(new ThresholdBasedCompactionStrategy(4)));
        runtime.registerModule(module);
        runtime.start();

        startDatabase();

        verifyCounts(1, new NaiveRelationshipCounter(database));
        verifyCompactedCounts(1, new CachedRelationshipCounter(database));
        verifyCounts(1, new FallbackRelationshipCounter(database));

        database.shutdown();

        batchInserter = new TransactionSimulatingBatchInserterImpl(BatchInserters.inserter(temporaryFolder.getRoot().getAbsolutePath()));

        runtime = new BatchGraphAwareRuntime(batchInserter);
        module = new RelationshipCountRuntimeModule(RelationshipCountConfigurationImpl.defaultConfiguration().withThreshold(20));
        runtime.registerModule(module);
        runtime.start();

        startDatabase();

        verifyCounts(1, new NaiveRelationshipCounter(database));
        verifyCounts(1, new CachedRelationshipCounter(database));
        verifyCounts(1, new FallbackRelationshipCounter(database));
    }

    @Test
    public void defaultRuntimeOnExistingDatabase() {
        setUpTwoNodes();
        simulateInserts();

        BatchGraphAwareRuntime runtime = new BatchGraphAwareRuntime(batchInserter);
        final RelationshipCountRuntimeModule module = new RelationshipCountRuntimeModule();
        runtime.registerModule(module);
        runtime.start();

        startDatabase();

        verifyCounts(1, new NaiveRelationshipCounter(database));
        verifyCounts(1, new CachedRelationshipCounter(database));
        verifyCounts(1, new FallbackRelationshipCounter(database));
    }

    @Test
    public void customRuntimeOnNewDatabase() {
        BatchGraphAwareRuntime runtime = new BatchGraphAwareRuntime(batchInserter);
        final RelationshipCountRuntimeModule module = new RelationshipCountRuntimeModule();
        runtime.registerModule(module);
        runtime.start();

        setUpTwoNodes();
        simulateInserts();
        startDatabase();

        verifyCounts(1, new NaiveRelationshipCounter(database));
        verifyCounts(1, new CachedRelationshipCounter(database));
        verifyCounts(1, new FallbackRelationshipCounter(database));
    }

    @Test
    public void customRuntimeOnExistingDatabase() {
        setUpTwoNodes();
        simulateInserts();

        BatchGraphAwareRuntime runtime = new BatchGraphAwareRuntime(batchInserter);
        final RelationshipCountRuntimeModule module = new RelationshipCountRuntimeModule();
        runtime.registerModule(module);
        runtime.start();

        startDatabase();

        verifyCounts(1, new NaiveRelationshipCounter(database));
        verifyCounts(1, new CachedRelationshipCounter(database));
        verifyCounts(1, new FallbackRelationshipCounter(database));
    }

    @Test
    public void weightedRelationships() {
        BatchGraphAwareRuntime runtime = new BatchGraphAwareRuntime(batchInserter);
        final RelationshipCountRuntimeModule module = new RelationshipCountRuntimeModule(
                RelationshipCountConfigurationImpl.defaultConfiguration()
                        .with(new WeighingStrategy() {
                            @Override
                            public int getRelationshipWeight(Relationship relationship, Node pointOfView) {
                                return (int) relationship.getProperty(WEIGHT, 1);
                            }
                        }));

        runtime.registerModule(module);
        runtime.start();

        setUpTwoNodes();
        simulateInserts();
        startDatabase();

        verifyWeightedCounts(1, new NaiveRelationshipCounter(database));
        verifyWeightedCounts(1, new CachedRelationshipCounter(database));
        verifyWeightedCounts(1, new FallbackRelationshipCounter(database));
    }


    @Test
    public void defaultStrategiesWithLowerThreshold() {
        BatchGraphAwareRuntime runtime = new BatchGraphAwareRuntime(batchInserter);
        final RelationshipCountRuntimeModule module = new RelationshipCountRuntimeModule(
                RelationshipCountConfigurationImpl.defaultConfiguration().with(new ThresholdBasedCompactionStrategy(4))
        );
        runtime.registerModule(module);
        runtime.start();

        setUpTwoNodes();
        simulateInserts();
        startDatabase();

        verifyCounts(1, new NaiveRelationshipCounter(database));
        verifyCompactedCounts(1, new CachedRelationshipCounter(database));
        verifyCounts(1, new FallbackRelationshipCounter(database));
    }

    @Test
    public void defaultStrategiesWithLowerThreshold2() {
        BatchGraphAwareRuntime runtime = new BatchGraphAwareRuntime(batchInserter);
        final RelationshipCountRuntimeModule module = new RelationshipCountRuntimeModule(
                RelationshipCountConfigurationImpl.defaultConfiguration().with(new ThresholdBasedCompactionStrategy(4))
        );
        runtime.registerModule(module);
        runtime.start();

        setUpTwoNodes();
        simulateInserts();
        simulateInserts();
        startDatabase();

        verifyCounts(2, new NaiveRelationshipCounter(database));
        verifyCompactedCounts(2, new CachedRelationshipCounter(database));
        verifyCounts(2, new FallbackRelationshipCounter(database));
    }

    @Test
    public void defaultStrategiesWithLowerThreshold3() {
        BatchGraphAwareRuntime runtime = new BatchGraphAwareRuntime(batchInserter);
        final RelationshipCountRuntimeModule module = new RelationshipCountRuntimeModule(
                RelationshipCountConfigurationImpl.defaultConfiguration().with(new ThresholdBasedCompactionStrategy(3))
        );
        runtime.registerModule(module);
        runtime.start();

        setUpTwoNodes();
        simulateInserts();
        startDatabase();

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
    public void weightedRelationshipsWithCompaction() {
        BatchGraphAwareRuntime runtime = new BatchGraphAwareRuntime(batchInserter);
        final RelationshipCountRuntimeModule module = new RelationshipCountRuntimeModule(
                RelationshipCountConfigurationImpl.defaultConfiguration()
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
        simulateInserts();
        simulateInserts();
        simulateInserts();
        simulateInserts();
        startDatabase();

        verifyWeightedCounts(4, new FallbackRelationshipCounter(database));
        verifyWeightedCounts(4, new NaiveRelationshipCounter(database));
    }

    @Test
    public void twoSimultaneousModules() {
        BatchGraphAwareRuntime runtime = new BatchGraphAwareRuntime(batchInserter);
        final RelationshipCountRuntimeModule module1 = new RelationshipCountRuntimeModule("M1", RelationshipCountConfigurationImpl.defaultConfiguration());
        final RelationshipCountRuntimeModule module2 = new RelationshipCountRuntimeModule("M2",
                RelationshipCountConfigurationImpl.defaultConfiguration()
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
        simulateInserts();
        simulateInserts();
        startDatabase();

        verifyCounts(2, new NaiveRelationshipCounter(database, "M1"));
        verifyCounts(2, new CachedRelationshipCounter(database, "M1"));
        verifyCounts(2, new FallbackRelationshipCounter(database, "M1"));

        verifyWeightedCounts(2, new NaiveRelationshipCounter(database, "M2"));
        verifyWeightedCounts(2, new CachedRelationshipCounter(database, "M2"));
        verifyWeightedCounts(2, new FallbackRelationshipCounter(database, "M2"));
    }

    @Test
    public void customRelationshipInclusionStrategy() {
        BatchGraphAwareRuntime runtime = new BatchGraphAwareRuntime(batchInserter);
        final RelationshipCountRuntimeModule module = new RelationshipCountRuntimeModule(
                RelationshipCountConfigurationImpl.defaultConfiguration()
                        .with(new RelationshipInclusionStrategy() {
                            @Override
                            public boolean include(Relationship relationship) {
                                return !relationship.isType(TWO);
                            }
                        }));

        runtime.registerModule(module);
        runtime.start();

        setUpTwoNodes();
        simulateInserts();
        simulateInserts();
        startDatabase();

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
        BatchGraphAwareRuntime runtime = new BatchGraphAwareRuntime(batchInserter);
        final RelationshipCountRuntimeModule module = new RelationshipCountRuntimeModule(
                RelationshipCountConfigurationImpl.defaultConfiguration()
                        .with(new RelationshipPropertyInclusionStrategy() {
                            @Override
                            public boolean include(String key, Relationship propertyContainer) {
                                return !WEIGHT.equals(key);
                            }
                        }));

        runtime.registerModule(module);
        runtime.start();

        setUpTwoNodes();
        simulateInserts();
        simulateInserts();
        startDatabase();

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
    @Ignore("bug in neo4j") //https://github.com/neo4j/neo4j/issues/1304
    public void batchTest() {
        BatchGraphAwareRuntime runtime = new BatchGraphAwareRuntime(batchInserter);
        final RelationshipCountRuntimeModule module = new RelationshipCountRuntimeModule();
        runtime.registerModule(module);
        runtime.start();

        setUpTwoNodes();

        for (int i = 0; i < 100; i++) {
            simulateInserts();
        }

        startDatabase();

        verifyCounts(100, new NaiveRelationshipCounter(database));
        verifyCounts(100, new CachedRelationshipCounter(database));
        verifyCounts(100, new FallbackRelationshipCounter(database));
    }

    @Test
    public void batchTestWithMultipleModulesAndLowerThreshold() {
        BatchGraphAwareRuntime runtime = new BatchGraphAwareRuntime(batchInserter);
        final RelationshipCountRuntimeModule module1 = new RelationshipCountRuntimeModule("M1", RelationshipCountConfigurationImpl.defaultConfiguration().with(new ThresholdBasedCompactionStrategy(4)));
        final RelationshipCountRuntimeModule module2 = new RelationshipCountRuntimeModule("M2", RelationshipCountConfigurationImpl.defaultConfiguration().with(new ThresholdBasedCompactionStrategy(4)));
        runtime.registerModule(module1);
        runtime.registerModule(module2);
        runtime.start();

        setUpTwoNodes();

        for (int i = 0; i < 20; i++) {
            simulateInserts();
        }

        startDatabase();

        verifyCounts(20, new NaiveRelationshipCounter(database, "M1"));
        verifyCompactedCounts(20, new CachedRelationshipCounter(database, "M1"));
        verifyCounts(20, new FallbackRelationshipCounter(database, "M1"));

        verifyCounts(20, new NaiveRelationshipCounter(database, "M2"));
        verifyCompactedCounts(20, new CachedRelationshipCounter(database, "M2"));
        verifyCounts(20, new FallbackRelationshipCounter(database, "M2"));
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
