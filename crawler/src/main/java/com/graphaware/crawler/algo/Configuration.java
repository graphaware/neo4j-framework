/*
 * Configuration class can contain whatever is important, 
  * but should mainly include the strategies
 *
 */

package com.graphaware.crawler.algo;

import com.graphaware.common.strategy.NodeInclusionStrategy;
import com.graphaware.common.strategy.RelationshipInclusionStrategy;

/**
 *
 * @author Vojtech Havlicek (Graphaware)
 */public interface Configuration {
    
     NodeInclusionStrategy getNodeInclusionStrategy();
     RelationshipInclusionStrategy getRelInclusionStrategy();
}
