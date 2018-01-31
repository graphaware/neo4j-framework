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

import static com.graphaware.test.unit.GraphUnit.assertEmpty;
import static com.graphaware.test.unit.GraphUnit.assertSameGraph;
import static com.graphaware.test.util.TestUtils.waitFor;

public class MinimalConfigTtlTest extends GraphAwareIntegrationTest {

    @Override
    protected String configFile() {
        return "neo4j-ttl-minimal.conf";
    }

    @Test
    public void shouldExpireNodesAndRelationshipsWhenExpiryDateReached() {
        getDatabase().execute("CREATE (s1:State {name:'Cloudy', timeToLive:2000})-[:THEN {timeToLive:2000}]->" +
                "(s2:State {name:'Windy', timeToLive:4000})");

        assertSameGraph(getDatabase(), "CREATE (s1:State {name:'Cloudy', timeToLive:2000})-[:THEN " +
                "{timeToLive:2000}]->(s2:State {name:'Windy', timeToLive:4000})");

        waitFor(2200);

        assertSameGraph(getDatabase(), "CREATE (s2:State {name:'Windy', timeToLive:4000})");

        waitFor(2200);

        assertEmpty(getDatabase());
    }
}
