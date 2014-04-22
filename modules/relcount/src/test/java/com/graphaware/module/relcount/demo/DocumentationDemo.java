package com.graphaware.module.relcount.demo;

import com.graphaware.common.description.relationship.RelationshipDescription;
import com.graphaware.common.description.relationship.RelationshipDescriptionFactory;
import com.graphaware.common.strategy.RelationshipInclusionStrategy;
import com.graphaware.common.strategy.RelationshipPropertyInclusionStrategy;
import com.graphaware.module.relcount.RelationshipCountConfiguration;
import com.graphaware.module.relcount.RelationshipCountConfigurationImpl;
import com.graphaware.module.relcount.RelationshipCountRuntimeModule;
import com.graphaware.module.relcount.compact.ThresholdBasedCompactionStrategy;
import com.graphaware.module.relcount.count.*;
import com.graphaware.runtime.ProductionGraphAwareRuntime;
import org.junit.Test;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;

import static com.graphaware.common.description.predicate.Predicates.equalTo;
import static com.graphaware.common.description.relationship.RelationshipDescriptionFactory.literal;
import static com.graphaware.common.description.relationship.RelationshipDescriptionFactory.wildcard;
import static com.graphaware.module.relcount.demo.BaseDocumentationDemo.Rels.FOLLOWS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.neo4j.graphdb.Direction.INCOMING;
import static org.neo4j.graphdb.Direction.OUTGOING;

/**
 * Demonstration of different full relationship counting options.
 */
public class DocumentationDemo extends BaseDocumentationDemo {

    @Test
    public void demonstrateCachedRelationshipCounter() {
        ProductionGraphAwareRuntime runtime = new ProductionGraphAwareRuntime(database);
        RelationshipCountRuntimeModule module = new RelationshipCountRuntimeModule();
        runtime.registerModule(module);
        runtime.start();

        populateDatabase();

        RelationshipCounter counter = new CachedRelationshipCounter(database);
        //alternatively:
//        RelationshipCounter counter = new CachedRelationshipCounter();

        try (Transaction tx = database.beginTx()) {

            Node tracy = database.getNodeById(2);

            assertEquals(9, counter.count(tracy, wildcard(FOLLOWS, INCOMING)));
            assertEquals(9, counter.count(tracy, wildcard(FOLLOWS, OUTGOING)));
            assertEquals(1, counter.count(tracy, literal(FOLLOWS, INCOMING)));

            assertEquals(4, counter.count(tracy, wildcard(FOLLOWS, OUTGOING).with(STRENGTH, equalTo(1))));
            assertEquals(3, counter.count(tracy, wildcard(FOLLOWS, INCOMING).with(STRENGTH, equalTo(2))));

            tx.success();
        }
    }

    @Test
    public void demonstrateFullCachedRelationshipCounterWithCustomThreshold() {
        ProductionGraphAwareRuntime runtime = new ProductionGraphAwareRuntime(database);

        RelationshipCountConfiguration relationshipCountConfiguration = RelationshipCountConfigurationImpl.defaultConfiguration().with(new ThresholdBasedCompactionStrategy(7));
        RelationshipCountRuntimeModule module = new RelationshipCountRuntimeModule(relationshipCountConfiguration);

        runtime.registerModule(module);
        runtime.start();

        populateDatabase();

        RelationshipCounter counter = new CachedRelationshipCounter(database);

        try (Transaction tx = database.beginTx()) {

            Node tracy = database.getNodeById(2);

            RelationshipDescription followers = RelationshipDescriptionFactory.wildcard(FOLLOWS, INCOMING);
            assertEquals(9, counter.count(tracy, followers));
            assertEquals(9, counter.count(tracy, wildcard(FOLLOWS, OUTGOING)));

            assertEquals(4, counter.count(tracy, wildcard(FOLLOWS, OUTGOING).with(STRENGTH, equalTo(1))));
            assertEquals(3, counter.count(tracy, wildcard(FOLLOWS, INCOMING).with(STRENGTH, equalTo(2))));

            tx.success();
        }
    }

