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

package com.graphaware.common.util;

import com.graphaware.common.junit.InjectNeo4j;
import com.graphaware.common.junit.InjectNeo4j.Lifecycle;
import com.graphaware.common.junit.Neo4jExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;

import java.util.Collections;
import java.util.Map;

import static com.graphaware.common.util.Change.changesToMap;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.*;

/**
 *  Unit test for {@link Change}.
 */
@ExtendWith(Neo4jExtension.class)
public class ChangeTest {

    @InjectNeo4j(lifecycle = Lifecycle.METHOD)
    private GraphDatabaseService database;

    @BeforeEach
    private void populate() {
        database.executeTransactionally("CREATE " +
                "(a), " +
                "(b {key:'value'})," +
                "(b)-[:test]->(a)," +
                "(c {key:'value'})");
    }

    @Test
    public void shouldConvertChangesToMap() {
        try (Transaction tx = database.beginTx()) {
            Change<Node> nodeChange = new Change<>(tx.getNodeById(0), tx.getNodeById(0));
            Map<Long, Change<Node>> changeMap = changesToMap(asList(nodeChange));
            assertEquals(0, changeMap.get(0L).getCurrent().getId());
            assertEquals(0, changeMap.get(0L).getPrevious().getId());
            assertEquals(1, changeMap.size());
        }
    }

    @Test
    public void equalChangesShouldBeEqual() {
        try (Transaction tx = database.beginTx()) {
            Change<Node> nodeChange1 = new Change<>(tx.getNodeById(0), tx.getNodeById(0));
            Change<Node> nodeChange2 = new Change<>(tx.getNodeById(0), tx.getNodeById(0));
            Change<Node> nodeChange3 = new Change<>(tx.getNodeById(1), tx.getNodeById(1));

            assertTrue(nodeChange1.equals(nodeChange2));
            assertTrue(nodeChange2.equals(nodeChange1));
            assertFalse(nodeChange3.equals(nodeChange1));
            assertFalse(nodeChange1.equals(nodeChange3));
        }
    }

    @Test
    public void invalidChangeShouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> {
            try (Transaction tx = database.beginTx()) {
                Change<Node> nodeChange1 = new Change<>(tx.getNodeById(0), tx.getNodeById(1));
                changesToMap(Collections.singleton(nodeChange1));
            }
        });
    }
}
