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

package com.graphaware.runtime.module.thirdparty;

import com.graphaware.common.representation.GraphDetachedNode;
import com.graphaware.common.representation.GraphDetachedRelationship;
import com.graphaware.runtime.GraphAwareRuntime;
import com.graphaware.runtime.GraphAwareRuntimeFactory;
import com.graphaware.runtime.module.TxDrivenModule;
import com.graphaware.writer.thirdparty.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.backup.OnlineBackupSettings;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Transaction;
import org.neo4j.helpers.collection.MapUtil;
import org.neo4j.shell.ShellSettings;
import org.neo4j.test.TestGraphDatabaseFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static com.graphaware.common.util.DatabaseUtils.registerShutdownHook;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.neo4j.kernel.configuration.Settings.FALSE;

/**
 * Integration test for {@link WriterBasedThirdPartyIntegrationModule}
 */
public class DefaultThirdPartyIntegrationModuleTest {

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
    public void modificationsShouldBeCorrectlyBuilt() throws InterruptedException {
        RememberingWriter writer = new RememberingWriter();
        TxDrivenModule module = new DefaultThirdPartyIntegrationModule("test", writer);

        database.execute("CREATE (p:Person {name:'Michal', age:30})-[:WORKS_FOR {since:2013, role:'MD'}]->(c:Company {name:'GraphAware', est: 2013})");
        database.execute("MATCH (ga:Company {name:'GraphAware'}) CREATE (p:Person {name:'Adam'})-[:WORKS_FOR {since:2014}]->(ga)");

        long danielaId, michalId, adamId, gaId;
        try (Transaction tx = database.beginTx()) {
            michalId = database.findNode(Label.label("Person"), "name", "Michal").getId();
            adamId = database.findNode(Label.label("Person"), "name", "Adam").getId();
            gaId = database.findNode(Label.label("Company"), "name", "GraphAware").getId();

            tx.success();
        }

        GraphAwareRuntime runtime = GraphAwareRuntimeFactory.createRuntime(database);
        runtime.registerModule(module);
        runtime.start();
        runtime.waitUntilStarted();

        try (Transaction tx = database.beginTx()) {
            database.execute("MATCH (ga:Company {name:'GraphAware'}) CREATE (p:Person {name:'Daniela'})-[:WORKS_FOR]->(ga)");
            database.execute("MATCH (p:Person {name:'Michal'}) SET p.age=31");
            tx.success();
        }

        try (Transaction tx = database.beginTx()) {
            danielaId = database.findNode(Label.label("Person"), "name", "Daniela").getId();
            tx.success();
        }

        try (Transaction tx = database.beginTx()) {
            database.execute("MATCH (p:Person {name:'Adam'})-[r]-() DELETE p,r");
            database.execute("MATCH (p:Person {name:'Michal'})-[r:WORKS_FOR]->() REMOVE r.role");
            tx.success();
        }

        Thread.sleep(1000);

        List<Collection<WriteOperation<?>>> writeOperations = writer.getRemembered();
        assertEquals(2, writeOperations.size());
        assertEquals(3, writeOperations.get(0).size());
        assertEquals(3, writeOperations.get(1).size());

        assertTrue(writeOperations.get(0).contains(new NodeCreated<>(
                new GraphDetachedNode(danielaId, new String[]{"Person"}, MapUtil.map("name", "Daniela")))));

        assertTrue(writeOperations.get(0).contains(new NodeUpdated<>(
                new GraphDetachedNode(michalId, new String[]{"Person"}, MapUtil.map("name", "Michal", "age", 30L)),
                new GraphDetachedNode(michalId, new String[]{"Person"}, MapUtil.map("name", "Michal", "age", 31L)))));

        assertTrue(writeOperations.get(0).contains(new RelationshipCreated<>(
                new GraphDetachedRelationship(1L, danielaId, gaId, "WORKS_FOR", Collections.<String, Object>emptyMap())
        )));

        assertTrue(writeOperations.get(1).contains(new NodeDeleted<>(
                new GraphDetachedNode(adamId, new String[]{"Person"}, MapUtil.map("name", "Adam")))));


        assertTrue(writeOperations.get(1).contains(new RelationshipUpdated<>(
                new GraphDetachedRelationship(0L, michalId, gaId, "WORKS_FOR", MapUtil.map("since", 2013L, "role", "MD")),
                new GraphDetachedRelationship(0L, michalId, gaId, "WORKS_FOR", MapUtil.map("since", 2013L)))));

        assertTrue(writeOperations.get(1).contains(new RelationshipDeleted<>(
                new GraphDetachedRelationship(20L, adamId, gaId, "WORKS_FOR", MapUtil.map("since", 2014L))
        )));
    }
}
