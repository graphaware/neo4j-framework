package com.graphaware.example.module;

import com.graphaware.common.strategy.InclusionStrategies;
import com.graphaware.common.strategy.RelationshipInclusionStrategy;
import com.graphaware.runtime.BaseGraphAwareRuntimeModule;
import com.graphaware.runtime.config.MinimalRuntimeModuleConfiguration;
import com.graphaware.runtime.config.RuntimeModuleConfiguration;
import com.graphaware.tx.event.improved.api.ImprovedTransactionData;
import com.graphaware.tx.executor.batch.IterableInputBatchTransactionExecutor;
import com.graphaware.tx.executor.batch.UnitOfWork;
import com.graphaware.tx.executor.single.TransactionCallback;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;

import java.util.Collections;

import static com.graphaware.example.module.Labels.FriendshipCounter;
import static com.graphaware.example.module.Relationships.FRIEND_OF;
import static org.neo4j.tooling.GlobalGraphOperations.at;

/**
 * {@link com.graphaware.runtime.GraphAwareRuntimeModule} that counts the total friendship strength in the database
 * and keeps it up to date.
 */
public class FriendshipStrengthModule extends BaseGraphAwareRuntimeModule {

    private final RuntimeModuleConfiguration configuration;
    private final FriendshipStrengthCounter counter;

    public FriendshipStrengthModule(String moduleId, GraphDatabaseService database) {
        super(moduleId);
        this.counter = new FriendshipStrengthCounter(database);

        //only take into account relationships with FRIEND_OF type:
        configuration = new MinimalRuntimeModuleConfiguration(
                InclusionStrategies.all().with(
                        new RelationshipInclusionStrategy() {
                            @Override
                            public boolean include(Relationship relationship) {
                                return relationship.isType(FRIEND_OF);
                            }
                        }
                ));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RuntimeModuleConfiguration getConfiguration() {
        return configuration;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(GraphDatabaseService database) {
        try (Transaction tx = database.beginTx()) {
            for (Node counter : at(database).getAllNodesWithLabel(FriendshipCounter)) {
                counter.delete();
            }
            tx.success();
        }

        new IterableInputBatchTransactionExecutor<>(
                database, 10000,
                new TransactionCallback<Iterable<Relationship>>() {
                    @Override
                    public Iterable<Relationship> doInTransaction(GraphDatabaseService database) throws Exception {
                        return at(database).getAllRelationships();
                    }
                },
                new UnitOfWork<Relationship>() {
                    @Override
                    public void execute(GraphDatabaseService database, Relationship relationship, int batchNumber, int stepNumber) {
                        counter.handleCreatedFriendships(Collections.singleton(relationship));
                    }
                }
        ).execute();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void beforeCommit(ImprovedTransactionData transactionData) {
        if (transactionData.mutationsOccurred()) {
            counter.handleCreatedFriendships(transactionData.getAllCreatedRelationships());
            counter.handleChangedFriendships(transactionData.getAllChangedRelationships());
            counter.handleDeletedFriendships(transactionData.getAllDeletedRelationships());
        }
    }
}
