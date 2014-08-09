package com.graphaware.common.strategy;

import org.junit.Test;
import org.neo4j.graphdb.*;
import org.neo4j.test.TestGraphDatabaseFactory;

import static org.junit.Assert.*;
import static org.neo4j.graphdb.Direction.*;
import static org.neo4j.graphdb.DynamicRelationshipType.*;

/**
 * Unit test for  {@link IncludeRelationships}.
 */
public class IncludeRelationshipsTest {

    @Test
    public void shouldIncludeCorrectRelationships() {
        GraphDatabaseService database = new TestGraphDatabaseFactory().newImpermanentDatabase();

        try (Transaction tx = database.beginTx()) {
            Node n1 = database.createNode();
            Node n2 = database.createNode();
            Relationship r = n1.createRelationshipTo(n2, withName("TEST"));

            assertTrue(IncludeRelationships.ALL.include(r));
            assertTrue(IncludeRelationships.OUTGOING.include(r, n1));
            assertFalse(IncludeRelationships.INCOMING.include(r, n1));
            assertFalse(IncludeRelationships.OUTGOING.include(r, n2));
            assertTrue(IncludeRelationships.INCOMING.include(r,n2));

            assertTrue(new IncludeRelationships(withName("TEST")).include(r));
            assertTrue(new IncludeRelationships(withName("TEST"),withName("TEST2")).include(r));
            assertFalse(new IncludeRelationships(withName("TEST2"), withName("TEST3")).include(r));

            assertTrue(new IncludeRelationships(withName("TEST")).include(r));
            assertTrue(new IncludeRelationships(OUTGOING, withName("TEST"), withName("TEST2")).include(r, n1));
            assertFalse(new IncludeRelationships(OUTGOING, withName("TEST"), withName("TEST2")).include(r, n2));
            assertFalse(new IncludeRelationships(BOTH, withName("TEST2"), withName("TEST3")).include(r, n1));
            assertFalse(new IncludeRelationships(withName("TEST2"), withName("TEST3")).include(r, n2));

            tx.success();
        }
    }
}
