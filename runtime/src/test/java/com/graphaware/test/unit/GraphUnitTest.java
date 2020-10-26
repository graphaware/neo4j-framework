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

package com.graphaware.test.unit;

import com.graphaware.common.junit.InjectNeo4j;
import com.graphaware.common.junit.Neo4jExtension;
import com.graphaware.runtime.GraphAwareRuntime;
import com.graphaware.runtime.GraphAwareRuntimeFactory;
import com.graphaware.runtime.bootstrap.TestModule;
import com.graphaware.runtime.policy.InclusionPoliciesFactory;
import org.apache.commons.configuration2.MapConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
import org.neo4j.harness.Neo4j;

import java.util.Collections;

import static com.graphaware.common.util.IterableUtils.count;
import static com.graphaware.test.unit.GraphUnit.assertSameGraph;
import static com.graphaware.test.unit.GraphUnit.clearGraph;
import static org.junit.jupiter.api.Assertions.assertEquals;


/**
 * Test for {@link com.graphaware.test.unit.GraphUnit} when Runtime is present.
 */
@ExtendWith(Neo4jExtension.class)
public class GraphUnitTest {

    @InjectNeo4j
    private Neo4j neo4j;

    @InjectNeo4j
    private GraphDatabaseService database;
    
    private void populateDatabase(String cypher) {
        database.executeTransactionally(cypher);
    }

    @Test
    public void clearGraphWithRuntimeShouldDeleteAllNodesAndRelsButNotGraphProps() {
        GraphAwareRuntime runtime = GraphAwareRuntimeFactory.createRuntime(neo4j.databaseManagementService(), database);
        runtime.registerModule(new TestModule("test", new MapConfiguration(Collections.singletonMap("test", "test"))));
        runtime.start();

        try (Transaction tx = database.beginTx()) {
            String cypher = "CREATE " +
                    "(blue:Blue {name:'Blue'})<-[:REL]-(red1:Red {name:'Red'})-[:REL]->(black1:Black {name:'Black'})-[:REL]->(green:Green {name:'Green'})," +
                    "(red2:Red {name:'Red'})-[:REL]->(black2:Black {name:'Black'})";

            populateDatabase(cypher);
            tx.commit();
        }

        try (Transaction tx = database.beginTx()) {
            clearGraph(tx, InclusionPoliciesFactory.allBusiness());
            tx.commit();
        }

        try (Transaction tx = database.beginTx()) {
            assertEquals(0, count(tx.getAllNodes()));
            tx.commit();
        }

        runtime.stop();
    }

    @Test
    public void equalGraphsWithRuntimeShouldPassSameGraphTestBusinessStrategies() {
        GraphAwareRuntime runtime = GraphAwareRuntimeFactory.createRuntime(neo4j.databaseManagementService(), database);
        runtime.start();

        String assertCypher = "CREATE " +
                "(blue:Blue {name:'Blue'})<-[:REL]-(red1:Red {name:'Red'})-[:REL]->(black1:Black {name:'Black'})-[:REL]->(green:Green {name:'Green'})," +
                "(red2:Red {name:'Red'})-[:REL]->(black2:Black {name:'Black'})";
        populateDatabase(assertCypher);

        assertSameGraph(database, assertCypher, InclusionPoliciesFactory.allBusiness());

        runtime.stop();
    }
}
