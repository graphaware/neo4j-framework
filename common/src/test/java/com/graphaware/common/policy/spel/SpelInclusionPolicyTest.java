package com.graphaware.common.policy.spel;

import org.junit.After;
import org.junit.Before;
import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.graphdb.*;
import org.neo4j.test.TestGraphDatabaseFactory;

import static com.graphaware.common.util.IterableUtils.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.neo4j.graphdb.Direction.*;
import static org.neo4j.graphdb.DynamicLabel.*;
import static org.neo4j.graphdb.DynamicRelationshipType.*;

/**
 * Abstract base class for {@link com.graphaware.common.policy.spel.SpelInclusionPolicy} implementation unit tests.
 */
public abstract class SpelInclusionPolicyTest {

    protected GraphDatabaseService database;

    @Before
    public void setUp() {
        database = new TestGraphDatabaseFactory().newImpermanentDatabase();

        new ExecutionEngine(database).execute("CREATE " +
                "(m:Employee {name:'Michal'})-[:WORKS_FOR {role:'Director', since:2013}]->(ga:Company {name:'GraphAware', form:'Ltd'})," +
                "(v:Intern {name:'Vojta', age:25})-[:WORKS_FOR {since:2014, until:2014}]->(ga)," +
                "(m)-[:LIVES_IN]->(l:Place {name:'London'})<-[:LIVES_IN]-(v)"
        );
    }

    @After
    public void tearDown() {
        database.shutdown();
    }

    protected Node michal() {
        return getSingle(database.findNodesByLabelAndProperty(label("Employee"), "name", "Michal"));
    }

    protected Node vojta() {
        return getSingle(database.findNodesByLabelAndProperty(label("Intern"), "name", "Vojta"));
    }

    protected Node graphaware() {
        return getSingle(database.findNodesByLabelAndProperty(label("Company"), "name", "GraphAware"));
    }

    protected Node london() {
        return getSingle(database.findNodesByLabelAndProperty(label("Place"), "name", "London"));
    }

    protected Relationship michalWorksFor() {
        return michal().getSingleRelationship(withName("WORKS_FOR"), OUTGOING);
    }

    protected Relationship michalLivesIn() {
        return michal().getSingleRelationship(withName("LIVES_IN"), OUTGOING);
    }

    protected Relationship vojtaWorksFor() {
        return vojta().getSingleRelationship(withName("WORKS_FOR"), OUTGOING);
    }

    protected Relationship vojtaLivesIn() {
        return vojta().getSingleRelationship(withName("LIVES_IN"), OUTGOING);
    }
}
