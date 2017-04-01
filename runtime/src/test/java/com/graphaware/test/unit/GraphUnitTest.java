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

package com.graphaware.test.unit;

import com.graphaware.common.kv.GraphKeyValueStore;
import com.graphaware.runtime.GraphAwareRuntime;
import com.graphaware.runtime.GraphAwareRuntimeFactory;
import com.graphaware.runtime.bootstrap.TestRuntimeModule;
import com.graphaware.runtime.policy.InclusionPoliciesFactory;
import com.graphaware.test.integration.DatabaseIntegrationTest;
import com.graphaware.test.integration.EmbeddedDatabaseIntegrationTest;
import org.junit.Test;
import org.neo4j.graphdb.Transaction;

import java.util.Collections;

import static com.graphaware.common.util.IterableUtils.count;
import static com.graphaware.test.unit.GraphUnit.assertSameGraph;
import static com.graphaware.test.unit.GraphUnit.clearGraph;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


/**
 * Test for {@link com.graphaware.test.unit.GraphUnit} when Runtime is present.
 */
public class GraphUnitTest extends EmbeddedDatabaseIntegrationTest {

    private void populateDatabase(String cypher) {
        getDatabase().execute(cypher);
    }

    @Test
    public void clearGraphWithRuntimeShouldDeleteAllNodesAndRelsButNotGraphProps() {
        GraphAwareRuntime runtime = GraphAwareRuntimeFactory.createRuntime(getDatabase());
        runtime.registerModule(new TestRuntimeModule("test", Collections.singletonMap("test", "test")));
        runtime.start();

        try (Transaction tx = getDatabase().beginTx()) {
            String cypher = "CREATE " +
                    "(blue:Blue {name:'Blue'})<-[:REL]-(red1:Red {name:'Red'})-[:REL]->(black1:Black {name:'Black'})-[:REL]->(green:Green {name:'Green'})," +
                    "(red2:Red {name:'Red'})-[:REL]->(black2:Black {name:'Black'})";

            populateDatabase(cypher);
            tx.success();
        }

        try (Transaction tx = getDatabase().beginTx()) {
            clearGraph(getDatabase(), InclusionPoliciesFactory.allBusiness());
            tx.success();
        }

        try (Transaction tx = getDatabase().beginTx()) {
            assertEquals(0, count(getDatabase().getAllNodes()));
            assertTrue(new GraphKeyValueStore(getDatabase()).hasKey("_GA_TX_MODULE_test"));
            tx.success();
        }
    }

    @Test
    public void equalGraphsWithRuntimeShouldPassSameGraphTestBusinessStrategies() {
        GraphAwareRuntime runtime = GraphAwareRuntimeFactory.createRuntime(getDatabase());
        runtime.start();

        String assertCypher = "CREATE " +
                "(blue:Blue {name:'Blue'})<-[:REL]-(red1:Red {name:'Red'})-[:REL]->(black1:Black {name:'Black'})-[:REL]->(green:Green {name:'Green'})," +
                "(red2:Red {name:'Red'})-[:REL]->(black2:Black {name:'Black'})";
        populateDatabase(assertCypher);

        assertSameGraph(getDatabase(), assertCypher, InclusionPoliciesFactory.allBusiness());
    }
}
