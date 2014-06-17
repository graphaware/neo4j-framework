package com.graphaware.generator.distribution;

import java.util.Comparator;
import java.util.List;

/**
 * A distribution of node degrees for {@link com.graphaware.generator.relationship.RelationshipGenerator}s.
 */
public interface DegreeDistribution {

    /**
     * Get the node degrees produced by this distribution.
     *
     * @return node degrees. Should be immutable (read-only).
     */
    List<Integer> getDegrees();

    /**
     * Returns true iff this distribution is a zero-list.
     *
     * @return true iff this distribution is a zero-list.
     */
    boolean isZeroList();

    /**
     * Get degree by index.
     *
     * @param index of the degree to get.
     * @return degree.
     */
    int get(int index);

    /**
     * Set degree by index.
     *
     * @param index of the degree to set.
     * @param value to set.
     */
    void set(int index, int value);

    /**
     * Decrease the degree at the specified index by 1.
     *
     * @param index to decrease.
     */
    void decrease(int index);

    /**
     * Get the size of the distribution, i.e., the number of nodes.
     *
     * @return size.
     */
    int size();

    /**
     * Make a duplicate of this distribution. This must be a deep copy, i.e. no objects must be shared between this and
     * the duplicated distribution.
     *
     * @return a duplicate.
     */
    DegreeDistribution duplicate();

    /**
     * Sort the degree distribution using the given comparator.
     *
     * @param comparator comparator.
     */
    void sort(Comparator<Integer> comparator);
}
