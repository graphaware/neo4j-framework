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

package com.graphaware.example;

import com.graphaware.tx.executor.single.SimpleTransactionExecutor;
import com.graphaware.tx.executor.single.VoidReturningCallback;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.backup.OnlineBackupSettings;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.shell.ShellSettings;
import org.neo4j.test.TestGraphDatabaseFactory;

import static com.graphaware.common.util.DatabaseUtils.registerShutdownHook;
import static org.neo4j.graphdb.Direction.OUTGOING;
import static org.neo4j.graphdb.RelationshipType.withName;
import static org.neo4j.graphdb.Label.label;
import static org.neo4j.kernel.configuration.Settings.FALSE;


/**
 * Demo of {@link ChangeLogger}.
 */
public class ChangeLoggerDemo {

    private GraphDatabaseService database;

    @Before
    public void setUp() {
        database = new TestGraphDatabaseFactory()
                .newImpermanentDatabaseBuilder()
                .setConfig(OnlineBackupSettings.online_backup_enabled, FALSE)
                .setConfig(ShellSettings.remote_shell_enabled, FALSE)
                .newGraphDatabase();

        registerShutdownHook(database);
        database.registerTransactionEventHandler(new ChangeLogger());
    }

    @After
    public void tearDown() {
        database.shutdown();
    }

    @Test
    public void demonstrateLogging() {
        performMutations(database);
    }

    private void performMutations(GraphDatabaseService database) {
        SimpleTransactionExecutor executor = new SimpleTransactionExecutor(database);

        //create nodes
        executor.executeInTransaction(new VoidReturningCallback() {
            @Override
            protected void doInTx(GraphDatabaseService database) {
                database.createNode(label("SomeLabel1"));

                Node node1 = database.createNode(label("SomeLabel2"));
                node1.setProperty("name", "One");

                Node node2 = database.createNode();
                node2.setProperty("name", "Two");

                Node node3 = database.createNode();
                node3.setProperty("name", "Three");
            }
        });

        //create relationship
        executor.executeInTransaction(new VoidReturningCallback() {
            @Override
            protected void doInTx(GraphDatabaseService database) {
                Node one = database.getNodeById(1);
                Node two = database.getNodeById(2);

                Relationship relationship = one.createRelationshipTo(two, withName("TEST"));
                relationship.setProperty("level", 2);

                two.createRelationshipTo(one, withName("TEST"));
            }
        });

        //change and delete nodes
        executor.executeInTransaction(new VoidReturningCallback() {
            @Override
            protected void doInTx(GraphDatabaseService database) {
                database.getNodeById(3).delete();
                database.getNodeById(1).setProperty("name", "New One");
                database.getNodeById(1).setProperty("numbers", new int[]{1, 2, 3});
                database.getNodeById(2).addLabel(label("NewLabel"));
            }
        });

        //change and delete relationships
        executor.executeInTransaction(new VoidReturningCallback() {
            @Override
            protected void doInTx(GraphDatabaseService database) {
                database.getNodeById(2).getSingleRelationship(withName("TEST"), OUTGOING).delete();

                database.getNodeById(1).getSingleRelationship(withName("TEST"), OUTGOING).setProperty("level", 3);
            }
        });
    }
}
