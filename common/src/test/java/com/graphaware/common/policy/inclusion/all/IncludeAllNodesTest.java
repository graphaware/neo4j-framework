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

package com.graphaware.common.policy.inclusion.all;

import com.graphaware.common.util.IterableUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.test.TestGraphDatabaseFactory;

import static com.graphaware.common.util.DatabaseUtils.registerShutdownHook;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Unit test for {@link IncludeAllNodes}.
 */
public class IncludeAllNodesTest {

    private GraphDatabaseService database;

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

    @Test
    public void shouldIncludeAllNodes() {
        try (Transaction tx = database.beginTx()) {
            for (Node node : database.getAllNodes()) {
                assertTrue(IncludeAllNodes.getInstance().include(node));
            }
            tx.success();
        }
    }

    @Test
    public void shouldGetAllNodes() {
        try (Transaction tx = database.beginTx()) {
            assertEquals(4, IterableUtils.count(IncludeAllNodes.getInstance().getAll(database)));
            tx.success();
        }
    }
}
