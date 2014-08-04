package com.graphaware.common.util.testing;

import java.util.List;

/**
 * Compares the result lists of two objects
 *
 */
public class SimilarityComparison {

    /**
     * Returns the most basic similarity
     * measure of the two lists of THE SAME LENGTH!
     *
     * TODO: Perhaps weight by the order in case of ranks?
     *
     * @param a
     * @param b
     * @return
     */
    public double compareListsOfEqualLength(List a, List b) {
        if(a.size() != b.size())
            throw  new RuntimeException("Two lists of unequal length were tested for similarity!");

        int length = a.size();
        int numerator = 0;

        for(int i = 0; i < length; ++i) {
            if(a.get(i).equals(b.get(i)))
                numerator ++;
        }

        return (double) numerator / (double) length;
    }

    /**
     * Returns an unordered similarity of
     * the two lists of THE SAME LENGTH!
     *
     * @param a
     * @param b
     * @return
     */
    public double unorderedComparisonOfEqualLengthLists(List a, List b) {
        if(a.size() != b.size())
            throw  new RuntimeException("Two lists of unequal length were tested for similarity!");

        int length = a.size();
        int numerator = 0;

        for(int i = 0; i < length; ++i) {
            if(a.contains(b.get(i)))
                numerator ++;
        }

        return (double) numerator / (double) length;
    }
}
