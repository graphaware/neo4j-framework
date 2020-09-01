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

package com.graphaware.common.policy.inclusion.none;

import com.graphaware.common.UnitTest;
import com.graphaware.common.util.IterableUtils;
import org.junit.jupiter.api.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Unit test for {@link IncludeNoNodes}.
 */
public class IncludeNoNodesTest extends UnitTest {

    @Override
    protected void populate(GraphDatabaseService database) {
        database.executeTransactionally("CREATE " +
                "(m:Employee {name:'Michal'})-[:WORKS_FOR {role:'Director', since:2013}]->(ga:Company {name:'GraphAware', form:'Ltd'})," +
                "(v:Intern {name:'Vojta', age:25})-[:WORKS_FOR {since:2014, until:2014}]->(ga)," +
                "(m)-[:LIVES_IN]->(l:Place {name:'London'})<-[:LIVES_IN]-(v)"
        );
    }

    @Test
    public void shouldIncludeNoNodes() {
        try (Transaction tx = database.beginTx()) {
            for (Node node : tx.getAllNodes()) {
                assertFalse(IncludeNoNodes.getInstance().include(node));
            }
            tx.commit();
        }
    }

    @Test
    public void shouldGetNoNodes() {
        try (Transaction tx = database.beginTx()) {
            assertEquals(0, IterableUtils.count(IncludeNoNodes.getInstance().getAll(tx)));
            tx.commit();
        }
    }
}
