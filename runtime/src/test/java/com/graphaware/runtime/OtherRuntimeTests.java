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

package com.graphaware.runtime;

import com.graphaware.runtime.bootstrap.RuntimeKernelExtension;
import org.junit.jupiter.api.RepeatedTest;
import org.neo4j.configuration.SettingImpl;
import org.neo4j.configuration.SettingValueParsers;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.harness.Neo4j;
import org.neo4j.harness.Neo4jBuilders;

import java.time.Duration;
import java.util.Random;

import static com.graphaware.runtime.bootstrap.RuntimeKernelExtension.*;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

/**
 * Aux runtime tests for bugs found while doing manual testing.
 */
public class OtherRuntimeTests {

    private final Random random = new Random();

    @RepeatedTest(10)
    public void makeSureDeadlockDoesNotOccur() {
        Neo4j controls = Neo4jBuilders.newInProcessBuilder().withConfig(RUNTIME_ENABLED, true).build();
        GraphDatabaseService database = controls.defaultDatabaseService();

        assertTimeoutPreemptively(Duration.ofSeconds(5), () -> {
                    Thread.sleep(random.nextInt(10));

                    try (Transaction tx = database.beginTx()) {
                        Node node = tx.createNode(Label.label("TEST"));
                        node.setProperty("test", "test");
                        tx.commit();
                    }

                    Thread.sleep(random.nextInt(200));
                }
        );

        controls.close();
    }

    @RepeatedTest(10)
    public void makeSureDeadlockDoesNotOccur1() {
        Neo4j controls = Neo4jBuilders.newInProcessBuilder().withConfig(RUNTIME_ENABLED, true).build();
        GraphDatabaseService database = controls.defaultDatabaseService();

        assertTimeoutPreemptively(Duration.ofSeconds(5), () -> {
            Thread.sleep(random.nextInt(10));

            try (Transaction tx = database.beginTx()) {
                Node node1 = tx.createNode();
                node1.setProperty("name", "MB");
                node1.addLabel(Label.label("Person"));

                tx.commit();
            }

            Thread.sleep(random.nextInt(200));
        });

        controls.close();
    }

    @RepeatedTest(10)
    public void makeSureDeadlockDoesNotOccur2() {
        Neo4j controls = Neo4jBuilders.newInProcessBuilder().withConfig(RUNTIME_ENABLED, true).build();
        GraphDatabaseService database = controls.defaultDatabaseService();

        assertTimeoutPreemptively(Duration.ofSeconds(5), () -> {
            try (Transaction tx = database.beginTx()) {
                Node node = tx.createNode();
                node.setProperty("test", "test");
                tx.commit();
            }
        });

        controls.close();
    }

    @RepeatedTest(10)
    public void makeSureDeadlockDoesNotOccur3() {
        Neo4j controls = Neo4jBuilders.newInProcessBuilder().withConfig(RUNTIME_ENABLED, true).build();
        GraphDatabaseService database = controls.defaultDatabaseService();

        assertTimeoutPreemptively(Duration.ofSeconds(5), () -> {
            try (Transaction tx = database.beginTx()) {
                tx.createNode();
                tx.commit();
            }
        });

        controls.close();
    }

    @RepeatedTest(10)
    public void makeSureDeadlockDoesNotOccur4() {
        Neo4j controls = Neo4jBuilders.newInProcessBuilder().withConfig(RUNTIME_ENABLED, true).build();
        GraphDatabaseService database = controls.defaultDatabaseService();

        assertTimeoutPreemptively(Duration.ofSeconds(5), () -> {
            try (Transaction tx = database.beginTx()) {
                tx.createNode(Label.label("TEST"));
                tx.commit();
            }
        });

        controls.close();
    }

    @RepeatedTest(10)
    public void makeSureDeadlockDoesNotOccur5() {
        Neo4j controls = Neo4jBuilders.newInProcessBuilder().withConfig(RUNTIME_ENABLED, true).build();
        GraphDatabaseService database = controls.defaultDatabaseService();

        assertTimeoutPreemptively(Duration.ofSeconds(5), () -> {
            try (Transaction tx = database.beginTx()) {
                Node node = tx.createNode(Label.label("TEST"));
                node.setProperty("test", "test");
                tx.commit();
            }
        });

        controls.close();
    }

    @RepeatedTest(10)
    public void makeSureDeadlockDoesNotOccur6() throws InterruptedException {
        Neo4j controls = Neo4jBuilders.newInProcessBuilder().withConfig(RUNTIME_ENABLED, true).build();
        GraphDatabaseService database = controls.defaultDatabaseService();

        assertTimeoutPreemptively(Duration.ofSeconds(5), () -> {
            try (Transaction tx = database.beginTx()) {
                Node node = tx.createNode(Label.label("TEST"));
                node.setProperty("test", "test");
                tx.commit();
            }
        });

        Thread.sleep(random.nextInt(200));

        controls.close();
    }
}
