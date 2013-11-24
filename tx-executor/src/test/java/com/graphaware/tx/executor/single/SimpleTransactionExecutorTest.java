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

package com.graphaware.tx.executor.single;

import com.graphaware.common.test.IterableUtils;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.TransactionFailureException;
import org.neo4j.test.TestGraphDatabaseFactory;

import static junit.framework.Assert.assertEquals;

/**
 * Unit test for {@link com.graphaware.tx.executor.single.SimpleTransactionExecutor}.
 */
public class SimpleTransactionExecutorTest {

    private GraphDatabaseService database;
    private TransactionExecutor executor;

    @Before
    public void setUp() {
        database = new TestGraphDatabaseFactory().newImpermanentDatabase();
        executor = new SimpleTransactionExecutor(database);
    }

    @Test
    public void nodeShouldBeSuccessfullyCreatedInTransaction() {
        executor.executeInTransaction(new VoidReturningCallback() {
            @Override
            public void doInTx(GraphDatabaseService database) {
                database.createNode();
            }
        });

        Assert.assertEquals(2, IterableUtils.countNodes(database));
    }

    @Test
    public void nodeShouldBeSuccessfullyDeletedInTransaction() {
        executor.executeInTransaction(new VoidReturningCallback() {
            @Override
            protected void doInTx(GraphDatabaseService database) {
                database.getNodeById(0).delete();
            }
        });

        Assert.assertEquals(0, IterableUtils.countNodes(database));
    }

    @Test(expected = TransactionFailureException.class)
    public void deletingNodeWithRelationshipsShouldThrowException() {
        createNodeAndRelationship();

        executor.executeInTransaction(new TransactionCallback<Void>() {
            @Override
            public Void doInTransaction(GraphDatabaseService database) {
                database.getNodeById(0).delete();
                return null;
            }
        });
    }

    @Test
    public void deletingNodeWithRelationshipsShouldNotSucceed() {
        createNodeAndRelationship();

        Assert.assertEquals(2, IterableUtils.countNodes(database));

        executor.executeInTransaction(new TransactionCallback<Void>() {
            @Override
            public Void doInTransaction(GraphDatabaseService database) {
                database.getNodeById(0).delete();
                return null;
            }
        }, KeepCalmAndCarryOn.getInstance());

        Assert.assertEquals(2, IterableUtils.countNodes(database));
    }

    private void createNodeAndRelationship() {
        executor.executeInTransaction(new TransactionCallback<Void>() {
            @Override
            public Void doInTransaction(GraphDatabaseService database) {
                Node node = database.createNode();
                node.createRelationshipTo(database.getNodeById(0), DynamicRelationshipType.withName("TEST_REL_TYPE"));
                return null;
            }
        });
    }
}
