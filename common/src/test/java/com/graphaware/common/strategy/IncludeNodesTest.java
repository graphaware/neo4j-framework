package com.graphaware.common.strategy;

import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.test.TestGraphDatabaseFactory;

import static com.graphaware.common.description.predicate.Predicates.equalTo;
import static com.graphaware.common.description.predicate.Predicates.undefined;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.neo4j.graphdb.DynamicLabel.label;

/**
 * Unit test for  {@link com.graphaware.common.strategy.IncludeNodes}.
 */
public class IncludeNodesTest {

    @Test
    public void shouldIncludeCorrectRelationships() {
        GraphDatabaseService database = new TestGraphDatabaseFactory().newImpermanentDatabase();

        try (Transaction tx = database.beginTx()) {
            Node n = database.createNode(label("Test"));
            n.setProperty("test", "test");

            assertTrue(IncludeNodes.all().include(n));
            assertTrue(IncludeNodes.all().with("Test").include(n));
            assertFalse(IncludeNodes.all().with(label("Test2")).include(n));
            assertTrue(IncludeNodes.all().with(label("Bla")).with((Label) null).include(n));

            assertTrue(
                    IncludeNodes
                            .all()
                            .with("test", equalTo("test")).include(n));

            assertFalse(
                    IncludeNodes
                            .all()
                            .with("test", equalTo("test2")).include(n));

            assertFalse(
                    IncludeNodes
                            .all()
                            .with("test", undefined()).include(n));

            tx.success();
        }
    }
}
