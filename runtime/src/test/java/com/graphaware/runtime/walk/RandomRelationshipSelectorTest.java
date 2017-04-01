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

package com.graphaware.runtime.walk;

import com.graphaware.common.policy.inclusion.fluent.IncludeRelationships;
import com.graphaware.common.policy.inclusion.spel.SpelRelationshipInclusionPolicy;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.backup.OnlineBackupSettings;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.shell.ShellSettings;
import org.neo4j.test.TestGraphDatabaseFactory;

import static com.graphaware.common.util.DatabaseUtils.registerShutdownHook;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.neo4j.graphdb.Direction.INCOMING;
import static org.neo4j.graphdb.Direction.OUTGOING;
import static org.neo4j.graphdb.RelationshipType.withName;
import static org.neo4j.graphdb.Label.label;
import static org.neo4j.kernel.configuration.Settings.FALSE;

/**
 * Unit test for {@link RandomRelationshipSelector}.
 */
public class RandomRelationshipSelectorTest {

    private GraphDatabaseService database;

    @Before
    public void setUp() {
        database = new TestGraphDatabaseFactory()
                .newImpermanentDatabaseBuilder()
                .setConfig(OnlineBackupSettings.online_backup_enabled, FALSE)
                .setConfig(ShellSettings.remote_shell_enabled, FALSE)
                .newGraphDatabase();

        registerShutdownHook(database);
    }

    @After
    public void tearDown() {
        database.shutdown();
    }

    @Test
    public void shouldReturnNullOnNodeWithNoRelationships() {
        try (Transaction tx = database.beginTx()) {
            database.createNode();
            tx.success();
        }

        try (Transaction tx = database.beginTx()) {
            assertNull(new RandomRelationshipSelector().selectRelationship(database.getNodeById(0)));
            tx.success();
        }
    }

    @Test
    public void shouldReturnNullOnNodeWithNoMatchingRelationships() {
        try (Transaction tx = database.beginTx()) {
            Node node1 = database.createNode();
            Node node2 = database.createNode();
            node1.createRelationshipTo(node2, withName("TEST"));
            tx.success();
        }

        try (Transaction tx = database.beginTx()) {
            assertNull(new RandomRelationshipSelector(IncludeRelationships.all().with(withName("NOT_EXIST"))).selectRelationship(database.getNodeById(0)));
            assertNull(new RandomRelationshipSelector(IncludeRelationships.all().with(INCOMING)).selectRelationship(database.getNodeById(0)));
            assertNull(new RandomRelationshipSelector(new SpelRelationshipInclusionPolicy("isOutgoing() && otherNode.hasLabel('Test')")).selectRelationship(database.getNodeById(0)));
            tx.success();
        }
    }

    @Test
    public void shouldReturnSingleMatchingRelationship() {
        try (Transaction tx = database.beginTx()) {
            Node node1 = database.createNode();
            Node node2 = database.createNode(label("Test"));
            node1.createRelationshipTo(node2, withName("TEST"));
            tx.success();
        }

        try (Transaction tx = database.beginTx()) {
            assertEquals(0, new RandomRelationshipSelector(IncludeRelationships.all().with(OUTGOING)).selectRelationship(database.getNodeById(0)).getId());
            assertEquals(0, new RandomRelationshipSelector(new SpelRelationshipInclusionPolicy("isOutgoing() && otherNode.hasLabel('Test')")).selectRelationship(database.getNodeById(0)).getId());
            tx.success();
        }
    }
}
