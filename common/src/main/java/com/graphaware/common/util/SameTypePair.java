package com.graphaware.common.util;

/**
 * An immutable pair of objects, which must be of the same type.
 *
 * @param <T> type of the objects in the pair.
 */
public class SameTypePair<T> extends Pair<T, T> {

    /**
     * Construct a new pair.
     *
     * @param first  first element, can be null.
     * @param second second element, can be null.
     */
    public SameTypePair(T first, T second) {
        super(first, second);
    }
}
