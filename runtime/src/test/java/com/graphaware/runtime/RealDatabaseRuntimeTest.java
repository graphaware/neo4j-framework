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
import com.graphaware.runtime.metadata.ProductionSingleNodeMetadataRepository;
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
import static org.neo4j.tooling.GlobalGraphOperations.at;

/**
 * Unit test for {@link ProductionRuntime}.
 */
public class RealDatabaseRuntimeTest extends DatabaseBackedRuntimeTest {

    @Before
    public void setUp() {
        database = new TestGraphDatabaseFactory().newImpermanentDatabase();
        repository = new ProductionSingleNodeMetadataRepository(database, DefaultRuntimeConfiguration.getInstance());
    }

    @After
    public void tearDown() {
        database.shutdown();
    }

    @Test(expected = TransactionFailureException.class)
    public void shouldNotBeAllowedToDeleteRuntimeRootNode() {
        GraphAwareRuntime runtime = GraphAwareRuntimeFactory.createRuntime(database);
        runtime.start();

        try (Transaction tx = database.beginTx()) {
            getRuntimeRoot().delete();
            tx.success();
        }
    }

    protected Node getRuntimeRoot() {
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

    protected Node createRuntimeRoot() {
        Node root;

        try (Transaction tx = database.beginTx()) {
            if (getRuntimeRoot() != null) {
                throw new IllegalArgumentException("Runtime root already exists!");
            }
            root = database.createNode(GA_METADATA);
            tx.success();
        }

        return root;
    }
}
