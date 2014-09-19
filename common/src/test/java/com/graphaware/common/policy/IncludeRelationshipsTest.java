package com.graphaware.common.policy;

import com.graphaware.common.policy.fluent.IncludeRelationships;
import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.test.TestGraphDatabaseFactory;

import static com.graphaware.common.description.predicate.Predicates.equalTo;
import static com.graphaware.common.description.predicate.Predicates.undefined;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.neo4j.graphdb.Direction.*;
import static org.neo4j.graphdb.DynamicRelationshipType.withName;

/**
 * Unit test for  {@link com.graphaware.common.policy.fluent.IncludeRelationships}.
 */
public class IncludeRelationshipsTest {

    @Test
    public void shouldIncludeCorrectRelationships() {
        GraphDatabaseService database = new TestGraphDatabaseFactory().newImpermanentDatabase();

        try (Transaction tx = database.beginTx()) {
            Node n1 = database.createNode();
            Node n2 = database.createNode();
            Relationship r = n1.createRelationshipTo(n2, withName("TEST"));
            r.setProperty("test", "test");

            assertTrue(IncludeRelationships.all().include(r));
            assertTrue(IncludeRelationships.all().with(OUTGOING).include(r, n1));
            assertFalse(IncludeRelationships.all().with(INCOMING).include(r, n1));
            assertFalse(IncludeRelationships.all().with(OUTGOING).include(r, n2));
            assertTrue(IncludeRelationships.all().with(INCOMING).include(r, n2));

            assertTrue(IncludeRelationships.all().with(BOTH, "TEST").include(r));
            assertTrue(IncludeRelationships.all().with(BOTH, withName("TEST"), withName("TEST2")).include(r));
            assertFalse(IncludeRelationships.all().with(BOTH, withName("TEST2"), withName("TEST3")).include(r));

            assertTrue(IncludeRelationships.all().with(BOTH, withName("TEST")).include(r));
            assertTrue(IncludeRelationships.all().with(OUTGOING, withName("TEST"), withName("TEST2")).include(r, n1));
            assertFalse(IncludeRelationships.all().with(OUTGOING, withName("TEST"), withName("TEST2")).include(r, n2));
            assertFalse(IncludeRelationships.all().with(BOTH, "TEST2", "TEST3").include(r, n1));
            assertFalse(IncludeRelationships.all().with(BOTH, withName("TEST2"), withName("TEST3")).include(r, n2));

            assertTrue(
                    IncludeRelationships
                            .all()
                            .with(BOTH, withName("TEST"))
                            .with("test", equalTo("test")).include(r));

            assertFalse(
                    IncludeRelationships
                            .all()
                            .with(BOTH, "TEST")
                            .with("test", equalTo("test2")).include(r));

            assertFalse(
                    IncludeRelationships
                            .all()
                            .with(BOTH, withName("TEST"))
                            .with("test", undefined()).include(r));

            tx.success();
        }
    }
}
