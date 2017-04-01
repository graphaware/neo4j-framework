/*
 * Copyright (c) 2013-2017 GraphAware
 *
 * This file is part of the GraphAware Framework.
 *
 * GraphAware Framework is free software: you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received a copy of
 * the GNU General Public License along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 */

package com.graphaware.common.policy.inclusion.spel;

import org.junit.After;
import org.junit.Before;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.test.TestGraphDatabaseFactory;

import static com.graphaware.common.util.DatabaseUtils.registerShutdownHook;
import static com.graphaware.common.util.IterableUtils.getSingle;
import static org.neo4j.graphdb.Direction.OUTGOING;
import static org.neo4j.graphdb.Label.label;
import static org.neo4j.graphdb.RelationshipType.withName;

/**
 * Abstract base class for {@link com.graphaware.common.policy.inclusion.spel.SpelInclusionPolicy} implementation unit tests.
 */
public abstract class SpelInclusionPolicyTest {

    protected GraphDatabaseService database;

    @Before
    public void setUp() {
        database = new TestGraphDatabaseFactory().newImpermanentDatabase();
        registerShutdownHook(database);

        database.execute("CREATE " +
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
        return getSingle(database.findNodes(label("Employee"), "name", "Michal"));
    }

    protected Node vojta() {
        return getSingle(database.findNodes(label("Intern"), "name", "Vojta"));
    }

    protected Node graphaware() {
        return getSingle(database.findNodes(label("Company"), "name", "GraphAware"));
    }

    protected Node london() {
        return getSingle(database.findNodes(label("Place"), "name", "London"));
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
