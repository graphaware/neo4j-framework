package com.graphaware.tx.event;

import com.graphaware.common.strategy.IncludeNoRelationships;
import com.graphaware.common.strategy.InclusionStrategies;
import com.graphaware.common.strategy.NodeInclusionStrategy;
import com.graphaware.tx.event.improved.api.FilteredTransactionData;
import com.graphaware.tx.event.improved.api.ImprovedTransactionData;
import com.graphaware.tx.event.improved.api.LazyTransactionData;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.event.TransactionData;
import org.neo4j.graphdb.event.TransactionEventHandler;
import org.neo4j.test.TestGraphDatabaseFactory;

/**
 * Only for documentation. If you need to change this class, change the code in README.md as well please.
 */
public class JustForDocs {

    private void justForDocs() {
        GraphDatabaseService database = new TestGraphDatabaseFactory().newImpermanentDatabase();
        database.registerTransactionEventHandler(new TransactionEventHandler<Object>() {
            @Override
            public Object beforeCommit(TransactionData data) throws Exception {
                ImprovedTransactionData improvedTransactionData = new LazyTransactionData(data);

                //have fun here with improvedTransactionData!

                return null;
            }

            @Override
            public void afterCommit(TransactionData data, Object state) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void afterRollback(TransactionData data, Object state) {
                //To change body of implemented methods use File | Settings | File Templates.
            }
        });
    }

    private void justForDocs2() {
        GraphDatabaseService database = new TestGraphDatabaseFactory().newImpermanentDatabase();
        database.registerTransactionEventHandler(new TransactionEventHandler.Adapter<Object>() {
            @Override
            public Object beforeCommit(TransactionData data) throws Exception {
                InclusionStrategies inclusionStrategies = InclusionStrategies.all()
                        .with(new NodeInclusionStrategy() {
                            @Override
                            public boolean include(Node node) {
                                return node.getProperty("name", "default").equals("Two");
                            }
                        })
                        .with(IncludeNoRelationships.getInstance());

                ImprovedTransactionData improvedTransactionData
                        = new FilteredTransactionData(new LazyTransactionData(data), inclusionStrategies);

                //have fun here with improvedTransactionData!

                return null;
            }
        });
    }
}