    @Test
    public void demonstrateFullCachedRelationshipCounterWithCustomLowerThreshold() {
        ProductionGraphAwareRuntime runtime = new ProductionGraphAwareRuntime(database);

        RelationshipCountConfiguration relationshipCountConfiguration = RelationshipCountConfigurationImpl.defaultConfiguration().with(new ThresholdBasedCompactionStrategy(3));
        RelationshipCountRuntimeModule module = new RelationshipCountRuntimeModule(relationshipCountConfiguration);

        runtime.registerModule(module);
        runtime.start();

        populateDatabase();

        try (Transaction tx = database.beginTx()) {

            Node tracy = database.getNodeById(2);

            RelationshipCounter counter = new CachedRelationshipCounter(database);
            assertEquals(9, counter.count(tracy, wildcard(FOLLOWS, INCOMING)));

            try {
                counter.count(tracy, wildcard(FOLLOWS, INCOMING).with(STRENGTH, equalTo(2)));
                fail();
            } catch (UnableToCountException e) {
                //ok
            }

            assertEquals(9, counter.count(tracy, wildcard(FOLLOWS, OUTGOING)));

            tx.success();
        }
    }

    @Test
    public void demonstrateFullCachedRelationshipCounterWithCustomThresholdAndWeighingStrategy() {
        ProductionGraphAwareRuntime runtime = new ProductionGraphAwareRuntime(database);

        WeighingStrategy customWeighingStrategy = new WeighingStrategy() {
            @Override
            public int getRelationshipWeight(Relationship relationship, Node pointOfView) {
                return (int) relationship.getProperty(STRENGTH, 1);
            }
        };

        RelationshipCountConfiguration relationshipCountConfiguration = RelationshipCountConfigurationImpl.defaultConfiguration()
                .with(new ThresholdBasedCompactionStrategy(7))
                .with(customWeighingStrategy);

        RelationshipCountRuntimeModule module = new RelationshipCountRuntimeModule(relationshipCountConfiguration);

        runtime.registerModule(module);
        runtime.start();

        populateDatabase();

        try (Transaction tx = database.beginTx()) {

            Node tracy = database.getNodeById(2);

            RelationshipCounter counter = new CachedRelationshipCounter(database);

            assertEquals(12, counter.count(tracy, wildcard(FOLLOWS, INCOMING)));
            assertEquals(11, counter.count(tracy, wildcard(FOLLOWS, OUTGOING)));

            assertEquals(4, counter.count(tracy, wildcard(FOLLOWS, OUTGOING).with(STRENGTH, equalTo(1))));
            assertEquals(6, counter.count(tracy, wildcard(FOLLOWS, INCOMING).with(STRENGTH, equalTo(2))));


            tx.success();
        }
    }

    @Test
    public void demonstrateFullCachedRelationshipCounterWithCustomRelationshipInclusionStrategy() {
        ProductionGraphAwareRuntime runtime = new ProductionGraphAwareRuntime(database);

        RelationshipInclusionStrategy customRelationshipInclusionStrategy = new RelationshipInclusionStrategy() {
            @Override
            public boolean include(Relationship relationship) {
                return relationship.isType(FOLLOWS);
            }
        };

        RelationshipCountConfiguration relationshipCountConfiguration = RelationshipCountConfigurationImpl.defaultConfiguration()
                .with(customRelationshipInclusionStrategy);

        RelationshipCountRuntimeModule module = new RelationshipCountRuntimeModule(relationshipCountConfiguration);

        runtime.registerModule(module);
        runtime.start();

        populateDatabase();

        try (Transaction tx = database.beginTx()) {

            Node tracy = database.getNodeById(2);

            RelationshipCounter counter = new CachedRelationshipCounter(database);

            assertEquals(9, counter.count(tracy, wildcard(FOLLOWS, INCOMING)));
            assertEquals(9, counter.count(tracy, wildcard(FOLLOWS, OUTGOING)));

            assertEquals(4, counter.count(tracy, wildcard(FOLLOWS, OUTGOING).with(STRENGTH, equalTo(1))));
            assertEquals(3, counter.count(tracy, wildcard(FOLLOWS, INCOMING).with(STRENGTH, equalTo(2))));

            tx.success();
        }
    }

