package com.graphaware.common.util;

import java.util.Objects;

/**
 * An immutable pair of objects of the same type, where the order of the pair does not matter.
 *
 * @param <T> type of the objects in the pair.
 */
public class UnorderedPair<T> extends SameTypePair<T> {

    /**
     * Construct a new pair.
     *
     * @param first  first element, can be null.
     * @param second second element, can be null.
     */
    public UnorderedPair(T first, T second) {
        super(first, second);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj instanceof UnorderedPair) {
            UnorderedPair that = (UnorderedPair) obj;

            return (Objects.equals(first(), that.first()) && Objects.equals(second(), that.second())) ||
                    (Objects.equals(first(), that.second()) && Objects.equals(second(), that.first()));
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hashCode(first()) + Objects.hashCode(second());
    }

}
