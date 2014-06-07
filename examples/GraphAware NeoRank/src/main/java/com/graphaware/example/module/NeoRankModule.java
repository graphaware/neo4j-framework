/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.graphaware.example.module;

// GRAPHAWARE FRAMEWORK
import com.graphaware.common.strategy.InclusionStrategies;
import com.graphaware.common.strategy.RelationshipInclusionStrategy;
import com.graphaware.runtime.BaseGraphAwareRuntimeModule;
import com.graphaware.runtime.config.MinimalRuntimeModuleConfiguration;
import com.graphaware.runtime.config.RuntimeModuleConfiguration;
import com.graphaware.tx.event.improved.api.ImprovedTransactionData;

// ENUMS
import static com.graphaware.example.module.Relationships.*;

// NEO4J
import java.util.Collections;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;



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
    
    
    @Override
    public void beforeCommit(ImprovedTransactionData transactionData) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    
}
