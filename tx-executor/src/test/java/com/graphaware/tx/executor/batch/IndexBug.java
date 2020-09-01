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

package com.graphaware.tx.executor.batch;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.neo4j.graphdb.*;
import org.neo4j.harness.Neo4j;
import org.neo4j.harness.Neo4jBuilders;

import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Disabled
public class IndexBug {

    private static final int NUMBER_OF_NODES = 10_000;
    private static final int BATCH_SIZE = 1_000;

    private Neo4j controls;
    protected GraphDatabaseService database;

    @BeforeEach
    public void setUp() {
        controls = Neo4jBuilders.newInProcessBuilder().build();
        database = controls.defaultDatabaseService();

        try (Transaction tx = database.beginTx()) {
            for (int i = 0; i < NUMBER_OF_NODES; i++) {
                Node node = tx.createNode();
                node.addLabel(Label.label("FirstLabel"));
            }
            tx.commit();
        }
    }

    @AfterEach
    public void tearDown() {
        controls.close();
    }

    @Test
    public void testLabelsCanBeAddedInBatch() {
        Iterator<Node> allNodes;

        try (Transaction tx = database.beginTx()) {
            allNodes = tx.findNodes(Label.label("FirstLabel"));
            tx.commit();
        }

        int counter = 0;
        while (processBatch(allNodes)) {
            System.out.println("Processed Batch "+ ++counter);
        }

        int i = 0;
        try (Transaction tx = database.beginTx()) {
            ResourceIterator<Node> nodes = tx.findNodes(Label.label("SecondLabel"));
            while (nodes.hasNext()) {
                i++;
                nodes.next();
            }

            tx.commit();
        }

        assertEquals(NUMBER_OF_NODES, i);
    }

    private boolean processBatch(Iterator<Node> allNodes) {
        boolean result = true;

        try (Transaction tx = database.beginTx()) {
            for (int i = 0; i < BATCH_SIZE; i++) {
                if (!allNodes.hasNext()) {
                    result = false;
                    break;
                }

                Node next = allNodes.next();
                System.out.println(next.getProperty("test", "nothing"));
                //next.addLabel(Label.label("SecondLabel"));
            }
            tx.commit();
        }

        return result;
    }
}
