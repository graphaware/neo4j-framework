package com.graphaware.common.util;

import java.util.Objects;

/**
 * An immutable pair of objects, where the order of the pair does not matter.
 *
 * @param <T> type of the objects in the pair.
 */
public class UnorderedPair<T> {
    private final T first;
    private final T second;

    /**
     * Construct a new pair.
     *
     * @param a first element, can be null.
     * @param b second element, can be null.
     */
    public UnorderedPair(T a, T b) {
        first = a;
        second = b;
    }

    /**
     * Return the first element of the pair.
     *
     * @return first element, can be null.
     */
    public T first() {
        return first;
    }

    /**
     * Return the second element of the pair.
     *
     * @return second element, can be null.
     */
    public T second() {
        return second;
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

            return (Objects.equals(this.first(), that.first()) && Objects.equals(this.second(), that.second())) ||
                    (Objects.equals(this.first(), that.second()) && Objects.equals(this.second(), that.first()));
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hashCode(first) + Objects.hashCode(second);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "(" + Objects.toString(first) + ", " + Objects.toString(second) + ")";
    }
}
