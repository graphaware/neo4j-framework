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

package com.graphaware.lifecycle.config;

import com.graphaware.lifecycle.LifecyleModule;
import com.graphaware.runtime.GraphAwareRuntime;
import com.graphaware.runtime.GraphAwareRuntimeFactory;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseBuilder;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

import java.io.File;
import java.util.function.Consumer;

public class GraphAwareExpiryTest {

    public static void main(String[] args) throws Exception {
        GraphDatabaseBuilder graphDatabaseBuilder = new GraphDatabaseFactory()
                .newEmbeddedDatabaseBuilder(new File(System.getProperty("java.io.tmpdir") + File.separator + "neo4j9"));
        GraphDatabaseService graphDatabaseService = graphDatabaseBuilder.newGraphDatabase();
        GraphAwareRuntime graphAwareRuntime = GraphAwareRuntimeFactory.createRuntime(graphDatabaseService);

        LifecycleConfiguration configuration = LifecycleConfiguration.defaultConfiguration().withNodeExpirationProperty("expire");

        graphAwareRuntime.registerModule(new LifecyleModule("EXP", graphDatabaseService, configuration,
                configuration.scheduledEvents(), configuration.commitEvents(), configuration.getMaxNoExpirations()));

        graphAwareRuntime.start();
        graphAwareRuntime.waitUntilStarted();

        long now = System.currentTimeMillis();
        long twentySecondsFromNow = now + 20 * 1000;
        long fiftySecondsFromNow = now + 50 * 1000;

        try (Transaction tx = graphDatabaseService.beginTx()) {
            Node s1 = graphDatabaseService.createNode(Label.label("State1"));
            s1.setProperty("name", "Cloudy");
            s1.setProperty("expire", twentySecondsFromNow);

            Node s2 = graphDatabaseService.createNode(Label.label("State1"));
            s2.setProperty("name", "Windy");
            s2.setProperty("expire", fiftySecondsFromNow);

            tx.success();
        }

        System.out.println("1st print");

        try (Transaction tx = graphDatabaseService.beginTx()) {
            graphDatabaseService.findNodes(Label.label("State1")).forEachRemaining(new Consumer<Node>() {
                @Override
                public void accept(Node node) {
                    System.out.println(node.getProperty("expire"));
                }
            });

            tx.success();
        }

        Thread.sleep(25000 - (System.currentTimeMillis() - now));

        System.out.println("2nd print");

        try (Transaction tx = graphDatabaseService.beginTx()) {
            graphDatabaseService.findNodes(Label.label("State1")).forEachRemaining(new Consumer<Node>() {
                @Override
                public void accept(Node node) {
                    System.out.println(node.getProperty("expire"));
                }
            });

            tx.success();
        }

        Thread.sleep(55000 - (System.currentTimeMillis() - now));

        System.out.println("3rd print");

        try (Transaction tx = graphDatabaseService.beginTx()) {
            graphDatabaseService.findNodes(Label.label("State1")).forEachRemaining(new Consumer<Node>() {
                @Override
                public void accept(Node node) {
                    System.out.println(node.getProperty("expire"));
                    System.out.println(System.currentTimeMillis() + " is now");
                }
            });

            tx.success();
        }

        graphDatabaseService.shutdown();
    }
}
