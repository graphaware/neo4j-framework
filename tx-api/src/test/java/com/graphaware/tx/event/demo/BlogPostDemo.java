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

package com.graphaware.tx.event.demo;

import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.TransactionFailureException;
import org.neo4j.graphdb.event.TransactionData;
import org.neo4j.graphdb.event.TransactionEventHandler;
import org.neo4j.test.TestGraphDatabaseFactory;

/**
 * Demo for a blog post about improved TX event API.
 */
public class BlogPostDemo {

    @Test
    public void showSimpleEventHandling() {
        GraphDatabaseService database = new TestGraphDatabaseFactory().newImpermanentDatabase();

        database.registerTransactionEventHandler(new TransactionEventHandler<Void>() {
            @Override
            public Void beforeCommit(TransactionData data) throws Exception {
                System.out.println("Committing transaction");
                return null;
            }

            @Override
            public void afterCommit(TransactionData data, Void state) {
                System.out.println("Committed transaction");
            }

            @Override
            public void afterRollback(TransactionData data, Void state) {
                System.out.println("Transaction rolled back");
            }
        });

        Transaction tx = database.beginTx();
        try {
            database.createNode();
            tx.success();
        } finally {
            tx.finish();
        }

        /**
         prints:
         > Committing transaction
         > Committed transaction
         **/
    }

    @Test(expected = TransactionFailureException.class)
    public void attemptLoggingDeletedNodes() {
        GraphDatabaseService database = new TestGraphDatabaseFactory().newImpermanentDatabase();

        database.registerTransactionEventHandler(new TransactionEventHandler.Adapter<Void>() {
            @Override
            public Void beforeCommit(TransactionData data) throws Exception {
                for (Node deletedNode : data.deletedNodes()) {
                    StringBuilder message = new StringBuilder("About to delete node ID ")
                            .append(deletedNode.getId())
                            .append(" ");

                    for (String key : deletedNode.getPropertyKeys()) {
                        message.append("key=").append(key);
                        message.append("value=").append(deletedNode.getProperty(key));
                    }

                    System.out.println(message.toString());
                }

                return null;
            }
        });

        Transaction tx = database.beginTx();
        try {
            database.getNodeById(0).setProperty("test key", "test value");
            tx.success();
        } finally {
            tx.finish();
        }

        tx = database.beginTx();
        try {
            database.getNodeById(0).delete();
            tx.success();
        } finally {
            tx.finish();
        }
    }
}
