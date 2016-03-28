/*
 * Copyright (c) 2013-2016 GraphAware
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

package com.graphaware.example.module;

import com.graphaware.common.policy.RelationshipInclusionPolicy;
import com.graphaware.runtime.config.FluentTxDrivenModuleConfiguration;
import com.graphaware.runtime.config.TxDrivenModuleConfiguration;
import com.graphaware.runtime.module.BaseTxDrivenModule;
import com.graphaware.tx.event.improved.api.ImprovedTransactionData;
import com.graphaware.tx.executor.batch.IterableInputBatchTransactionExecutor;
import com.graphaware.tx.executor.batch.UnitOfWork;
import com.graphaware.tx.executor.input.TransactionalInput;
import com.graphaware.tx.executor.single.TransactionCallback;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.helpers.collection.Iterables;
import org.neo4j.helpers.collection.Iterators;

import java.util.Collections;

import static com.graphaware.example.module.Labels.FriendshipCounter;
import static com.graphaware.example.module.Relationships.FRIEND_OF;

/**
 * {@link com.graphaware.runtime.module.TxDrivenModule} that counts the total friendship strength in the database
 * and keeps it up to date.
 */
public class FriendshipStrengthModule extends BaseTxDrivenModule<Void> {

    private final TxDrivenModuleConfiguration configuration;
    private final FriendshipStrengthCounter counter;

    public FriendshipStrengthModule(String moduleId, GraphDatabaseService database) {
        super(moduleId);
        this.counter = new FriendshipStrengthCounter(database);

        //only take into account relationships with FRIEND_OF type:
        configuration = FluentTxDrivenModuleConfiguration
                .defaultConfiguration()
                .with(
                        new RelationshipInclusionPolicy.Adapter() {
                            @Override
                            public boolean include(Relationship relationship) {
                                return relationship.isType(FRIEND_OF);
                            }
                        }
                );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TxDrivenModuleConfiguration getConfiguration() {
        return configuration;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(GraphDatabaseService database) {
        try (Transaction tx = database.beginTx()) {
            for (Node counter : Iterators.asResourceIterable(database.findNodes(FriendshipCounter))) {
                counter.delete();
            }
            tx.success();
        }

        new IterableInputBatchTransactionExecutor<>(
                database, 10000,
                new TransactionalInput<>(database, 10000, new TransactionCallback<Iterable<Relationship>>() {
                    @Override
                    public Iterable<Relationship> doInTransaction(GraphDatabaseService database) throws Exception {
                        return database.getAllRelationships();
                    }
                }),
                (db, relationship, batchNumber, stepNumber) -> counter.handleCreatedFriendships(Collections.singleton(relationship))
        ).execute();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Void beforeCommit(ImprovedTransactionData transactionData) {
        if (transactionData.mutationsOccurred()) {
            counter.handleCreatedFriendships(transactionData.getAllCreatedRelationships());
            counter.handleChangedFriendships(transactionData.getAllChangedRelationships());
            counter.handleDeletedFriendships(transactionData.getAllDeletedRelationships());
        }

        return null;
    }
}
