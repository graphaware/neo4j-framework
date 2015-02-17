/*
 * Copyright (c) 2015 GraphAware
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

package com.graphaware.perf.writes;

import org.junit.Ignore;
import org.junit.Test;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Very very simple write perf test
 */
@Ignore
public class WritePerfTest {

    @Test
    public void testPerf() {
        final GraphDatabaseService database = new GraphDatabaseFactory().newEmbeddedDatabase("/tmp/db" + System.currentTimeMillis());

        for (int i = 0; i < 10; i++) {
            ExecutorService executorService = Executors.newFixedThreadPool(10);
            long start = System.currentTimeMillis();
            for (int j = 0; j < 100; j++) {
                executorService.submit(new Runnable() {
                    @Override
                    public void run() {
                        try (Transaction tx = database.beginTx()) {
                            Node node1 = database.createNode(DynamicLabel.label("Person"));
                            node1.setProperty("name", "Person1");
                            Node node2 = database.createNode(DynamicLabel.label("Person"));
                            node2.setProperty("name", "Person2");
                            node1.createRelationshipTo(node2, DynamicRelationshipType.withName("FRIEND"));
                            tx.success();
                        }
                    }
                });
            }
            executorService.shutdown();
            try {
                executorService.awaitTermination(1, TimeUnit.MINUTES);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.print((System.currentTimeMillis() - start) + ";");
        }

        database.shutdown();
    }
}
