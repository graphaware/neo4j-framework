package com.graphaware.common.util.testing;

import java.util.Comparator;

/**
 * Comparator class for rank-node pairs
 */
public class RankNodePairComparator implements Comparator<RankNodePair> {
    @Override
    public int compare(RankNodePair first, RankNodePair second) {
        if(first.rank() > second.rank()){
            return -1;
        }
        if(first.rank() < second.rank()){
            return 1;
        }
        return 0;
    }
}
