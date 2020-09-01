/*
 * Copyright (c) 2013-2020 GraphAware
 *
 * This file is part of the GraphAware Framework.
 *
 * GraphAware Framework is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received a copy of
 * the GNU General Public License along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 */

package com.graphaware.common.policy.inclusion.spel;

import com.graphaware.common.UnitTest;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;

import static com.graphaware.common.util.IterableUtils.getSingle;
import static org.neo4j.graphdb.Direction.OUTGOING;
import static org.neo4j.graphdb.Label.label;
import static org.neo4j.graphdb.RelationshipType.withName;

/**
 * Abstract base class for {@link com.graphaware.common.policy.inclusion.spel.SpelInclusionPolicy} implementation unit tests.
 */
public abstract class SpelInclusionPolicyTest extends UnitTest {

    @Override
    protected void populate(GraphDatabaseService database) {
        database.executeTransactionally("CREATE " +
                "(m:Employee {name:'Michal'})-[:WORKS_FOR {role:'Director', since:2013}]->(ga:Company {name:'GraphAware', form:'Ltd'})," +
                "(v:Intern {name:'Vojta', age:25})-[:WORKS_FOR {since:2014, until:2014}]->(ga)," +
                "(m)-[:LIVES_IN]->(l:Place {name:'London'})<-[:LIVES_IN]-(v)"
        );
    }

    protected Node michal(Transaction database) {
        return getSingle(database.findNodes(label("Employee"), "name", "Michal"));
    }

    protected Node vojta(Transaction database) {
        return getSingle(database.findNodes(label("Intern"), "name", "Vojta"));
    }

    protected Node graphaware(Transaction database) {
        return getSingle(database.findNodes(label("Company"), "name", "GraphAware"));
    }

    protected Node london(Transaction database) {
        return getSingle(database.findNodes(label("Place"), "name", "London"));
    }

    protected Relationship michalWorksFor(Transaction database) {
        return michal(database).getSingleRelationship(withName("WORKS_FOR"), OUTGOING);
    }

    protected Relationship michalLivesIn(Transaction database) {
        return michal(database).getSingleRelationship(withName("LIVES_IN"), OUTGOING);
    }

    protected Relationship vojtaWorksFor(Transaction database) {
        return vojta(database).getSingleRelationship(withName("WORKS_FOR"), OUTGOING);
    }

    protected Relationship vojtaLivesIn(Transaction database) {
        return vojta(database).getSingleRelationship(withName("LIVES_IN"), OUTGOING);
    }
}
