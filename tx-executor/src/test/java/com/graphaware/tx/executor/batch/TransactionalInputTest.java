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

package com.graphaware.tx.executor.batch;

import com.graphaware.test.data.CypherPopulator;
import com.graphaware.test.data.DatabasePopulator;
import com.graphaware.test.integration.DatabaseIntegrationTest;
import com.graphaware.test.integration.EmbeddedDatabaseIntegrationTest;
import com.graphaware.tx.executor.input.TransactionalInput;
import com.graphaware.tx.executor.single.TransactionCallback;
import org.junit.Test;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.helpers.collection.Iterables;
import org.neo4j.helpers.collection.Iterators;
import org.neo4j.kernel.impl.transaction.TransactionCounters;
import org.neo4j.kernel.internal.GraphDatabaseAPI;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;

/**
 * Unit test for {@link TransactionalInput}.
 */
public class TransactionalInputTest extends EmbeddedDatabaseIntegrationTest {

    @Override
    protected DatabasePopulator databasePopulator() {
        return new CypherPopulator() {
            @Override
            protected String[] statementGroups() {
                return new String[]{
                        "CREATE (p:Person {name:'Michal'})",
                        "CREATE (p:Person {name:'Vince'})",
                        "CREATE (p:Person {name:'Luanne'})",
                        "CREATE (p:Person {name:'Christophe'})"
                };
            }
        };
    }

    @Test
    public void shouldReturnItemsInMultipleTransactions() {
        TransactionCounters monitor = ((GraphDatabaseAPI) getDatabase()).getDependencyResolver().resolveDependency(TransactionCounters.class);
        long noTx = monitor.getNumberOfCommittedTransactions();

        TransactionalInput<Node> input = new TransactionalInput<>(getDatabase(), 2, database -> Iterators.asResourceIterable(database.findNodes(Label.label("Person"))));

        Set<Node> nodes = new HashSet<>();

        for (Node node : input) {
            nodes.add(node);
        }

        assertEquals(4, nodes.size());
        assertEquals(noTx + 3, monitor.getNumberOfCommittedTransactions());

    }

    @Test
    public void shouldReturnItemsInMultipleTransactions2() {
        TransactionCounters monitor = ((GraphDatabaseAPI) getDatabase()).getDependencyResolver().resolveDependency(TransactionCounters.class);
        long noTx = monitor.getNumberOfCommittedTransactions();

        TransactionalInput<Node> input = new TransactionalInput<>(getDatabase(), 1, new TransactionCallback<Iterable<Node>>() {
            @Override
            public Iterable<Node> doInTransaction(GraphDatabaseService database) throws Exception {
                return Iterators.asResourceIterable(database.findNodes(Label.label("Person")));
            }
        });

        Set<Node> nodes = new HashSet<>();

        for (Node node : input) {
            nodes.add(node);
        }

        assertEquals(4, nodes.size());
        assertEquals(noTx + 5, monitor.getNumberOfCommittedTransactions());
    }

    @Test
    public void shouldReturnItemsInMultipleTransactions3() {
        TransactionCounters monitor = ((GraphDatabaseAPI) getDatabase()).getDependencyResolver().resolveDependency(TransactionCounters.class);
        long noTx = monitor.getNumberOfCommittedTransactions();

        TransactionalInput<Node> input = new TransactionalInput<>(getDatabase(), 3, new TransactionCallback<Iterable<Node>>() {
            @Override
            public Iterable<Node> doInTransaction(GraphDatabaseService database) throws Exception {
                return Iterators.asResourceIterable(database.findNodes(Label.label("Person")));
            }
        });

        Set<Node> nodes = new HashSet<>();

        for (Node node : input) {
            nodes.add(node);
        }

        assertEquals(4, nodes.size());
        assertEquals(noTx + 2, monitor.getNumberOfCommittedTransactions());
    }

    @Test
    public void shouldReturnItemsInMultipleTransactions4() {
        TransactionCounters monitor = ((GraphDatabaseAPI) getDatabase()).getDependencyResolver().resolveDependency(TransactionCounters.class);
        long noTx = monitor.getNumberOfCommittedTransactions();

        TransactionalInput<Node> input = new TransactionalInput<>(getDatabase(), 100, new TransactionCallback<Iterable<Node>>() {
            @Override
            public Iterable<Node> doInTransaction(GraphDatabaseService database) throws Exception {
                return Iterators.asResourceIterable(database.findNodes(Label.label("Person")));
            }
        });

        Set<Node> nodes = new HashSet<>();

        for (Node node : input) {
            nodes.add(node);
        }

        assertEquals(4, nodes.size());
        assertEquals(noTx + 1, monitor.getNumberOfCommittedTransactions());
    }
}
