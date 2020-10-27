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

package com.graphaware.runtime.module.thirdparty;

import com.graphaware.common.junit.InjectNeo4j;
import com.graphaware.common.junit.Neo4jExtension;
import com.graphaware.common.representation.GraphDetachedNode;
import com.graphaware.common.representation.GraphDetachedRelationship;
import com.graphaware.runtime.CommunityRuntime;
import com.graphaware.runtime.GraphAwareRuntime;
import com.graphaware.runtime.manager.CommunityModuleManager;
import com.graphaware.writer.thirdparty.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.neo4j.graphdb.*;
import org.neo4j.harness.Neo4j;
import org.neo4j.internal.helpers.collection.MapUtil;

import java.util.Collection;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(Neo4jExtension.class)
public class ThirdPartyIntegrationModuleTest {

    @InjectNeo4j
    private Neo4j neo4j;

    @InjectNeo4j
    private GraphDatabaseService database;

    @Test
    public void modificationsShouldBeCorrectlyBuilt() {
        TestThirdPartyModule module = new TestThirdPartyModule("test");

        database.executeTransactionally("CREATE (p:Person {name:'Michal', age:30})-[:WORKS_FOR {since:2013, role:'MD'}]->(c:Company {name:'GraphAware', est: 2013})");
        database.executeTransactionally("MATCH (ga:Company {name:'GraphAware'}) CREATE (p:Person {name:'Adam'})-[:WORKS_FOR {since:2014}]->(ga)");

        long danielaId, michalId, adamId, gaId, dWorksForId, mWorksForId, aWorksForId;
        try (Transaction tx = database.beginTx()) {
            michalId = tx.findNode(Label.label("Person"), "name", "Michal").getId();
            Node adam = tx.findNode(Label.label("Person"), "name", "Adam");
            adamId = adam.getId();
            gaId = tx.findNode(Label.label("Company"), "name", "GraphAware").getId();
            aWorksForId = adam.getSingleRelationship(RelationshipType.withName("WORKS_FOR"), Direction.OUTGOING).getId();

            tx.commit();
        }

        GraphAwareRuntime runtime = new CommunityRuntime(database, neo4j.databaseManagementService());
        runtime.registerModule(module);
        runtime.start();

        try (Transaction tx = database.beginTx()) {
            tx.execute("MATCH (ga:Company {name:'GraphAware'}) CREATE (p:Person {name:'Daniela'})-[:WORKS_FOR]->(ga)");
            tx.execute("MATCH (p:Person {name:'Michal'}) SET p.age=31");
            tx.execute("MATCH (p:Person {name:'Adam'})-[r]-() DELETE p,r");
            tx.execute("MATCH (p:Person {name:'Michal'})-[r:WORKS_FOR]->() REMOVE r.role");
            tx.commit();
        }

        try (Transaction tx = database.beginTx()) {
            Node daniela = tx.findNode(Label.label("Person"), "name", "Daniela");
            dWorksForId = daniela.getSingleRelationship(RelationshipType.withName("WORKS_FOR"), Direction.OUTGOING).getId();
            danielaId = daniela.getId();

            Node michal = tx.findNode(Label.label("Person"), "name", "Michal");
            mWorksForId = michal.getSingleRelationship(RelationshipType.withName("WORKS_FOR"), Direction.OUTGOING).getId();

            tx.commit();
        }

        Collection<WriteOperation<?>> writeOperations = module.getWriteOperations();
        assertEquals(6, writeOperations.size());

        assertTrue(writeOperations.contains(new NodeCreated<>(
                new GraphDetachedNode(danielaId, new String[]{"Person"}, MapUtil.map("name", "Daniela")))));

        assertTrue(writeOperations.contains(new NodeUpdated<>(
                new GraphDetachedNode(michalId, new String[]{"Person"}, MapUtil.map("name", "Michal", "age", 30L)),
                new GraphDetachedNode(michalId, new String[]{"Person"}, MapUtil.map("name", "Michal", "age", 31L)))));

        assertTrue(writeOperations.contains(new NodeDeleted<>(
                new GraphDetachedNode(adamId, new String[]{"Person"}, MapUtil.map("name", "Adam")))));

        assertTrue(writeOperations.contains(new RelationshipCreated<>(
                new GraphDetachedRelationship(dWorksForId, danielaId, gaId, "WORKS_FOR", Collections.<String, Object>emptyMap())
        )));

        assertTrue(writeOperations.contains(new RelationshipUpdated<>(
                new GraphDetachedRelationship(mWorksForId, michalId, gaId, "WORKS_FOR", MapUtil.map("since", 2013L, "role", "MD")),
                new GraphDetachedRelationship(mWorksForId, michalId, gaId, "WORKS_FOR", MapUtil.map("since", 2013L)))));

        assertTrue(writeOperations.contains(new RelationshipDeleted<>(
                new GraphDetachedRelationship(aWorksForId, adamId, gaId, "WORKS_FOR", MapUtil.map("since", 2014L))
        )));

        for (WriteOperation<?> operation : writeOperations) {
            if (operation.getDetails() instanceof GraphDetachedRelationship) {
                assertTrue(((GraphDetachedRelationship) operation.getDetails()).getStartNode() instanceof GraphDetachedNode);
                assertTrue(((GraphDetachedRelationship) operation.getDetails()).getEndNode() instanceof GraphDetachedNode);
            }
        }

        runtime.stop();
    }
}
