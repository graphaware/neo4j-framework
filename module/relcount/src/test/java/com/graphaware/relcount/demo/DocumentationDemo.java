package com.graphaware.relcount.demo;

import com.graphaware.common.description.relationship.RelationshipDescription;
import com.graphaware.common.description.relationship.RelationshipDescriptionFactory;
import com.graphaware.common.strategy.RelationshipInclusionStrategy;
import com.graphaware.common.strategy.RelationshipPropertyInclusionStrategy;
import com.graphaware.framework.GraphAwareFramework;
import com.graphaware.relcount.compact.ThresholdBasedCompactionStrategy;
import com.graphaware.relcount.count.RelationshipCounter;
import com.graphaware.relcount.count.UnableToCountException;
import com.graphaware.relcount.count.WeighingStrategy;
import com.graphaware.relcount.module.RelationshipCountModule;
import com.graphaware.relcount.module.RelationshipCountStrategies;
import com.graphaware.relcount.module.RelationshipCountStrategiesImpl;
import org.junit.Test;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import static com.graphaware.common.description.predicate.Predicates.equalTo;
import static com.graphaware.common.description.relationship.RelationshipDescriptionFactory.literal;
import static com.graphaware.common.description.relationship.RelationshipDescriptionFactory.wildcard;
import static com.graphaware.relcount.demo.BaseDocumentationDemo.Rels.FOLLOWS;
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
        GraphAwareFramework framework = new GraphAwareFramework(database);
        RelationshipCountModule module = new RelationshipCountModule();
        framework.registerModule(module);
        framework.start();

        populateDatabase();

        RelationshipCounter counter = module.cachedCounter();
        //alternatively:
//        RelationshipCounter counter = new CachedRelationshipCounter();

        Node tracy = database.getNodeById(2);

        assertEquals(9, counter.count(tracy, wildcard(FOLLOWS, INCOMING)));
        assertEquals(9, counter.count(tracy, wildcard(FOLLOWS, OUTGOING)));
        assertEquals(1, counter.count(tracy, literal(FOLLOWS, INCOMING)));

        assertEquals(4, counter.count(tracy, wildcard(FOLLOWS, OUTGOING).with(STRENGTH, equalTo(1))));
        assertEquals(3, counter.count(tracy, wildcard(FOLLOWS, INCOMING).with(STRENGTH, equalTo(2))));
    }

    @Test
    public void demonstrateFullCachedRelationshipCounterWithCustomThreshold() {
        GraphAwareFramework framework = new GraphAwareFramework(database);

        RelationshipCountStrategies relationshipCountStrategies = RelationshipCountStrategiesImpl.defaultStrategies().with(new ThresholdBasedCompactionStrategy(7));
        RelationshipCountModule module = new RelationshipCountModule(relationshipCountStrategies);

        framework.registerModule(module);
        framework.start();

        populateDatabase();

        RelationshipCounter counter = module.cachedCounter();

        Node tracy = database.getNodeById(2);

        RelationshipDescription followers = RelationshipDescriptionFactory.wildcard(FOLLOWS, INCOMING);
        assertEquals(9, counter.count(tracy, followers));
        assertEquals(9, counter.count(tracy, wildcard(FOLLOWS, OUTGOING)));

        assertEquals(4, counter.count(tracy, wildcard(FOLLOWS, OUTGOING).with(STRENGTH, equalTo(1))));
        assertEquals(3, counter.count(tracy, wildcard(FOLLOWS, INCOMING).with(STRENGTH, equalTo(2))));
    }

    @Test
    public void demonstrateFullCachedRelationshipCounterWithCustomLowerThreshold() {
        GraphAwareFramework framework = new GraphAwareFramework(database);

        RelationshipCountStrategies relationshipCountStrategies = RelationshipCountStrategiesImpl.defaultStrategies().with(new ThresholdBasedCompactionStrategy(3));
        RelationshipCountModule module = new RelationshipCountModule(relationshipCountStrategies);

        framework.registerModule(module);
        framework.start();

        populateDatabase();

        Node tracy = database.getNodeById(2);

        RelationshipCounter counter = module.cachedCounter();
        assertEquals(9, counter.count(tracy, wildcard(FOLLOWS, INCOMING)));

        try {
            counter.count(tracy, wildcard(FOLLOWS, INCOMING).with(STRENGTH, equalTo(2)));
            fail();
        } catch (UnableToCountException e) {
            //ok
        }

        assertEquals(9, counter.count(tracy, wildcard(FOLLOWS, OUTGOING)));
    }

    @Test
    public void demonstrateFullCachedRelationshipCounterWithCustomThresholdAndWeighingStrategy() {
        GraphAwareFramework framework = new GraphAwareFramework(database);

        WeighingStrategy customWeighingStrategy = new WeighingStrategy() {
            @Override
            public int getRelationshipWeight(Relationship relationship, Node pointOfView) {
                return (int) relationship.getProperty(STRENGTH, 1);
            }

            @Override
            public String asString() {
                return "custom";
            }
        };

        RelationshipCountStrategies relationshipCountStrategies = RelationshipCountStrategiesImpl.defaultStrategies()
                .with(new ThresholdBasedCompactionStrategy(7))
                .with(customWeighingStrategy);

        RelationshipCountModule module = new RelationshipCountModule(relationshipCountStrategies);

        framework.registerModule(module);
        framework.start();

        populateDatabase();

        Node tracy = database.getNodeById(2);

        RelationshipCounter counter = module.cachedCounter();

        assertEquals(12, counter.count(tracy, wildcard(FOLLOWS, INCOMING)));
        assertEquals(11, counter.count(tracy, wildcard(FOLLOWS, OUTGOING)));

        assertEquals(4, counter.count(tracy, wildcard(FOLLOWS, OUTGOING).with(STRENGTH, equalTo(1))));
        assertEquals(6, counter.count(tracy, wildcard(FOLLOWS, INCOMING).with(STRENGTH, equalTo(2))));
    }

    @Test
    public void demonstrateFullCachedRelationshipCounterWithCustomRelationshipInclusionStrategy() {
        GraphAwareFramework framework = new GraphAwareFramework(database);

        RelationshipInclusionStrategy customRelationshipInclusionStrategy = new RelationshipInclusionStrategy() {
            @Override
            public boolean include(Relationship relationship) {
                return relationship.isType(FOLLOWS);
            }

            @Override
            public String asString() {
                return "followsOnly";
            }
        };

        RelationshipCountStrategies relationshipCountStrategies = RelationshipCountStrategiesImpl.defaultStrategies()
                .with(customRelationshipInclusionStrategy);

        RelationshipCountModule module = new RelationshipCountModule(relationshipCountStrategies);

        framework.registerModule(module);
        framework.start();

        populateDatabase();

        Node tracy = database.getNodeById(2);

        RelationshipCounter counter = module.cachedCounter();

        assertEquals(9, counter.count(tracy, wildcard(FOLLOWS, INCOMING)));
        assertEquals(9, counter.count(tracy, wildcard(FOLLOWS, OUTGOING)));

        assertEquals(4, counter.count(tracy, wildcard(FOLLOWS, OUTGOING).with(STRENGTH, equalTo(1))));
        assertEquals(3, counter.count(tracy, wildcard(FOLLOWS, INCOMING).with(STRENGTH, equalTo(2))));
    }

    @Test
    public void demonstrateFullCachedRelationshipCounterWithCustomRelationshipPropertyInclusionStrategy() {
        GraphAwareFramework framework = new GraphAwareFramework(database);

        RelationshipPropertyInclusionStrategy customRelationshipPropertyInclusionStrategy = new RelationshipPropertyInclusionStrategy() {
            @Override
            public boolean include(String key, Relationship propertyContainer) {
                return !"timestamp".equals(key);
            }

            @Override
            public String asString() {
                return "noTimestamp";
            }
        };

        RelationshipCountStrategies relationshipCountStrategies = RelationshipCountStrategiesImpl.defaultStrategies()
                .with(customRelationshipPropertyInclusionStrategy);

        RelationshipCountModule module = new RelationshipCountModule(relationshipCountStrategies);

        framework.registerModule(module);
        framework.start();

        populateDatabase();

        Node tracy = database.getNodeById(2);

        RelationshipCounter counter = module.cachedCounter();

        assertEquals(9, counter.count(tracy, wildcard(FOLLOWS, INCOMING)));
        assertEquals(9, counter.count(tracy, wildcard(FOLLOWS, OUTGOING)));

        assertEquals(4, counter.count(tracy, wildcard(FOLLOWS, OUTGOING).with(STRENGTH, equalTo(1))));
        assertEquals(3, counter.count(tracy, wildcard(FOLLOWS, INCOMING).with(STRENGTH, equalTo(2))));
    }

    @Test
    public void demonstrateFullNaiveRelationshipCounter() {
        populateDatabase();

        RelationshipCountModule module = new RelationshipCountModule();

        Node tracy = database.getNodeById(2);

        RelationshipCounter counter = module.naiveCounter();

        assertEquals(9, counter.count(tracy, wildcard(FOLLOWS, INCOMING)));
        assertEquals(9, counter.count(tracy, wildcard(FOLLOWS, OUTGOING)));

        assertEquals(4, counter.count(tracy, wildcard(FOLLOWS, OUTGOING).with(STRENGTH, equalTo(1))));
        assertEquals(3, counter.count(tracy, wildcard(FOLLOWS, INCOMING).with(STRENGTH, equalTo(2))));
    }

    @Test
    public void demonstrateFullFallingBackRelationshipCounterWithCustomLowerThreshold() {
        GraphAwareFramework framework = new GraphAwareFramework(database);

        RelationshipCountStrategies relationshipCountStrategies = RelationshipCountStrategiesImpl.defaultStrategies().with(new ThresholdBasedCompactionStrategy(3));
        RelationshipCountModule module = new RelationshipCountModule(relationshipCountStrategies);

        framework.registerModule(module);
        framework.start();

        populateDatabase();

        Node tracy = database.getNodeById(2);

        RelationshipCounter counter = module.fallbackCounter();

        assertEquals(9, counter.count(tracy, wildcard(FOLLOWS, INCOMING))); //uses cache
        assertEquals(3, counter.count(tracy, wildcard(FOLLOWS, INCOMING).with(STRENGTH, equalTo(2)))); //falls back to naive
    }
}