    @Test
    public void demonstrateFullCachedRelationshipCounterWithCustomRelationshipPropertyInclusionStrategy() {
        ProductionGraphAwareRuntime runtime = new ProductionGraphAwareRuntime(database);

        RelationshipPropertyInclusionStrategy customRelationshipPropertyInclusionStrategy = new RelationshipPropertyInclusionStrategy() {
            @Override
            public boolean include(String key, Relationship propertyContainer) {
                return !"timestamp".equals(key);
            }
        };

        RelationshipCountConfiguration relationshipCountConfiguration = RelationshipCountConfigurationImpl.defaultConfiguration()
                .with(customRelationshipPropertyInclusionStrategy);

        RelationshipCountRuntimeModule module = new RelationshipCountRuntimeModule(relationshipCountConfiguration);

        runtime.registerModule(module);
        runtime.start();

        populateDatabase();

        try (Transaction tx = database.beginTx()) {

            Node tracy = database.getNodeById(2);

            RelationshipCounter counter = new CachedRelationshipCounter(database);

            assertEquals(9, counter.count(tracy, wildcard(FOLLOWS, INCOMING)));
            assertEquals(9, counter.count(tracy, wildcard(FOLLOWS, OUTGOING)));

            assertEquals(4, counter.count(tracy, wildcard(FOLLOWS, OUTGOING).with(STRENGTH, equalTo(1))));
            assertEquals(3, counter.count(tracy, wildcard(FOLLOWS, INCOMING).with(STRENGTH, equalTo(2))));

            tx.success();
        }
    }

    @Test
    public void demonstrateFullNaiveRelationshipCounter() {
        populateDatabase();

        RelationshipCountRuntimeModule module = new RelationshipCountRuntimeModule();

        try (Transaction tx = database.beginTx()) {

            Node tracy = database.getNodeById(2);

            RelationshipCounter counter = new NaiveRelationshipCounter(database);

            assertEquals(9, counter.count(tracy, wildcard(FOLLOWS, INCOMING)));
            assertEquals(9, counter.count(tracy, wildcard(FOLLOWS, OUTGOING)));

            assertEquals(4, counter.count(tracy, wildcard(FOLLOWS, OUTGOING).with(STRENGTH, equalTo(1))));
            assertEquals(3, counter.count(tracy, wildcard(FOLLOWS, INCOMING).with(STRENGTH, equalTo(2))));

            tx.success();
        }
    }

    @Test
    public void demonstrateFullFallingBackRelationshipCounterWithCustomLowerThreshold() {
        ProductionGraphAwareRuntime runtime = new ProductionGraphAwareRuntime(database);

        RelationshipCountConfiguration relationshipCountConfiguration = RelationshipCountConfigurationImpl.defaultConfiguration().with(new ThresholdBasedCompactionStrategy(3));
        RelationshipCountRuntimeModule module = new RelationshipCountRuntimeModule(relationshipCountConfiguration);

        runtime.registerModule(module);
        runtime.start();

        populateDatabase();

        try (Transaction tx = database.beginTx()) {

            Node tracy = database.getNodeById(2);

            RelationshipCounter counter = new FallbackRelationshipCounter(database);

            assertEquals(9, counter.count(tracy, wildcard(FOLLOWS, INCOMING))); //uses cache
            assertEquals(3, counter.count(tracy, wildcard(FOLLOWS, INCOMING).with(STRENGTH, equalTo(2)))); //falls back to naive

            tx.success();
        }
    }
}
