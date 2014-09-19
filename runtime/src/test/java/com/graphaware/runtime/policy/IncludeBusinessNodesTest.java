package com.graphaware.runtime.policy;

import com.graphaware.common.policy.composite.CompositeNodeInclusionPolicy;
import com.graphaware.common.policy.fluent.IncludeNodes;
import com.graphaware.runtime.policy.all.IncludeAllBusinessNodes;
import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.test.TestGraphDatabaseFactory;

import static com.graphaware.common.description.predicate.Predicates.equalTo;
import static com.graphaware.common.description.predicate.Predicates.undefined;
import static com.graphaware.common.policy.composite.CompositeNodeInclusionPolicy.*;
import static com.graphaware.runtime.config.RuntimeConfiguration.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.neo4j.graphdb.DynamicLabel.label;

/**
 * Test for {@link CompositeNodeInclusionPolicy} with {@link IncludeAllBusinessNodes} and a programmatically configured {@link IncludeNodes}
 */
public class IncludeBusinessNodesTest {

    @Test
    public void shouldIncludeCorrectRelationships() {
        GraphDatabaseService database = new TestGraphDatabaseFactory().newImpermanentDatabase();

        try (Transaction tx = database.beginTx()) {
            Node n = database.createNode(label("Test"));
            n.setProperty("test", "test");
            Node internal = database.createNode(label(GA_PREFIX + "test"));

            assertTrue(of(IncludeAllBusinessNodes.getInstance(), IncludeNodes.all()).include(n));
            assertFalse(of(IncludeAllBusinessNodes.getInstance(), IncludeNodes.all()).include(internal));
            assertTrue(of(IncludeAllBusinessNodes.getInstance(), IncludeNodes.all().with(label("Test"))).include(n));
            assertFalse(of(IncludeAllBusinessNodes.getInstance(), IncludeNodes.all().with("Test")).include(internal));
            assertFalse(of(IncludeAllBusinessNodes.getInstance(), IncludeNodes.all().with(label("Test2"))).include(n));
            assertTrue(of(IncludeAllBusinessNodes.getInstance(), IncludeNodes.all().with(label("Bla")).with((Label) null)).include(n));

            assertTrue(
                    of(IncludeAllBusinessNodes.getInstance(), IncludeNodes
                            .all()
                            .with("test", equalTo("test"))).include(n));

            assertFalse(
                    of(IncludeAllBusinessNodes.getInstance(), IncludeNodes
                            .all()
                            .with("test", equalTo("test"))).include(internal));

            assertFalse(
                    of(IncludeAllBusinessNodes.getInstance(), IncludeNodes
                            .all()
                            .with("test", equalTo("test2"))).include(n));

            assertFalse(
                    of(IncludeAllBusinessNodes.getInstance(), IncludeNodes
                            .all()
                            .with("test", undefined())).include(n));

            tx.success();
        }
    }
}
