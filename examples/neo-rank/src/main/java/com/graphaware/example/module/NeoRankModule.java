package com.graphaware.example.module;

// GRAPHAWARE FRAMEWORK
import com.graphaware.common.strategy.InclusionStrategies;
import com.graphaware.common.strategy.RelationshipInclusionStrategy;

// ENUMS
import static com.graphaware.example.module.Relationships.*;
import com.graphaware.runtime.BaseGraphAwareRuntimeModule;
import com.graphaware.runtime.config.MinimalRuntimeModuleConfiguration;
import com.graphaware.runtime.config.RuntimeModuleConfiguration;
import com.graphaware.tx.event.improved.api.ImprovedTransactionData;
import com.graphaware.tx.executor.batch.IterableInputBatchTransactionExecutor;
import com.graphaware.tx.executor.batch.UnitOfWork;
import com.graphaware.tx.executor.single.TransactionCallback;

// NEO4J
import java.util.Collections;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import static org.neo4j.tooling.GlobalGraphOperations.at;



/**
 * A module for a PageRank-like centrality measure on Neo4j database
 * @author Vojtech Havlicek (Graphaware)
 */

public class NeoRankModule extends BaseGraphAwareRuntimeModule {
    
    private final RuntimeModuleConfiguration configuration;
   
    public NeoRankModule(String moduleId, GraphDatabaseService database) {    
       super(moduleId);
       configuration = new MinimalRuntimeModuleConfiguration(
                InclusionStrategies.all().with(
                    new RelationshipInclusionStrategy(){
                        @Override
                        public boolean include(Relationship relationship){
                            return relationship.isType(CASUAL);
                        }
                    }
            ));
    }
    
    /**
     * {@inheritDoc}
     * @return 
     */
    @Override
    public RuntimeModuleConfiguration getConfiguration() {
        return configuration;
    }
    
    /**
     * {@inheritDoc } 
     * @param database
     */
    @Override
    public void initialize(GraphDatabaseService database) 
    {   
        try (Transaction tx = database.beginTx())
        {   
            
            // NeoRank init here
            
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
                        
                    }
                }
        ).execute();
    }
    
    /**
     * 
     * @param transactionData 
     */
    @Override
    public void beforeCommit(ImprovedTransactionData transactionData) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    
}
