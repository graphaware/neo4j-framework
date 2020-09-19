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

package com.graphaware.tx.event.improved;

import com.graphaware.common.junit.InjectNeo4j;
import com.graphaware.common.junit.Neo4jExtension;
import com.graphaware.test.data.CypherPopulator;
import com.graphaware.tx.event.improved.api.ImprovedTransactionData;
import com.graphaware.tx.event.improved.api.LazyTransactionData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.event.TransactionData;
import org.neo4j.graphdb.event.TransactionEventListenerAdapter;
import org.neo4j.harness.Neo4j;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(Neo4jExtension.class)
public class AdditionalModificationsIntegrationTest {

    @InjectNeo4j
    private Neo4j neo4j;

    @BeforeEach
    protected void poulate() {
         new CypherPopulator() {
            @Override
            protected String[] statementGroups() {
                return new String[]{"CREATE " +
                        "(m:Person {name:'Michal'})," +
                        "(l:City {name:'London'})," +
                        "(p:City {name:'Prague'})," +
                        "(m)-[:LIVES_IN]->(p)," +
                        "(m)-[:LIVES_IN]->(l)" +
                        ""};
            }
        }.populate(neo4j.defaultDatabaseService());
    }

    @Test
    public void additionalCreatesShouldNotImpactTxData() {
        TransactionEventListenerAdapter<Void> listener = new TransactionEventListenerAdapter<>() {

            @Override
            public Void beforeCommit(TransactionData data, Transaction transaction, GraphDatabaseService databaseService) throws Exception {
                ImprovedTransactionData transactionData = new LazyTransactionData(data, transaction);

                assertEquals(1, transactionData.getAllCreatedNodes().size());

                for (Node node : transactionData.getAllCreatedNodes()) {
                    Node unknownCity = transaction.createNode(Label.label("City"));
                    node.createRelationshipTo(unknownCity, RelationshipType.withName("LIVES_IN"));
                }

                assertEquals(1, transactionData.getAllCreatedNodes().size());

                return null;
            }
        };

        neo4j.databaseManagementService().registerTransactionEventListener(neo4j.defaultDatabaseService().databaseName(), listener);

        try (Transaction tx = neo4j.defaultDatabaseService().beginTx()) {
            tx.createNode();
            tx.commit();
        }

        neo4j.databaseManagementService().unregisterTransactionEventListener(neo4j.defaultDatabaseService().databaseName(), listener);
    }


}
