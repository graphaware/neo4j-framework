/*
 * Copyright (c) 2013 GraphAware
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

package com.graphaware.runtime;

import com.graphaware.runtime.config.DefaultRuntimeConfiguration;
import com.graphaware.runtime.metadata.ProductionSingleNodeMetadataRepository;
import com.graphaware.runtime.metadata.TimerDrivenModuleContext;
import com.graphaware.runtime.module.DeliberateTransactionRollbackException;
import com.graphaware.runtime.module.TimerDrivenModule;
import com.graphaware.runtime.module.TxDrivenModule;
import com.graphaware.tx.event.improved.api.ImprovedTransactionData;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.TransactionFailureException;
import org.neo4j.test.TestGraphDatabaseFactory;

import java.util.Iterator;

import static com.graphaware.runtime.config.RuntimeConfiguration.GA_METADATA;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.neo4j.tooling.GlobalGraphOperations.at;

/**
 * Unit test for {@link ProductionRuntime}.
 */
public class RealDatabaseProductionRuntimeTest extends DatabaseRuntimeTest {

    @Before
    public void setUp() {
        database = new TestGraphDatabaseFactory().newImpermanentDatabase();
        repository = new ProductionSingleNodeMetadataRepository(database, DefaultRuntimeConfiguration.getInstance());
    }

    @After
    public void tearDown() {
        database.shutdown();
    }

    private TimerDrivenModule mockTimerModule() {
        return mockTimerModule(MOCK);
    }

    private TimerDrivenModule mockTimerModule(String id) {
        TimerDrivenModuleContext mockContext = mock(TimerDrivenModuleContext.class);

        TimerDrivenModule mockModule = mock(TimerDrivenModule.class);
        when(mockModule.getId()).thenReturn(id);
        when(mockModule.createInitialContext(database)).thenReturn(mockContext);

        return mockModule;
    }

    @Test(expected = TransactionFailureException.class)
    public void shouldNotBeAllowedToDeleteRuntimeMetadataNode() {
        GraphAwareRuntime runtime = GraphAwareRuntimeFactory.createRuntime(database);
        runtime.start();

        try (Transaction tx = database.beginTx()) {
            getMetadataNode().delete();
            tx.success();
        }
    }

    @Test
    public void moduleThrowingExceptionShouldRollbackTransaction() {
        TxDrivenModule mockModule = mockTxModule(MOCK);
        doThrow(new DeliberateTransactionRollbackException("Deliberate testing exception")).when(mockModule).beforeCommit(any(ImprovedTransactionData.class));

        GraphAwareRuntime runtime = createRuntime();
        runtime.registerModule(mockModule);
        runtime.start();

        try (Transaction tx = getTransaction()) {
            Node node = createNode();
            node.setProperty("test", "test");
            tx.success();
        } catch (Exception e) {
            //ok
        }

        assertEquals(1, countNodes()); //just the node for storing metadata
    }

    @Test(expected = IllegalStateException.class)
    public void shouldNotBeAbleToRegisterDifferentModulesWithSameId() {

        GraphAwareRuntime runtime = createRuntime();
        runtime.registerModule(mockTxModule());
        runtime.registerModule(mockTimerModule());
    }

    protected Node getMetadataNode() {
        Node root = null;

        try (Transaction tx = database.beginTx()) {
            Iterator<Node> roots = at(database).getAllNodesWithLabel(GA_METADATA).iterator();
            if (roots.hasNext()) {
                root = roots.next();
            }

            if (roots.hasNext()) {
                throw new IllegalStateException("There is more than 1 runtime root node!");
            }

            tx.success();
        }

        return root;
    }

    protected Node createMetadataNode() {
        Node root;

        try (Transaction tx = database.beginTx()) {
            if (getMetadataNode() != null) {
                throw new IllegalArgumentException("Runtime root already exists!");
            }
            root = database.createNode(GA_METADATA);
            tx.success();
        }

        return root;
    }
}
