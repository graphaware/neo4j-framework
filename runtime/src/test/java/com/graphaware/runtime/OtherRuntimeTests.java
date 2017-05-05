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

package com.graphaware.runtime;

import com.graphaware.runtime.bootstrap.RuntimeKernelExtension;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.neo4j.backup.OnlineBackupSettings;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.shell.ShellSettings;
import org.neo4j.test.TestGraphDatabaseFactory;
import org.neo4j.test.rule.RepeatRule;

import java.util.Random;

import static com.graphaware.common.util.DatabaseUtils.registerShutdownHook;
import static org.neo4j.kernel.configuration.Settings.*;

/**
 * Aux runtime tests for bugs found while doing manual testing.
 */
public class OtherRuntimeTests {

    private Random random = new Random();

    @Rule
    public RepeatRule repeatRule = new RepeatRule();

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Before
    public void createTempFolder() {
        temporaryFolder.getRoot().deleteOnExit();
    }

    @After
    public void deleteTempFolder() {
        temporaryFolder.delete();
    }

    @Test(timeout = 5000)
    @RepeatRule.Repeat(times = 10)
    public void makeSureDeadlockDoesNotOccur() throws InterruptedException {
        GraphDatabaseService database = new TestGraphDatabaseFactory()
                .newImpermanentDatabaseBuilder()
                .setConfig(OnlineBackupSettings.online_backup_enabled, FALSE)
                .setConfig(ShellSettings.remote_shell_enabled, FALSE)
                .setConfig(RuntimeKernelExtension.RUNTIME_ENABLED, "true")
                .newGraphDatabase();

        registerShutdownHook(database);

        Thread.sleep(random.nextInt(10));

        try (Transaction tx = database.beginTx()) {
            Node node = database.createNode(Label.label("TEST"));
            node.setProperty("test", "test");
            tx.success();
        }

        Thread.sleep(random.nextInt(200));

        database.shutdown();
    }

    @Test(timeout = 5000)
    @RepeatRule.Repeat(times = 10)
    public void makeSureDeadlockDoesNotOccur1() throws InterruptedException {
        GraphDatabaseService database = new TestGraphDatabaseFactory()
                .newImpermanentDatabaseBuilder()
                .setConfig(OnlineBackupSettings.online_backup_enabled, FALSE)
                .setConfig(ShellSettings.remote_shell_enabled, FALSE)
                .setConfig(RuntimeKernelExtension.RUNTIME_ENABLED, "true")
                .newGraphDatabase();

        registerShutdownHook(database);

        Thread.sleep(random.nextInt(10));


        try (Transaction tx = database.beginTx()) {
            Node node1 = database.createNode();
            node1.setProperty("name", "MB");
            node1.addLabel(Label.label("Person"));

            tx.success();
        }

        Thread.sleep(random.nextInt(200));

        database.shutdown();
    }

    @Test(timeout = 5000)
    @RepeatRule.Repeat(times = 10)
    public void makeSureDeadlockDoesNotOccur2() {
        GraphDatabaseService database = new TestGraphDatabaseFactory()
                .newImpermanentDatabaseBuilder()
                .setConfig(OnlineBackupSettings.online_backup_enabled, FALSE)
                .setConfig(ShellSettings.remote_shell_enabled, FALSE)
                .setConfig(RuntimeKernelExtension.RUNTIME_ENABLED, "true")
                .newGraphDatabase();

        registerShutdownHook(database);

        try (Transaction tx = database.beginTx()) {
            Node node = database.createNode();
            node.setProperty("test", "test");
            tx.success();
        }

        database.shutdown();
    }

    @Test(timeout = 5000)
    @RepeatRule.Repeat(times = 10)
    public void makeSureDeadlockDoesNotOccur3() {
        GraphDatabaseService database = new TestGraphDatabaseFactory()
                .newImpermanentDatabaseBuilder()
                .setConfig(OnlineBackupSettings.online_backup_enabled, FALSE)
                .setConfig(ShellSettings.remote_shell_enabled, FALSE)
                .setConfig(RuntimeKernelExtension.RUNTIME_ENABLED, "true")
                .newGraphDatabase();

        registerShutdownHook(database);

        try (Transaction tx = database.beginTx()) {
            database.createNode();
            tx.success();
        }

        database.shutdown();
    }

    @Test(timeout = 5000)
    @RepeatRule.Repeat(times = 10)
    public void makeSureDeadlockDoesNotOccur4() {
        GraphDatabaseService database = new TestGraphDatabaseFactory()
                .newImpermanentDatabaseBuilder()
                .setConfig(OnlineBackupSettings.online_backup_enabled, FALSE)
                .setConfig(ShellSettings.remote_shell_enabled, FALSE)
                .setConfig(RuntimeKernelExtension.RUNTIME_ENABLED, "true")
                .newGraphDatabase();

        registerShutdownHook(database);

        try (Transaction tx = database.beginTx()) {
            database.createNode(Label.label("TEST"));
            tx.success();
        }

        database.shutdown();
    }

    @Test(timeout = 5000)
    @RepeatRule.Repeat(times = 10)
    public void makeSureDeadlockDoesNotOccur5() {
        GraphDatabaseService database = new GraphDatabaseFactory()
                .newEmbeddedDatabaseBuilder(temporaryFolder.getRoot())
                .setConfig(OnlineBackupSettings.online_backup_enabled, FALSE)
                .setConfig(ShellSettings.remote_shell_enabled, FALSE)
                .setConfig(RuntimeKernelExtension.RUNTIME_ENABLED, "true")
                .newGraphDatabase();

        registerShutdownHook(database);

        try (Transaction tx = database.beginTx()) {
            Node node = database.createNode(Label.label("TEST"));
            node.setProperty("test", "test");
            tx.success();
        }

        database.shutdown();
    }

    @Test(timeout = 5000)
    @RepeatRule.Repeat(times = 10)
    public void makeSureDeadlockDoesNotOccur6() throws InterruptedException {
        GraphDatabaseService database = new GraphDatabaseFactory()
                .newEmbeddedDatabaseBuilder(temporaryFolder.getRoot())
                .setConfig(OnlineBackupSettings.online_backup_enabled, FALSE)
                .setConfig(ShellSettings.remote_shell_enabled, FALSE)
                .setConfig(RuntimeKernelExtension.RUNTIME_ENABLED, "true")
                .newGraphDatabase();

        registerShutdownHook(database);

        try (Transaction tx = database.beginTx()) {
            Node node = database.createNode(Label.label("TEST"));
            node.setProperty("test", "test");
            tx.success();
        }

        Thread.sleep(random.nextInt(200));

        database.shutdown();
    }
}
