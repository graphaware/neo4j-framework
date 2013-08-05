/*
 * Copyright (c) 2013 GraphAware
 *
 * This file is part of GraphAware.
 *
 * GraphAware is free software: you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received a copy of
 * the GNU General Public License along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 */

package com.graphaware.tx.executor.batch;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.test.ImpermanentGraphDatabase;

import java.util.Arrays;
import java.util.List;

import static com.graphaware.test.IterableUtils.countNodes;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Unit test for {@link IterableInputBatchTransactionExecutor}.
 */
public class IterableInputBatchTransactionExecutorTest {

    private GraphDatabaseService database;

    @Before
    public void setUp() {
        database = new ImpermanentGraphDatabase();
    }

    @After
    public void tearDown() {
        database.shutdown();
    }

    @Test
    public void nodesShouldBeCreatedFromListOfNames() {
        List<String> nodeNames = Arrays.asList("Name1", "Name2", "Name3");

        BatchTransactionExecutor executor = new IterableInputBatchTransactionExecutor<String>(database, 2, nodeNames, new UnitOfWork<String>() {
            @Override
            public void execute(GraphDatabaseService database, String nodeName) {
                Node node = database.createNode();
                node.setProperty("name", nodeName);
            }
        });

        executor.execute();

        assertEquals(4, countNodes(database));  //3 + root
        assertTrue(nodeNames.contains(database.getNodeById(1).getProperty("name")));
        assertTrue(nodeNames.contains(database.getNodeById(2).getProperty("name")));
        assertTrue(nodeNames.contains(database.getNodeById(3).getProperty("name")));
    }
}
