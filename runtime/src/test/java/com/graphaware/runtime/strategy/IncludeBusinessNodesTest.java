package com.graphaware.runtime.strategy;

import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.test.TestGraphDatabaseFactory;

import static com.graphaware.common.description.predicate.Predicates.equalTo;
import static com.graphaware.common.description.predicate.Predicates.undefined;
import static com.graphaware.runtime.config.RuntimeConfiguration.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.neo4j.graphdb.DynamicLabel.label;

/**
 * Unit test for  {@link IncludeBusinessNodes}.
 */
public class IncludeBusinessNodesTest {

    @Test
    public void shouldIncludeCorrectRelationships() {
        GraphDatabaseService database = new TestGraphDatabaseFactory().newImpermanentDatabase();

        try (Transaction tx = database.beginTx()) {
            Node n = database.createNode(label("Test"));
            n.setProperty("test", "test");
            Node internal = database.createNode(label(GA_PREFIX + "test"));

            assertTrue(IncludeBusinessNodes.all().include(n));
            assertFalse(IncludeBusinessNodes.all().include(internal));
            assertTrue(IncludeBusinessNodes.all().with(label("Test")).include(n));
            assertFalse(IncludeBusinessNodes.all().with("Test").include(internal));
            assertFalse(IncludeBusinessNodes.all().with(label("Test2")).include(n));
            assertTrue(IncludeBusinessNodes.all().with(label("Bla")).with((Label) null).include(n));

            assertTrue(
                    IncludeBusinessNodes
                            .all()
                            .with("test", equalTo("test")).include(n));

            assertFalse(
                    IncludeBusinessNodes
                            .all()
                            .with("test", equalTo("test")).include(internal));

            assertFalse(
                    IncludeBusinessNodes
                            .all()
                            .with("test", equalTo("test2")).include(n));

            assertFalse(
                    IncludeBusinessNodes
                            .all()
                            .with("test", undefined()).include(n));

            tx.success();
        }
    }
}
