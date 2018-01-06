/*
 * Copyright (c) 2013-2016 GraphAware
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

package com.graphaware.lifecycle.config;

import com.graphaware.test.integration.GraphAwareIntegrationTest;
import org.junit.Test;
import org.neo4j.graphdb.Transaction;

import static com.graphaware.test.unit.GraphUnit.assertSameGraph;
import static com.graphaware.test.util.TestUtils.waitFor;
import static org.junit.Assert.assertTrue;

public class FullCustomConfigExpiryTest extends GraphAwareIntegrationTest {

    private static final long SECOND = 1_000;

    @Override
    protected String configFile() {
        return "neo4j-expire-full-custom.conf";
    }

    @Test
    public void shouldExpireNodesAndRelationshipsWhenExpiryDateReached() {
        getDatabase().execute("CREATE (w:Warmup)");
        getDatabase().execute("MATCH (n) DETACH DELETE n");

        long now = System.currentTimeMillis();
        long fiveSecondsFromNow = now + 5 * SECOND;
        long sixSecondsFromNow = now + 6 * SECOND;

        getDatabase().execute("CREATE (s1:State {name:'Cloudy', _expire:" + fiveSecondsFromNow + "})-[:THEN]->(s2:NotAState {name:'Windy', _expire:" + sixSecondsFromNow + "})");

        assertSameGraph(getDatabase(), "CREATE (s1:State {name:'Cloudy', _expire:" + fiveSecondsFromNow + "})-[:THEN]->(s2:NotAState {name:'Windy', _expire:" + sixSecondsFromNow + "})");

        waitFor(5100 - (System.currentTimeMillis() - now));

        //force deleted relationship
        assertSameGraph(getDatabase(), "CREATE (s2:NotAState {name:'Windy', _expire:" + sixSecondsFromNow + "})");

        waitFor(6100 - (System.currentTimeMillis() - now));

        //not deleted because of inclusion policies
        assertSameGraph(getDatabase(), "CREATE (s2:NotAState {name:'Windy', _expire:" + sixSecondsFromNow + "})");

        try (Transaction tx = getDatabase().beginTx()) {
            assertTrue(getDatabase().index().existsForNodes("nodeExp"));
            assertTrue(getDatabase().index().existsForRelationships("relExp"));
            tx.success();
        }
    }
}
