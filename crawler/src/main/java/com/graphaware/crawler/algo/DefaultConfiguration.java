/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.graphaware.crawler.algo;

import com.graphaware.common.strategy.IncludeAllNodes;
import com.graphaware.common.strategy.IncludeAllRelationships;
import com.graphaware.common.strategy.NodeInclusionStrategy;
import com.graphaware.common.strategy.RelationshipInclusionStrategy;

/**
 *
 * @author Vojtech Havlicek (Graphaware)
 */
public class DefaultConfiguration implements Configuration {

    @Override
    public NodeInclusionStrategy getNodeInclusionStrategy() {
        return IncludeAllNodes.getInstance();
    }

    @Override
    public RelationshipInclusionStrategy getRelInclusionStrategy() {
        return IncludeAllRelationships.getInstance();
    }
    
}
